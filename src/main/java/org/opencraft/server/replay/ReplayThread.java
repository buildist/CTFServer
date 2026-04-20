package org.opencraft.server.replay;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import org.opencraft.server.Server;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.model.Entity;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.replay.ReplayFile.ReplayChunk;

import static org.opencraft.server.replay.ReplayFile.adjust;

public class ReplayThread extends Thread {

  private static final ThreadLocal<NumberFormat> NUMBER_FORMATTER =
      ThreadLocal.withInitial(() -> new DecimalFormat("#.##"));

  private final Player player;
  private final int day;
  private final int month;
  private final int year;
  private final int id;
  private final boolean onlyViewMetadata;

  public ReplayThread(Player player, int day, int month, int year, int id, boolean onlyViewMetadata) {
    Objects.requireNonNull(player);

    this.player = player;
    this.day = day;
    this.month = month;
    this.year = year;
    this.id = id;
    this.onlyViewMetadata = onlyViewMetadata;

    setName(player.getName() + "'s replay thread");
    setDaemon(true);
  }

  public static boolean thisThread() {
    return (Thread.currentThread() instanceof ReplayThread);
  }

  /*
   * Returns true if (and only if) the given player is watching a replay and
   * the current operation over this player is executed by any thread other than ReplayThread.
   */
  public static boolean isUnsafe(Player player) {
    return player.watchingReplay && !thisThread();
  }

  private static String timestampToMmSs(int timestamp) {
    int seconds = timestamp / 1000;
    int minutes = seconds / 60;
    seconds -= minutes * 60;

    return adjust(minutes, 2) + ":" + adjust(seconds, 2);
  }

  private void clearLocalEntities() { // partially a copy-paste from UpdateTask.java
    Set<Entity> le = player.getLocalEntities();
    Object[] localEntities = le.toArray();
    for (Object object : localEntities) {
      Entity localEntity = (Entity) object;
      le.remove(localEntity);
      if (localEntity instanceof Player) {
        player.getActionSender().sendRemovePlayer((Player) localEntity);
      } else {
        player.getActionSender().sendRemoveEntity(localEntity);
      }
    }
  }

  private void clearAnnouncementAndKillFeed() {
    GameMode gameMode = World.getWorld().getGameMode();
    gameMode.clearKillFeedFor(player);
    gameMode.sendAnnouncement(player, "");
  }

  private void doLogic(ReplayFile file) throws IOException, InterruptedException {
    String map = file.getMap();
    player.sendMessage("- &eMap: " + map);
    player.sendMessage("- &eDate: " + (new Date(file.getRoundStartTimestamp())));
    player.sendMessage("- &eRecording started: " + (new Date(file.getRecordingStartTimestamp())));
    if (onlyViewMetadata) return;

    player.sendMessage("- &eUse '/leave' or '/replay stop' commands to quit viewer mode");

    ReplayChunk previous = null;
    boolean forwarding = false;
    int forwardingTo = 0;
    while (file.isNextChunkAvailable()) {
      ReplayFile.ReplayChunk chunk = file.readNextChunk();
      if (forwarding && chunk.deltaMillis() >= forwardingTo) {
        player.sendMessage("- &eReached the given timestamp: &f" + timestampToMmSs(forwardingTo));
        player.replaySpeed = 1.0D;

        forwarding = false;
      }
      chunk.sleepUntilSendingThisChunk(player, previous);
      if (player.requestedToLeaveReplay) return;
      checkUsedCommand();

      for (Packet packet : chunk.packets()) {
        player.getSession().send(packet);
      }
      previous = chunk;

      int previousDelta = chunk.deltaMillis();
      synchronized (player) {
        if (player.replaySpeedChanged) {
          if (forwarding) {
            player.sendMessage("- &eForwarding the replay was cancelled");
            player.sendMessage("- &eTry &f/replay time <mm/ss>&e again if needed");

            forwarding = false;
          }
          player.sendMessage("- &eThe replay speed is now &f" +
              NUMBER_FORMATTER.get().format(player.replaySpeed) + "x");

          player.replaySpeedChanged = false;
        }
        if (player.replayTimestampChanged) {
          player.replayTimestampChanged = false;

          if (player.replayTimestamp < previousDelta) {
            player.sendMessage("- &eCannot jump to the older events, please start a new replay");

            return;
          }
          forwarding = true;
          forwardingTo = player.replayTimestamp;
          double newReplaySpeed = 10.0D;
          int toWait = (int) ((forwardingTo - previousDelta) / (newReplaySpeed * 1000));
          player.sendMessage("- &eYou will be brought to the specified timestamp in &f" + toWait + "&es.");
          player.sendMessage("- &eLet us forward some events a real quick...");
          player.replaySpeed = newReplaySpeed;

          /*
           * Nothing bad should happen if "/replay time" is executed multiple
           * times during forwarding = true. All necessary checks are already done.
           */
        }
        if (player.askedReplayTimestamp) {
          player.sendMessage("- &eThe current timestamp is &f" + timestampToMmSs(previousDelta));

          player.askedReplayTimestamp = false;
        }
        if (player.askedToPauseReplay) {
          player.askedToPauseReplay = false;

          player.sendMessage("- &ePaused");
          boolean asked;
          while (!(asked = player.askedToResumeReplay) && shouldWait()) {
            player.wait(100L); // the lock will be lifted here
          }
          player.askedToResumeReplay = false;

          if (asked) player.sendMessage("- &eResumed");
        }
      }
    }
    if (forwarding) {
      player.sendMessage("- &eCould not bring you to &f" + timestampToMmSs(forwardingTo));
      player.sendMessage("- &eLooks like the replay is shorter than that");
      player.sendMessage("");
    }
    player.sendMessage("- &eFinished reading the replay, type /leave to quit the viewer");
    if (forwarding) player.sendMessage("- &ePlease start it again if needed");
  }

  @Override
  public void run() {
    if (!onlyViewMetadata) {
      synchronized (player) {
        if (player.watchingReplay) {
          player.sendMessage("- &eYou are already watching a replay");

          return;
        }
        player.watchingReplay = true;
        player.usedCommandDuringReplay = false;
        player.replaySpeed = 1.0D;
        player.replaySpeedChanged = false;
        player.replayTimestampChanged = false;
        player.askedReplayTimestamp = false;
        player.askedToPauseReplay = false;
        player.askedToResumeReplay = false;
      }
    }

    boolean finishedLogic = false;
    try (ReplayFile file = new ReplayFile(day, month, year, id)) {
      file.setReading(true);

      if (!file.canRead()) {
        player.sendMessage("- &eCould not find such a replay");

        return;
      }
      if (ReplayManager.getInstance().isBusy(file)) {
        player.sendMessage("- &eThis replay file is not ready yet");

        return;
      }
      file.open();
      file.readHeader();
      if (!file.canRead()) {
        // replay was captured using a newer version of the server software
        player.sendMessage("- &eCannot read this replay, this might be a temporary issue");

        return;
      }

      if (!onlyViewMetadata) {
        clearLocalEntities();
        clearAnnouncementAndKillFeed();

        GameMode.removeBlockDefAndZones(player, World.getWorld().getLevel());
      }
      doLogic(file);
      finishedLogic = true;

      if (!onlyViewMetadata) {
        synchronized (player) {
          while (shouldWait()) {
            checkUsedCommand();

            player.wait(100L);
          }

          if (player.requestedToLeaveReplay) {
            player.requestedToLeaveReplay = false;
          }
        }
      }
    } catch (InterruptedException e) {
      interrupt();

      if (!finishedLogic) player.sendMessage("- &eThe operation was interrupted");
    } catch (IOException e) {
      Server.log("I/O error " + (finishedLogic ?
          "during waiting for player to quit" : "during reading a replay"));
      Server.log(e);

      if (!finishedLogic) player.sendMessage("- &eAn error occurred while reading the replay");
    } finally {
      if (!onlyViewMetadata) {
        MinecraftSession session = player.getSession();
        if (session.getPlayer() != null) { // still connected?
          clearAnnouncementAndKillFeed();

          for (short id = 0; id < 255; id++) { // do not remove -1 (255)
            player.getActionSender().sendRemovePlayerName(id);
            player.getActionSender().sendRemoveEntity(id);
          }
          LevelGzipper.getLevelGzipper().gzipLevel(session);
        }

        synchronized (player) {
          player.watchingReplay = false;
          player.usedCommandDuringReplay = false;
        }

        player.getUI().invalidateHUD();
      }
    }
  }

  // expected to be executed under synchronized (player) {...}
  private boolean shouldWait() {
    return !player.requestedToLeaveReplay && World.getWorld().getPlayerList().contains(player);
  }

  private void checkUsedCommand() {
    if (player.usedCommandDuringReplay) {
      player.sendMessage("- &eUnfortunately, you cannot use any commands except /leave");
      player.sendMessage("- &ewhile watching a replay. This may be a temporary limitation");

      player.usedCommandDuringReplay = false;
    }
  }
}
