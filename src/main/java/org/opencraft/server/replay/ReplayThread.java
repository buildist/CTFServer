package org.opencraft.server.replay;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import org.opencraft.server.Server;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.model.Entity;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.packet.Packet;

public class ReplayThread extends Thread {

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

        player.sendMessage("- &eUse /leave command to quit viewer mode");

        long started = System.currentTimeMillis();
        while (file.isNextChunkAvailable()) {
            ReplayFile.ReplayChunk chunk = file.readNextChunk();
            chunk.sleepUntilSendingThisChunk(player, started);
            if (player.requestedToLeaveReplay) return;

            for (Packet packet : chunk.packets()) {
                player.getSession().send(packet);
            }
        }
        player.sendMessage("- &eFinished reading the replay, type /leave to quit the viewer");
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
            }
        }

        boolean finishedLogic = false;
        try (ReplayFile file = new ReplayFile(day, month, year, id)) {
            file.setReading(true);

            if (!file.canRead()) {
                player.sendMessage("- &eCould not find such replay");

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

            clearLocalEntities();
            clearAnnouncementAndKillFeed();
            doLogic(file);
            finishedLogic = true;

            if (!onlyViewMetadata) {
                synchronized (player) {
                    while (!player.requestedToLeaveReplay && World.getWorld().getPlayerList().contains(player)) {
                        player.wait();
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

            if (!finishedLogic) player.sendMessage("- &eAn error occurred during reading the replay");
        } finally {
            if (!onlyViewMetadata) {
                clearAnnouncementAndKillFeed();

                LevelGzipper.getLevelGzipper().gzipLevel(player.getSession());

                synchronized (player) {
                    player.watchingReplay = false;
                }

                player.getUI().invalidateHUD();
            }
        }
    }
}
