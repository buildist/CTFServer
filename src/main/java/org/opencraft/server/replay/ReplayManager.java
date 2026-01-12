package org.opencraft.server.replay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.opencraft.server.Server;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.net.FakePlayerBase;
import org.opencraft.server.net.packet.Packet;

public class ReplayManager {

  public static class IOThread extends Thread {
    public static final int MAX_ELEMENTS = 1024;
    private static final IOThread INSTANCE = new IOThread();

    private final List<Packet> packetQueue = new ArrayList<>(MAX_ELEMENTS);
    private boolean notifiedSkipping;

    private IOThread() {
      setName("ReplayManager's disk I/O thread");
      setDaemon(true);
    }

    public synchronized void enqueuePacket(Packet packet) {
      int size;
      if ((size = packetQueue.size()) >= MAX_ELEMENTS) {
        if (!notifiedSkipping) {
          Server.log("ReplayManager cannot keep up, " + size +
              " packets are in queue to be recorded. First packet was dropped");

          notifiedSkipping = true;
        }

        return;
      }
      packetQueue.add(packet);

      notifyAll();
    }

    @Override
    public void run() {
      while (true) {
        List<Packet> queueCopy;
        synchronized (this) {
          queueCopy = new ArrayList<>(packetQueue);
          packetQueue.clear();
        }

        for (Packet packet : queueCopy) {
          ReplayManager.INSTANCE.registerPacket(packet, true);
        }

        synchronized (this) {
          try {
            while (packetQueue.isEmpty()) wait();
          } catch (InterruptedException e) {
            interrupt();

            Server.log(getName() + " was interrupted. This may "
                + "lead to corrupted replay files and degraded performance");

            break;
          }
        }
      }
    }
  }

  private static final ReplayManager INSTANCE = new ReplayManager();

  private long recordingStartTimestamp;
  private boolean recording;
  private ReplayFile replayFile;

  private long timestamp;
  private List<Packet> packetsCollected;

  static {
    IOThread.INSTANCE.start();
  }

  private ReplayManager() {
  }

  public static ReplayManager getInstance() {
    return INSTANCE;
  }

  public synchronized void roundEnded() {
    if (recording) {
      recording = false;

      flushPackets();
      FakePlayerBase.CAMERA_MAN.getSession().setConnected();
    }
    if (replayFile != null) {
      try {
        replayFile.close();
      } catch (IOException ignored) {
      }
      replayFile = null;
    }
    recordingStartTimestamp = 0L;
    timestamp = 0L;
  }

  public synchronized void startRecording() {
    if (recording) return;

    Calendar calendar = Calendar.getInstance();
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    int month = calendar.get(Calendar.MONTH) + 1;
    int year = calendar.get(Calendar.YEAR);
    //synchronized (ReplayFile.class) { // locateFreeId is only used by this singleton, entirely synchronized
    int id = ReplayFile.locateFreeId(day, month, year);
    if (id == ReplayFile.FAILED_TO_LOCATE_FREE_ID) {
      Server.log("Failed to locate free id for a ReplayFile");

      return;
    }
    replayFile = new ReplayFile(day, month, year, id);
    replayFile.setReading(false);
    Level level = World.getWorld().getLevel();
    try {
      replayFile.open();
      replayFile.setMap(level.id);
      replayFile.setRoundStartTimestamp(World.getWorld().getGameMode().gameStartTime);
      recordingStartTimestamp = System.currentTimeMillis();
      replayFile.setRecordingStartTimestamp(recordingStartTimestamp);

      replayFile.writeHeader();
    } catch (IOException e) {
      Server.log("Failed to start recording");
      Server.log(e);

      return;
    }
    //}
    World.getWorld().broadcast("- &e[&c!&e] The game is now being recorded under identifier " + id);
    recording = true;

    LevelGzipper.getLevelGzipper().gzipLevel(FakePlayerBase.CAMERA_MAN.getSession(), level);

    FakePlayerBase.CAMERA_MAN.getUI().invalidateHUD();
  }

  public synchronized boolean isRecording() {
    return recording;
  }

  public boolean isBusy(ReplayFile file) {
    return isBusy(file.getFilename());
  }

  public boolean isBusy(String filename) {
    if (filename.contains("important")) return false;

    synchronized (this) {
      return recording && replayFile.getFilename().equals(filename);
    }
  }

  public void registerPacket(Packet packet) {
    registerPacket(packet, false);
  }

  /*
   * Expected to be an outgoing (clientbound) packet.
   */
  void registerPacket(Packet packet, boolean managed) {
    if (!managed) {
      IOThread.INSTANCE.enqueuePacket(packet);

      return;
    }

    synchronized (this) {
      if (!recording) return;

      long currentTime = System.currentTimeMillis();
      if (currentTime == timestamp) {
        packetsCollected.add(packet);

        return;
      }
      flushPackets();
      packetsCollected = new ArrayList<>();
      packetsCollected.add(packet);
      timestamp = currentTime;
    }
  }

  private void flushPackets() {
    if (timestamp == 0L) return;

    int deltaMillis = (int) (timestamp - recordingStartTimestamp);

    ReplayFile.ReplayChunk chunk = new ReplayFile.ReplayChunk(deltaMillis, packetsCollected);
    try {
      replayFile.writeChunk(chunk);
    } catch (IOException e) {
      Server.log("Failed to write chunk");
      Server.log(e);
    }
    packetsCollected = null;
  }
}
