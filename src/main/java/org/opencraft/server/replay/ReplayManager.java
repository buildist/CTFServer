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
  private static final ReplayManager INSTANCE = new ReplayManager();

  private long recordingStartTimestamp;
  private boolean recording;
  private ReplayFile replayFile;

  private long timestamp;
  private List<Packet> packetsCollected;

  private ReplayManager() {
  }

  public static ReplayManager getInstance() {
    return INSTANCE;
  }

  public synchronized void roundEnded() {
    if (recording) {
      recording = false;

      flushPackets();
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

  /*
   * Expected to be an outgoing (clientbound) packet.
   */
  public synchronized void registerPacket(Packet packet) {
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
