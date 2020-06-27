/*
 * Jacob_'s Capture the Flag for Minecraft Classic and ClassiCube
 * Copyright (c) 2010-2014 Jacob Morgan
 * Based on OpenCraft v0.2
 *
 * OpenCraft License
 *
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Distribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Distributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Distributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opencraft.server.game.impl;

import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.cmd.impl.RCommand;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.BlockLog;
import org.opencraft.server.model.BlockLog.BlockInfo;
import org.opencraft.server.model.BuildMode;
import org.opencraft.server.model.EntityID;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.MapController;
import org.opencraft.server.model.MapRatings;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUI;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;
import org.opencraft.server.persistence.SavePersistenceRequest;

import java.io.IOException;
import java.util.ArrayList;

public class LaserTagGameMode extends GameMode {

  private static final ArrayList<TempEntity> entities = new ArrayList<>(127);

  public LaserTagGameMode() {
    super();
    registerCommand("r", RCommand.getCommand());
  }

  @Override
  public void playerConnected(final Player player) {
    String welcome = Configuration.getConfiguration().getWelcomeMessage();
    if (!player.isNewPlayer) {
      if (welcome != null && !welcome.equals("null")) {
        player.getActionSender().sendChatMessage("&a" + welcome);
      }
      player
          .getActionSender()
          .sendChatMessage("&bSay /join to start playing, or /spec to " + "spectate");
      player.getActionSender().sendChatMessage("&aSay /help to learn how to play");
      player.getActionSender().sendChatMessage("&aSay /rules to read the rules");
    } else {
      String helpText;
      player
          .getActionSender()
          .sendChatMessage("&bWelcome to Laser Tag! Here's how you play.");
      helpText = Constants.HELP_TEXT;
      player.getActionSender().sendChatMessage("&e" + helpText);
      player
          .getActionSender()
          .sendChatMessage(
              "&bSay /join to start playing or /spec to spectate"
                  + ". /help will show these instructions again.");
    }
    if (!player.getSession().ccUser) {
      player
          .getActionSender()
          .sendChatMessage(
              "-- &bWe recommend using the &aClassiCube &bclient"
                  + " (www.classicube.net) for more features.");
    }
  }

  @Override
  public boolean isSolidBlock(Level level, int x, int y, int z) {
    return false;
  }

  @Override
  public void playerChangedTeam(Player p) {

  }

  public void addSpawns() {
    Level map = World.getWorld().getLevel();
    Position redSpawn = map.redSpawnPosition.toBlockPos();
    Position blueSpawn = map.blueSpawnPosition.toBlockPos();
    map.setBlock(redSpawn.getX(), redSpawn.getY(), redSpawn.getZ() - 2, Constants.BLOCK_RESUPPLY);
    map.setBlock(blueSpawn.getX(), blueSpawn.getY(), blueSpawn.getZ() - 2, Constants.BLOCK_RESUPPLY);
  }

  @Override
  protected void resetGameMode() {
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      player.currentRoundPoints = 0;
      player.setHealth(GameSettings.getInt("Health"));
      player.setAmmo(GameSettings.getInt("Ammo"));
    }
  }

  private void updateKillFeed(Player attacker, Player defender) {
    synchronized (killFeed) {
      KillFeedItem item;
      KillFeedItem first = killFeed.isEmpty() ? null : killFeed.get(0);
      boolean isKill = defender.getHealth() == 0;
      if (!isKill && first != null && first.source == attacker && first.target == defender) {
        item = new KillFeedItem(attacker, defender, first.count + 1, false);
        killFeed.remove(0);
      } else {
        item = new KillFeedItem(attacker, defender, 1, isKill);
        if (isKill) defender.setAmmo(0);
      }
      killFeed.add(0, item);
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        if (p == attacker || p == defender) {
          sendAnnouncement(p, item.getMessage());
        }
      }

      if (killFeed.size() > 3) {
        killFeed.remove(killFeed.get(killFeed.size() - 1));
      }

      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        sendKillFeed(p);
      }
    }
  }

  public void endGame() {
    new Thread(
        new Runnable() {
          public void run() {
            try {
              int redPoints = 0;
              int bluePoints = 0;
              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                if (p.team == 0) {
                  redPoints += p.currentRoundPoints;
                } else if (p.team == 1) {
                  bluePoints += p.currentRoundPoints;
                }
              }

              String winner = null;
              int winnerID = -2;
              if (redPoints > bluePoints) {
                winner = "red";
                winnerID = 0;
              } else if (bluePoints > redPoints) {
                winner = "blue";
                winnerID = 1;
              }
              if (winner == null) {
                World.getWorld().broadcast("- &6The game ended in a tie!");
              } else {
                World.getWorld()
                    .broadcast("- &6The game has ended; the " + winner + " team wins!");
              }
              World.getWorld()
                  .broadcast(
                      "- &6Red had "
                          + redPoints
                          + " points, blue had "
                          + bluePoints
                          + ".");

              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                if (p.team != -1) {
                  p.setAttribute("games", (Integer) p.getAttribute("games") + 1);
                }
                if (p.team == winnerID) {
                  p.setAttribute("wins", (Integer) p.getAttribute("wins") + 1);
                }
                p.hasVoted = false;
                p.hasNominated = false;
              }
              Player[] top = getTopPlayers(3);
              World.getWorld().broadcast("- &3Top players for the round:");
              if (top[0] == null) {
                World.getWorld().broadcast("- &3Nobody");
              }
              for (int j = 0; j < 3; j++) {
                Player p = top[j];
                if (p == null) {
                  break;
                }
                World.getWorld()
                    .broadcast("- &2" + p.getName() + " - " + p.currentRoundPoints);
              }
              for (Player player : World.getWorld().getPlayerList().getPlayers()) {
                player.team = -1;
                player.sendToTeamSpawn();
              }
              rtvVotes = 0;
              rtvYesPlayers.clear();
              rtvNoPlayers.clear();
              if (GameSettings.getBoolean("Tournament")) {
                return;
              }
              World.getWorld().broadcast("- &aMap voting is now open for 40 seconds...");
              World.getWorld().broadcast("- &aSay /vote [mapname] to select the next map!");
              MapController.resetVotes();
              voting = true;
              int count = nominatedMaps.size();
              if (count > 3) {
                count = 3;
              }
              ArrayList<String> mapNames =
                  MapController.getRandomMapNames(
                      3 - count, new String[]{currentMap, previousMap});
              mapNames.addAll(nominatedMaps);
              String msg = "";
              for (String map : mapNames) {
                msg += map + ", ";
              }
              World.getWorld().broadcast("- &a" + msg);
              World.getWorld()
                  .broadcast(
                      "- &3Did you like the map you just played ("
                          + currentMap
                          + ")? Say /yes or /no followed by a reason (optional) to vote!");
              new Thread(
                  new Runnable() {
                    public void run() {
                      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                        try {
                          new SavePersistenceRequest(p).perform();
                        } catch (IOException ex) {
                        }
                      }
                    }
                  })
                  .start();
              Thread.sleep(40 * 1000);
              Level newLevel = MapController.getMostVotedForMap();
              ready = false;
              String rating = MapRatings.getRating(currentMap);
              World.getWorld().broadcast("- &3This map's approval rating is now " + rating);
              World.getWorld()
                  .broadcast("- &3See the ratings at http://jacobsc.tf/mapratings.");
              World.getWorld()
                  .broadcast(
                      "- &e" + newLevel.id + " had the most votes. Starting new " + "game!");
              Thread.sleep(7 * 1000);
              startGame(newLevel);
            } catch (Exception ex) {
              voting = false;
              Server.log(ex);
            }
          }
        })
        .start();
  }

  @Override
  public void setBlock(Player player, Level level, int x, int y, int z, int mode, int type) {
    int oldType = level.getBlock(x, y, z);
    int playerX = (player.getPosition().getX() - 16) / 32;
    int playerY = (player.getPosition().getY() - 16) / 32;
    int playerZ = (player.getPosition().getZ() - 16) / 32;
    int MAX_DISTANCE = 10;
    boolean ignore = false;
    if (mode == 0) {
      type = 0x00;
    }
    if (player.placeBlock != -1
        && (player.placeBlock != 7 || player.placeBlock == 7 && type == 1)) {
      type = player.placeBlock;
    }
    if (player.placeSolid && type == 1) {
      type = 7;
    }
    if (player.buildMode == BuildMode.BOX) {
      if (player.boxStartX == -1) {
        player.boxStartX = x;
        player.boxStartY = y;
        player.boxStartZ = z;
      } else {
        int lowerX = (player.boxStartX < x ? player.boxStartX : x);
        int lowerY = (player.boxStartY < y ? player.boxStartY : y);
        int lowerZ = (player.boxStartZ < z ? player.boxStartZ : z);
        int upperX = (player.boxStartX > x ? player.boxStartX : x);
        int upperY = (player.boxStartY > y ? player.boxStartY : y);
        int upperZ = (player.boxStartZ > z ? player.boxStartZ : z);
        for (int bx = lowerX; bx <= upperX; bx++) {
          for (int by = lowerY; by <= upperY; by++) {
            for (int bz = lowerZ; bz <= upperZ; bz++) {
              if (level.getBlock(bx, by, bz) != type) {
                if (player.placeBlock == -1) {
                  level.setBlock(bx, by, bz, type);
                } else {
                  level.setBlock(bx, by, bz, player.placeBlock);
                }
              }
            }
          }
        }
        player.boxStartX = -1;
        player.buildMode = BuildMode.NORMAL;
      }
    } else if (player.buildMode == BuildMode.BLOCK_INFO) {
      player.getActionSender().sendChatMessage(
          "ID: " + level.getBlock(x, y, z) + "Position: " + x + " " + z + " " + y);
      BlockInfo info = BlockLog.getInfo(x, y, z);
      if (info == null) {
        player.getActionSender().sendChatMessage("- &aNo one has changed this block yet.");
      } else {
        player
            .getActionSender()
            .sendChatMessage("- &aBlock last changed by: " + info.player.getName());
      }
      player.getActionSender().sendBlock(x, y, z, (short) oldType);
      player.buildMode = BuildMode.NORMAL;
    } else {
      if (player.team == -1 && !(player.isOp()) && !player.isVIP()) {
        ignore = true;
        player.getActionSender().sendChatMessage("- &eYou must join a team to build!");
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (short) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (short) 0);
        }
      } else if (!tournamentGameStarted) {
        ignore = true;
        player.getActionSender().sendChatMessage("- &aThe game has not started yet.");
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (short) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (short) 0);
        }
      } else if (!(x < playerX + MAX_DISTANCE
          && x > playerX - MAX_DISTANCE
          && y < playerY + MAX_DISTANCE
          && y > playerY - MAX_DISTANCE
          && z < playerZ + MAX_DISTANCE
          && z > playerZ - MAX_DISTANCE)) {
        ignore = true;
      } else if (z > level.ceiling) {
        ignore = true;
        player.getActionSender().sendChatMessage("- &eYou're not allowed to build this high!");
        player.outOfBoundsBlockChanges++;
        if (player.outOfBoundsBlockChanges == 10) {
          player
              .getActionSender()
              .sendChatMessage(
                  "- &cWARNING: You will be kicked automatically"
                      + " if you continue building here.");
        } else if (player.outOfBoundsBlockChanges == 16) {
          player.getActionSender().sendLoginFailure("\"Lag pillaring\" is not allowed");
          player.getSession().close();
        }
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (short) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (short) 0);
        }
      } else if (player.headBlockPosition != null
          && x == player.headBlockPosition.getX()
          && y == player.headBlockPosition.getY()
          && z == player.headBlockPosition.getZ()) {
        ignore = true;
        player.getActionSender().sendBlock(x, y, z, (short) oldType);
      } else if (player.brush) {
        int height = 3;
        int radius = 3;
        for (int offsetZ = -height; offsetZ <= radius; offsetZ++) {
          for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
              if (level.getBlock(offsetX + x, offsetY + y, offsetZ + z) != 7
                  && Math.abs(offsetX) + Math.abs(offsetY) + Math.abs(offsetZ)
                  <= Math.abs(radius)) {
                level.setBlock(offsetX + x, offsetY + y, offsetZ + z, type);
              }
            }
          }
        }
      } else if (level.isSolid(x, y, z)
          && (!player.isOp() || !player.placeSolid)
          && !GameSettings.getBoolean("Chaos")) {
        player.getActionSender().sendBlock(x, y, z, (short) level.getBlock(x, y, z));
      } else if ((type == BlockConstants.LAVA
          || type == BlockConstants.WATER
          || type == BlockConstants.ADMINIUM)
          && !player.isOp()) {
        player.getActionSender().sendBlock(x, y, z, (short) 0);
        player.getActionSender().sendChatMessage("- &eYou can't place this block type!");
      } else if (type > -1) {
        if (!ignore) {
          level.setBlock(x, y, z, (mode == 1 ? type : 0));
          BlockLog.logBlockChange(player, x, y, z);
        } else {
          player.getActionSender().sendBlock(x, y, z, (short) oldType);
        }
      }
      if (z <= level.ceiling) {
        player.outOfBoundsBlockChanges = 0;
      }
    }
  }

  public void addLaser(
      Player player,
      double x1, double y1, double z1,
      double x2, double y2, double z2) {
    int block = player.team == 0 ? Constants.LASER_RED : Constants.LASER_BLUE;
    double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
    double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
    dx /= d;
    dy /= d;
    dz /= d;
    for (int i = 1; i < d; i++) {
      addTempEntity(x1 + dx * i, y1 + dy * i, z1 + dz * i, block, 100);
    }
  }

  public void onHit(Player source, Player target, double x, double y, double z) {
    if (target.getHealth() == 0 || source.team == target.team || target.team == -1) return;

    target.setHealth(target.getHealth() - 1);
    source.incStat("hits");
    target.incStat("hitsTaken");
    source.addPoints(1);
    int block = source.team == 0 ? Constants.HIT_RED : Constants.HIT_BLUE;
    addTempEntity(x, y, z, block, 1000);

    if (target.getHealth() == 0) {
      source.incStat("kills");
      target.incStat("deaths");
      target.makeInvisible();
    }

    updateKillFeed(source, target);
  }

  public void onDied(Player target) {
    target.setHealth(0);
    target.incStat("deaths");
    updateKillFeed(null, target);
  }

  private void addTempEntity(double x, double y, double z, int block, long lifeTime) {
    TempEntity entity = new TempEntity(EntityID.get(), System.currentTimeMillis(), lifeTime);
    if (entity.id == -1) return;
    synchronized (entities) {
      entities.add(entity);
    }
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      player.getActionSender().sendExtSpawn(
          (byte) entity.id, "", "",
          (int) (x * 32), (int) (y * 32), (int) (z * 32),
          (byte) 0, (byte) 0);
      player.getActionSender().sendChangeModel((byte) entity.id, "" + block);
    }
  }

  public void step() {
    synchronized (killFeed) {
      boolean updated = false;
      for (int i = killFeed.size() - 1; i >= 0; i--) {
        if (System.currentTimeMillis() - killFeed.get(i).time > 10000) {
          killFeed.remove(i);
          updated = true;
        }
      }
      if (updated) {
        for (Player p : World.getWorld().getPlayerList().getPlayers()) {
          sendKillFeed(p);
        }
      }
    }

    synchronized (entities) {
      for (int i = 0; i < entities.size(); i++) {
        TempEntity e = entities.get(i);
        if (e.startTime + e.lifeTime < System.currentTimeMillis()) {
          entities.remove(i);
          EntityID.release(e.id);
          i--;
          for (Player p : World.getWorld().getPlayerList().getPlayers()) {
            p.getActionSender().sendRemoveEntity(e.id);
          }
        }
      }
    }

    long elapsedTime = System.currentTimeMillis() - World.getWorld().getGameMode().gameStartTime;
    if (elapsedTime > GameSettings.getInt("TimeLimit") * 60 * 1000) {
      gameStartTime = System.currentTimeMillis();
      endGame();
    }
  }

  @Override
  public PlayerUI createPlayerUI(Player p) {
    return new LTPlayerUI(p);
  }

  static class TempEntity {
    public final int id;
    public final long startTime;
    public final long lifeTime;

    public TempEntity(int id, long startTime, long lifeTime) {
      this.id = id;
      this.startTime = startTime;
      this.lifeTime = lifeTime;
    }
  }
}