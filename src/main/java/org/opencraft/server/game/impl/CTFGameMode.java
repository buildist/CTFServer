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

import com.google.common.collect.ImmutableList;
import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.WebServer;
import org.opencraft.server.cmd.impl.DefuseCommand;
import org.opencraft.server.cmd.impl.DefuseTNTCommand;
import org.opencraft.server.cmd.impl.FlagDropCommand;
import org.opencraft.server.cmd.impl.FlamethrowerCommand;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.model.*;
import org.opencraft.server.model.BlockLog.BlockInfo;

import java.util.ArrayList;

import tf.jacobsc.ctf.server.StalemateKt;
import tf.jacobsc.ctf.server.StatsKt;
import tf.jacobsc.utils.RatingKt;

public class CTFGameMode extends GameMode {

  public int redFlagX;
  public int redFlagY;
  public int redFlagZ;
  public int blueFlagX;
  public int blueFlagY;
  public int blueFlagZ;
  public boolean redFlagDropped = false;
  public boolean blueFlagDropped = false;
  public Thread redFlagDroppedThread;
  public Thread blueFlagDroppedThread;
  public static int redCaptures;
  public static int blueCaptures;
  public boolean redFlagTaken = false;
  public boolean blueFlagTaken = false;

  private boolean stalemateTags;
  private boolean suddenDeath;

  private Thread antiStalemateThread = null;


  public CTFGameMode() {
    super();
    registerCommand("d", DefuseCommand.getCommand());
    registerCommand("defuse", DefuseCommand.getCommand());
    registerCommand("defusetnt", DefuseTNTCommand.getCommand());
    registerCommand("dt", DefuseTNTCommand.getCommand());
    registerCommand("f", FlamethrowerCommand.getCommand());
    registerCommand("fd", FlagDropCommand.getCommand());
  }

  @Override
  public void playerConnected(final Player player) {
    super.playerConnected(player);
    String welcome = Configuration.getConfiguration().getWelcomeMessage();
    if (!player.isNewPlayer) {
      if (welcome != null && !welcome.equals("null")) {
        player.getActionSender().sendChatMessage("&a" + welcome);
      }
      player
          .getActionSender()
          .sendChatMessage("&bSay /join to start playing, or /spec to " + "spectate");
      if (getMode() == Level.CTF) {
        player.getActionSender().sendChatMessage("&aSay /help to learn how to play");
      } else {
        player
            .getActionSender()
            .sendChatMessage(
                "&aThis is a Team Deathmatch map. Say /help to " + "learn how to play");
      }
      player.getActionSender().sendChatMessage("&aSay /rules to read the rules");
    } else {
      String helpText;
      if (getMode() == Level.CTF) {
        player
            .getActionSender()
            .sendChatMessage("&bWelcome to Capture the Flag! Here's how you " + "play.");
        helpText = Constants.HELP_TEXT;
      } else {
        player
            .getActionSender()
            .sendChatMessage("&bWelcome to Team Deathmatch! Here's how you " + "play.");
        helpText = Constants.TDM_HELP_TEXT;
      }
      player.getActionSender().sendChatMessage("&e" + helpText);
      player
          .getActionSender()
          .sendChatMessage(
              "&bSay /join to start playing or /spec to spectate"
                  + ". /help will show these instructions again.");
    }
  }

  public void explodeTNT(
      Player p,
      Level level,
      int x,
      int y,
      int z,
      int r,
      boolean lethal,
      boolean tk,
      boolean deleteSelf,
      String type) {
    if (deleteSelf) {
      level.setBlock(x, y, z, 0);
    }

    // Big TNT should not be triggered by rockets and grenades
    if (type == null) {
      if (p.tntRadius == GameSettings.getInt("BigTNTRadius")) {
        p.bigTNTRemaining--;
      }

      if (p.bigTNTRemaining <= 0 && p.tntRadius == GameSettings.getInt("BigTNTRadius")) {
        p.tntRadius = 2;
        p.getActionSender().sendChatMessage("- &eYour big TNT has expired!");
      }
    }

    ArrayList<Player> killed = new ArrayList<>();
    if (lethal) {
      float px = x + 0.5f, py = y + 0.5f, pz = z + 0.5f;
      float pr = r + 0.5f;
      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        float tx = (t.getPosition().getX()) / 32f;
        float ty = (t.getPosition().getY()) / 32f;
        float tz = (t.getPosition().getZ()) / 32f;
        if (Math.abs(px - tx) < pr
            && Math.abs(py - ty) < pr
            && Math.abs(pz - tz) < pr
            && (p.team != t.team || (tk && (t == p || !t.hasFlag)))
            && !t.isSafe()
            && p.canKill(t, true)
            && !t.isHidden) {
          t.markSafe();
          killed.add(t);
          p.gotKill(t);
          t.sendToTeamSpawn();
          t.died(p);
          updateKillFeed(
              p,
              t,
              p.parseName()
                  + " exploded "
                  + t.getColoredName()
                  + (type == null ? "" : " &f(" + type + ")"));
          if (!tk) {
            checkFirstBlood(p, t);
          }
          if (t.team != -1 && t.team != p.team) {
            p.incIntAttribute("explodes");
            p.addPoints(5);
          }
          if (t.hasFlag) {
            dropFlag(t.team);
          }
        }
      }
    }

    ImmutableList.Builder<BlockChange> blockChanges = ImmutableList.builder();
    for (int cx = x - r; cx <= x + r; cx++) {
      for (int cy = y - r; cy <= y + r; cy++) {
        for (int cz = z - r; cz <= z + r; cz++) {
          if (!isSolidBlock(level, cx, cy, cz)) {
            blockChanges.add(new BlockChange(cx, cy, cz, 0));
          }
          defuseMineIfCan(p, cx, cy, cz);
        }
      }
    }
    World.getWorld().getLevel().setBlocks(blockChanges.build());

    if (killed.size() == 2) {
      World.getWorld().broadcast("- " + p.parseName() + " &egot a &bDouble Kill");
    } else if (killed.size() == 3) {
      World.getWorld().broadcast("- " + p.parseName() + " &egot a &bTriple Kill");
    } else if (killed.size() > 3) {
      World.getWorld().broadcast("- " + p.parseName() + " &egot a &b" + killed.size() + "x Kill");
      for (Player t : killed) {
        // Brodcast multi kills greater than 3 here because they won't all show up
        // in the kill feed.
        World.getWorld()
            .broadcast(
                "- "
                    + p.parseName()
                    + " exploded "
                    + t.getColoredName()
                    + (type == null ? "" : " &f(" + type + ")"));
      }
    }

    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      int ex = x  * 32 + 16;
      int ez = y  * 32 + 16;
      int ey = z  * 32 + 16;

      // If the player does not want to see particles, don't show them
      if (!player.ignorePlayers.contains("-particles")) player.getActionSender().sendSpawnEffect(Constants.EFFECT_TNT, ex, ey, ez, ex, ey, ez);
      if (!player.ignorePlayers.contains("-particles")) player.getActionSender().sendSpawnEffect(Constants.EFFECT_TNT_2, ex, ey, ez, ex, ey, ez);
    }
  }

  @Override
  public boolean isSolidBlock(Level level, int x, int y, int z) {
    int oldBlock = level.getBlock(x, y, z);
    return level.isSolid(x, y, z)
        || oldBlock == Constants.BLOCK_TNT_RED || oldBlock == Constants.BLOCK_TNT_BLUE
        || oldBlock == Constants.BLOCK_INVISIBLE
        || (x == blueFlagX && z == blueFlagY && y == blueFlagZ)
        || (x == redFlagX && z == redFlagY && y == redFlagZ)
        || isMine(x, y, z);
  }

  private void defuseMineIfCan(Player p, int x, int y, int z) {
    if (isMine(x, y, z)) {
      Mine m = World.getWorld().getMine(x, y, z);
      if (m == null) { // Shouldn't get here, but whatever. Just in case.
        return;
      }
      if (m.team != p.team) {
        World.getWorld().removeMine(m);
        World.getWorld().getLevel().setBlock((m.x - 16) / 32, (m.y - 16) / 32, (m.z - 16) / 32, 0);
        m.owner.removeMine(m);
        World.getWorld()
            .broadcast("- " + p.parseName() + " defused " + m.owner.parseName() + "'s mine!");
      }
    }
  }

  public void explodeTNT(Player p, Level level, int x, int y, int z, int r) {
    explodeTNT(p, level, x, y, z, r, true, false, true, null);
  }

  public void processFlamethrower(Player p, Position pos, Rotation r) {
    pos = pos.toBlockPos();
    double heading =
        Math.toRadians((int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256) - 90));
    double pitch =
        Math.toRadians((int) (360 - Server.getUnsigned(r.getLook()) * ((float) 360 / 256)));

    int distance = GameSettings.getInt("FlameThrowerStartDistanceFromPlayer");
    int length = GameSettings.getInt("FlameThrowerLength");
    int side = Integer.signum(length);
    int dir = Integer.signum(distance);

    double px = (pos.getX());
    double py = (pos.getY());
    double pz = (pos.getZ()) - 1;

    double vx = Math.cos(heading) * Math.cos(pitch);
    double vy = Math.sin(heading) * Math.cos(pitch);
    double vz = Math.sin(pitch);
    double x = px;
    double y = py;
    double z = pz;
    for (int i = 0; i < Math.abs(length) + Math.abs(distance); i++) {
      int bx = (int) Math.round(x);
      int by = (int) Math.round(y);
      int bz = (int) Math.round(z);

      // Check to make sure we are not above the build height.
      if (bz > World.getWorld().getLevel().ceiling) {
        return;
      }

      int oldBlock = World.getWorld().getLevel().getBlock(bx, by, bz);

      // Defuse mine if it's there
      defuseMineIfCan(p, bx, by, bz);
      // Can't go through sand, glass, obsidian, water, or non explodable blocks
      if (oldBlock == BlockConstants.WATER
          || oldBlock == BlockConstants.STILL_WATER
          || oldBlock == BlockConstants.SAND
          || oldBlock == BlockConstants.GLASS
          || oldBlock == BlockConstants.OBSIDIAN
          || isSolidBlock(World.getWorld().getLevel(), bx, by, bz)) {
        return;
      }

      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        Position blockPos = t.getPosition().toBlockPos();
        if (blockPos.getX() == bx
            && blockPos.getY() == by
            && (blockPos.getZ() == bz + 1 || blockPos.getZ() == bz)
            && (p.team != t.team)
            && !t.isSafe()
            && p.canKill(t, false)) {
          p.gotKill(t);
          t.sendToTeamSpawn();
          t.markSafe();
          t.died(p);
          updateKillFeed(p, t, p.parseName() + " cooked " + t.getColoredName());
          checkFirstBlood(p, t);
          p.addPoints(5);
          if (t.hasFlag) {
            dropFlag(t.team);
          }
        }
      }

      if (i < Math.abs(distance)) {
        x += vx * dir;
        y += vy * dir;
        z += vz * dir;
      } else {
        x += vx * side;
        y += vy * side;
        z += vz * side;
      }
    }
  }

  public void showScore() {
    if (getMode() == Level.TDM) {
      World.getWorld()
          .broadcast(
              "- Current score: Red has "
                  + redCaptures
                  + " kills; blue has "
                  + blueCaptures
                  + " kills");

    } else if (getMode() == Level.CTF) {
      World.getWorld()
          .broadcast(
              "- Current score: Red has "
                  + redCaptures
                  + " captures; blue has"
                  + " "
                  + blueCaptures
                  + " captures");
    }
  }

  public int getRedCaptures() {
    return redCaptures;
  }

  public int getBlueCaptures() {
    return blueCaptures;
  }

  public void placeRedFlag() {
    if (getMode() == Level.CTF) {
      World.getWorld().getLevel().setBlock(redFlagX, redFlagZ, redFlagY, Constants.BLOCK_RED_FLAG);
      World.getWorld().getLevel().clearSolidBlock(redFlagX, redFlagZ, redFlagY);
    }
  }

  public void placeBlueFlag() {
    if (getMode() == Level.CTF) {
      World.getWorld()
          .getLevel()
          .setBlock(blueFlagX, blueFlagZ, blueFlagY, Constants.BLOCK_BLUE_FLAG);
      World.getWorld().getLevel().clearSolidBlock(blueFlagX, blueFlagZ, blueFlagY);
    }
  }

  public void setRedFlagPos(int x, int y, int z) {
    redFlagX = x;
    redFlagY = y;
    redFlagZ = z;
  }

  public void setBlueFlagPos(int x, int y, int z) {
    blueFlagX = x;
    blueFlagY = y;
    blueFlagZ = z;
  }

  public void resetRedFlagPos() {
    if (getMode() == Level.CTF) {
      if (map.props.getProperty("redFlagPosition") != null) {
        String[] position = map.props.getProperty("redFlagPosition")
            .replace(" ", "")
            .split(",");
        redFlagX = Integer.parseInt(position[0]);
        redFlagY = Integer.parseInt(position[1]);
        redFlagZ = Integer.parseInt(position[2]);
      } else {
        redFlagX = Integer.parseInt(map.props.getProperty("redFlagX"));
        redFlagY = Integer.parseInt(map.props.getProperty("redFlagY"));
        redFlagZ = Integer.parseInt(map.props.getProperty("redFlagZ"));
      }
      redFlagDropped = false;
    }
  }

  public void resetBlueFlagPos() {
    if (getMode() == Level.CTF) {
      if (map.props.getProperty("blueFlagPosition") != null) {
        String[] position = map.props.getProperty("blueFlagPosition")
            .replace(" ", "")
            .split(",");
        blueFlagX = Integer.parseInt(position[0]);
        blueFlagY = Integer.parseInt(position[1]);
        blueFlagZ = Integer.parseInt(position[2]);
      } else {
        blueFlagX = Integer.parseInt(map.props.getProperty("blueFlagX"));
        blueFlagY = Integer.parseInt(map.props.getProperty("blueFlagY"));
        blueFlagZ = Integer.parseInt(map.props.getProperty("blueFlagZ"));
      }
      blueFlagDropped = false;
    }
  }

  protected void resetGameMode() {
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      player.hasFlag = false;
      player.hasTNT = false;
      player.flamethrowerFuel = Constants.FLAME_THROWER_FUEL;
      player.currentRoundPointsEarned = 0;
      player.setPoints(GameSettings.getInt("InitialPoints"));
      unblockSpawnZones(player);
    }

    redFlagTaken = false;
    blueFlagTaken = false;
    suddenDeath = false;
    stalemateTags = false;
    redCaptures = 0;
    blueCaptures = 0;

    resetRedFlagPos();
    resetBlueFlagPos();

    placeBlueFlag();
    placeRedFlag();

    openSpawns();
    if (World.getWorld().getLevel().invisibleRoof) addRoof();
  }

  private void updateKillFeed(Player attacker, Player defender, String killmsg) {
    synchronized (killFeed) {
      killFeed.add(0, new KillFeedItem(attacker, defender, 0, true, killmsg));
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        if (p == attacker || p == defender) {
          sendAnnouncement(p, killmsg);
        }
      }

      if (killFeed.size() > 3) {
        killFeed.remove(killFeed.get(killFeed.size() - 1));
      }

      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        sendKillFeed(p);
      }
    }

    WebServer.killFeed = killFeed;
  }

  private void openSpawns() {
    if (getMode() == Level.CTF) {
      Level map = World.getWorld().getLevel();
      Position blueSpawn = map.blueSpawnPosition.toBlockPos();
      Position redSpawn = map.redSpawnPosition.toBlockPos();
      int bDoorX = blueSpawn.getX();
      int bDoorY = blueSpawn.getY();
      int bDoorZ = blueSpawn.getZ() - 2;
      int rDoorX = redSpawn.getX();
      int rDoorY = redSpawn.getY();
      int rDoorZ = redSpawn.getZ() - 2;
      map.setBlock(rDoorX, rDoorY, rDoorZ, 0);
      map.setBlock(bDoorX, bDoorY, bDoorZ, 0);
    }
  }

  private void addRoof() {
    if (getMode() == Level.CTF) {
      Level map = World.getWorld().getLevel();

      for (int i = 0; i < map.width; i++) {
        for (int j = 0; j < map.height; j++) { // For some reason map.height is the map's length...?
          if (World.getWorld().getLevel().getBlock(i, j, map.ceiling + 1) != 0) continue; // Only replace air
          map.setBlock(i, j, map.ceiling + 1, 255); // 255 = invisible block
        }
      }
    }
  }

  public void endGame() {
    new Thread(
        new Runnable() {
          public void run() {
            try {
              String winner = null;
              int winnerID = -2;
              if (redCaptures > blueCaptures) {
                winner = "red";
                winnerID = 0;
              } else if (blueCaptures > redCaptures) {
                winner = "blue";
                winnerID = 1;
              }
              if (winner == null) {
                World.getWorld().broadcast("- &6The game ended in a tie!");
              } else {
                World.getWorld()
                    .broadcast("- &6The game has ended; the " + winner + " team wins!");
              }
              if (getMode() == Level.CTF) {
                World.getWorld()
                    .broadcast(
                        "- &6Red had "
                            + redCaptures
                            + " captures, blue had "
                            + blueCaptures
                            + ".");
              } else {
                World.getWorld()
                    .broadcast(
                        "- &6Red had "
                            + redCaptures
                            + " kills, blue had "
                            + blueCaptures
                            + ".");
              }
              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                if (p.team != -1) {
                  p.incIntAttribute("games");
                }
                if (p.team == winnerID) {
                  p.incIntAttribute("wins");
                }
                p.hasVoted = false;
                p.hasNominated = false;
              }
              Player[] top = getTopPlayers(3);
              World.getWorld().broadcast("- &3Top players: (&aKills&3/&cDeaths&3/&eCaps&3)");

              if (top[0] == null) {
                World.getWorld().broadcast("- &3Nobody");
              }

              for (int j = 0; j < 3; j++) {
                Player p = top[j];
                if (p == null) {
                  break;
                }

                World.getWorld()
                    .broadcast("- " + (j + 1) + ". &2" + p.getName() + " - " + p.currentRoundPointsEarned + " (&a" + p.kills + "&2/&c" + p.deaths + "&2/&e" + p.captures + "&2)");
              }

              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                int placement = getPlayerPlacement(p);
                if (placement == -1) {
                  p.getActionSender().sendChatMessage("- &eYou did not get any points this game.");
                  continue;
                }

                if (placement <= 3) continue;

                p.getActionSender().sendChatMessage("- " + (placement) + ". &2" + p.getName() + " - " + p.currentRoundPointsEarned + " (&a" + p.kills + "&2/&c" + p.deaths + "&2/&e" + p.captures + "&2)");
              }

              if (winnerID >= 0) {
                if (GameSettings.getBoolean("Tournament")) {
                  // If you ever change this so that ties are rated
                  // the rating system needs to have a draw probability > 0
                  RatingKt.rateTeamMatch(winnerID);
                } {
                  RatingKt.rateCasualMatch(winnerID);
                }
              }

              for (Player player : World.getWorld().getPlayerList().getPlayers()) {
                player.team = -1;
                player.hasFlag = false;
                player.hasTNT = false;
                player.isCreepering = false;
                player.kills = 0;
                player.deaths = 0;
                player.captures = 0;
                if (player.isFlamethrowerEnabled()) {
                  World.getWorld()
                      .getLevel()
                      .clearFire(player, player.linePosition, player.lineRotation);
                  player.disableFlameThrower();
                }
                player.flamethrowerTime = 0;
                player.rocketTime = 0;
                unblockSpawnZones(player);
                player.sendToTeamSpawn();
              }
              rtvVotes = 0;
              rtvYesPlayers.clear();
              rtvNoPlayers.clear();

              new Thread(() -> StatsKt.savePlayerStats(World.getWorld())).start();
              if (GameSettings.getBoolean("Tournament")) {
                return;
              }

              // Start voting
              Level active = World.getWorld().getLevel(); // We'll retrieve this information later to cancel sending players to new level if /newgame was called
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
              Thread.sleep(40 * 1000);

              // Check if level has been changed with /newgame, if so, don't bother changing levels
              if (active != World.getWorld().getLevel()) {
                //World.getWorld().broadcast("- &3Voting cancelled due to /newgame");
                voting = false;
                return;
              }
              // Setup next level
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

  public void playerChangedTeam(Player player) {
    if (player.hasFlag) {
      if (player.team == 0) {
        blueFlagTaken = false;
        placeBlueFlag();
      } else {
        redFlagTaken = false;
        placeRedFlag();
      }
      player.hasFlag = false;
      unblockSpawnZones(player);
      World.getWorld().broadcast("- " + player.parseName() + " dropped the flag!");
    }
  }

  public void checkForStalemate() {
    boolean stalemate = redFlagTaken && blueFlagTaken;

    if (stalemate) {
      World.getWorld().broadcast("- &eAnti-stalemate mode activated!");
      if (GameSettings.getBoolean("AntiStalemate")) {
        if (antiStalemateThread == null || !antiStalemateThread.isAlive()) {
          antiStalemateThread = StalemateKt.staleMateThread(World.getWorld(), GameSettings.getInt("AntiStalemateTime"));
        }
      }

      if (suddenDeath || GameSettings.getBoolean("StalemateTags")) {
        World.getWorld().broadcast("- &eIf your teammate gets tagged you'll drop the flag");
        stalemateTags = true;
      }
    }
  }

  public void blockSpawnZones(Player p) {
    if (!p.hasFlag) return;
    // TODO: TEN_BIT_BLOCKS support

    /*Level level = World.getWorld().getLevel();

    if (level.redSpawnZoneMin == null || level.redSpawnZoneMax == null) return;
    if (level.blueSpawnZoneMin == null || level.blueSpawnZoneMax == null) return;

    int minX = 0;
    int minZ = 0;
    int minY = 0;

    int maxX = 0;
    int maxZ = 0;
    int maxY = 0;

    if (p.team == 0) {
      // Red
      minX = level.redSpawnZoneMin.getX() - 32;
      minZ = level.redSpawnZoneMin.getZ();
      minY = level.redSpawnZoneMin.getY() - 32;

      maxX = level.redSpawnZoneMax.getX() + 32;
      maxZ = level.redSpawnZoneMax.getZ();
      maxY = level.redSpawnZoneMax.getY() + 32;
    } else if (p.team == 1) {
      // Blue
      minX = level.blueSpawnZoneMin.getX() - 32;
      minZ = level.blueSpawnZoneMin.getZ();
      minY = level.blueSpawnZoneMin.getY() - 32;

      maxX = level.blueSpawnZoneMax.getX() + 32;
      maxZ = level.blueSpawnZoneMax.getZ();
      maxY = level.blueSpawnZoneMax.getY() + 32;
    }

    List<Integer> indices = new ArrayList<>();
    List<Short> blocks = new ArrayList<>();

    // Populate indices and blocks lists with the block updates to be performed
    for (int x = minX; x < maxX; x++) {
      for (int z = minZ; z < maxZ; z++) {
        for (int y = minY; y < maxY; y++) {
          if (World.getWorld().getLevel().getBlock(x, y, z) != 0) continue; // Only replace air

          int index = ((y & 0xFF) << 16) | ((z & 0xFF) << 8) | (x & 0xFF); // Calculate the index based on x, y, and z coordinates
          indices.add(index); // Add the calculated index to the indices list
          blocks.add((short) Constants.BLOCK_INVISIBLE); // Replace with crates
        }
      }
    }

    p.getSession().getActionSender().sendBulkBlockUpdate(indices, blocks); // Send bulk block updates*/
  }

  public void unblockSpawnZones(Player p) {
    // TODO: TEN_BIT_BLOCKS support
    /*Level level = World.getWorld().getLevel();

    if (level.redSpawnZoneMin == null || level.redSpawnZoneMax == null) return;
    if (level.blueSpawnZoneMin == null || level.blueSpawnZoneMax == null) return;

    int minX = 0;
    int minZ = 0;
    int minY = 0;

    int maxX = 0;
    int maxZ = 0;
    int maxY = 0;

    if (p.team == 0) {
      // Red
      minX = level.redSpawnZoneMin.getX() - 32;
      minZ = level.redSpawnZoneMin.getZ();
      minY = level.redSpawnZoneMin.getY() - 32;

      maxX = level.redSpawnZoneMax.getX() + 32;
      maxZ = level.redSpawnZoneMax.getZ();
      maxY = level.redSpawnZoneMax.getY() + 32;
    } else if (p.team == 1) {
      // Blue
      minX = level.blueSpawnZoneMin.getX() - 32;
      minZ = level.blueSpawnZoneMin.getZ();
      minY = level.blueSpawnZoneMin.getY() - 32;

      maxX = level.blueSpawnZoneMax.getX() + 32;
      maxZ = level.blueSpawnZoneMax.getZ();
      maxY = level.blueSpawnZoneMax.getY() + 32;
    }

    List<Integer> indices = new ArrayList<>();
    List<Short> blocks = new ArrayList<>();

    // Populate indices and blocks lists with the block updates to be performed
    for (int x = minX; x < maxX; x++) {
      for (int z = minZ; z < maxZ; z++) {
        for (int y = minY; y < maxY; y++) {
          if (World.getWorld().getLevel().getBlock(x, y, z) != Constants.BLOCK_INVISIBLE) continue; // Only replace crates

          int index = ((y & 0xFF) << 16) | ((z & 0xFF) << 8) | (x & 0xFF); // Calculate the index based on x, y, and z coordinates
          //x + z * width + y * width * length
          indices.add(index); // Add the calculated index to the indices list
          blocks.add((short) 0); // Replace with air
        }
      }
    }

    p.getSession().getActionSender().sendBulkBlockUpdate(indices, blocks); // Send bulk block updates*/
  }

  public void dropFlag(int team) {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team == team) {
        dropFlag(p);
      }
    }
  }

  public void dropFlag(Player p) {
    if (antiStalemateThread != null && antiStalemateThread.isAlive()) {
      antiStalemateThread.interrupt();
    }
    dropFlag(p, false, false);
    stalemateTags = false;
  }

  public void returnDroppedBlueFlag() {
    if (blueFlagDropped) {
      World.getWorld().getLevel().setBlock(blueFlagX, blueFlagZ, blueFlagY, 0);
      resetBlueFlagPos();
      placeBlueFlag();
      World.getWorld().broadcast("- &eThe blue flag has been returned!");
    }
  }

  // Should only be used when the flag is not taken by a player
  public void returnDroppedRedFlag() {
    if (redFlagDropped) {
      World.getWorld().getLevel().setBlock(redFlagX, redFlagZ, redFlagY, 0);
      resetRedFlagPos();
      placeRedFlag();
      World.getWorld().broadcast("- &eThe red flag has been returned!");
    }
  }

  public void dropFlag(Player p, final boolean instant, final boolean isVoluntary) {
    if (p.hasFlag) {
      p.hasFlag = false;
      unblockSpawnZones(p);
      World.getWorld().broadcast("- " + p.parseName() + " dropped the flag!");
      sendAnnouncement(p.parseName() + " dropped the flag!");
      Position playerPos = p.getPosition().toBlockPos();
      final boolean _antiStalemate = redFlagTaken && blueFlagTaken;
      if (p.team == 0) {
        blueFlagTaken = false;
        blueFlagDropped = true;

        // If the player is above the build ceiling, drop the flag at the build ceiling to prevent out of bounds issues
        if (playerPos.getZ() > World.getWorld().getLevel().ceiling) setBlueFlagPos(playerPos.getX(), World.getWorld().getLevel().ceiling, playerPos.getY());
        else setBlueFlagPos(playerPos.getX(), playerPos.getZ() - 1, playerPos.getY());

        if (blueFlagDroppedThread != null)  {
          blueFlagDroppedThread.interrupt();
        }

        if (instant) {
          returnDroppedBlueFlag();
        } else {
          blueFlagDroppedThread =
              new Thread(
                  () -> {
                    try {
                      if (!_antiStalemate || isVoluntary) {
                        Thread.sleep(10 * 1000);
                      }
                      returnDroppedBlueFlag();
                    } catch (InterruptedException ex) {
                    }
                  });
          placeBlueFlag();
          blueFlagDroppedThread.start();
        }
      } else {
        redFlagTaken = false;
        redFlagDropped = true;

        // If the player is above the build ceiling, drop the flag at the build ceiling to prevent out of bounds issues
        if (playerPos.getZ() > World.getWorld().getLevel().ceiling) setRedFlagPos(playerPos.getX(), World.getWorld().getLevel().ceiling, playerPos.getY());
        else setRedFlagPos(playerPos.getX(), playerPos.getZ() - 1, playerPos.getY());

        if (redFlagDroppedThread != null && redFlagDroppedThread.isAlive())  {
          redFlagDroppedThread.interrupt();
        }

        if (instant) {
          returnDroppedRedFlag();
        } else {
          redFlagDroppedThread =
              new Thread(
                  () -> {
                    try {
                      if (!_antiStalemate || isVoluntary) {
                        Thread.sleep(10 * 1000);
                      }
                      returnDroppedRedFlag();
                    } catch (InterruptedException ex) {
                    }
                  });
          placeRedFlag();
          redFlagDroppedThread.start();
        }
      }
    }
  }

  public void processBlockRemove(Player p, int x, int z, int y) {
    if (x == redFlagX && y == redFlagY && z == redFlagZ) {
      if (p.team == 1) {
        if (!redFlagTaken) {
          // red flag taken
          if (getRedPlayers() == 0 || getBluePlayers() == 0) {
            placeRedFlag();
            p.getActionSender()
                .sendChatMessage("- &eFlag can't be captured when one team has 0 " + "people");
          } else if (p.duelPlayer != null) {
            placeRedFlag();
            p.getActionSender().sendChatMessage("- &eYou can't take the flag while dueling");
          } else {
            World.getWorld().broadcast("- &eRed flag taken by " + p.parseName() + "!");
            sendAnnouncement("&eRed flag taken by " + p.parseName() + "!");
            p.getActionSender()
                .sendChatMessage(
                    "- &eClick your own flag to capture, or use /fd "
                        + "to drop the flag and pass to a teammate");
            p.hasFlag = true;
            redFlagTaken = true;
            blockSpawnZones(p);
            checkForStalemate();
            resetRedFlagPos();
            if (redFlagDroppedThread != null) {
              redFlagDroppedThread.interrupt();
            }
          }
        }
      } else {
        // blue flag returned
        if (p.hasFlag && !redFlagTaken && !redFlagDropped) {
          World.getWorld()
              .broadcast("- &eBlue flag captured by " + p.parseName() + " for the red" + " team!");
          sendAnnouncement("&eBlue flag captured by " + p.parseName() + "!");
          redCaptures++;
          p.captures++;
          p.hasFlag = false;
          blueFlagTaken = false;
          unblockSpawnZones(p);
          placeBlueFlag();
          p.incIntAttribute("captures");
          p.addPoints(40);
          if (redCaptures == GameSettings.getInt("MaxCaptures") || suddenDeath) {
            nominatedMaps.clear();
            endGame();
          } else {
            showScore();
          }
        }
        if (!redFlagTaken) {
          placeRedFlag();
        }
      }
    }
    if (x == blueFlagX && y == blueFlagY && z == blueFlagZ) {
      if (p.team == 0) {
        if (!blueFlagTaken) {
          // blue flag taken
          if (getRedPlayers() == 0 || getBluePlayers() == 0) {
            placeBlueFlag();
            p.getActionSender()
                .sendChatMessage("- &eFlag can't be captured when one team has 0 " + "people");
          } else if (p.duelPlayer != null) {
            placeBlueFlag();
            p.getActionSender().sendChatMessage("- &eYou can't take the flag while dueling");
          } else {
            World.getWorld().broadcast("- &eBlue flag taken by " + p.parseName() + "!");
            sendAnnouncement("&eBlue flag taken by " + p.parseName() + "!");
            p.getActionSender()
                .sendChatMessage(
                    "- &eClick your own flag to capture, or use /fd "
                        + "to drop the flag and pass to a teammate,");
            p.hasFlag = true;
            blueFlagTaken = true;
            blockSpawnZones(p);
            checkForStalemate();
            resetBlueFlagPos();
            if (blueFlagDroppedThread != null) {
              blueFlagDroppedThread.interrupt();
            }
          }
        }
      } else {
        // red flag returned
        if (p.hasFlag && !blueFlagTaken && !blueFlagDropped) {
          World.getWorld()
              .broadcast("- &eRed flag captured by " + p.parseName() + " for the blue" + " team!");
          sendAnnouncement("&eRed flag captured by " + p.parseName() + "!");
          blueCaptures++;
          p.captures++;
          p.hasFlag = false;
          redFlagTaken = false;
          unblockSpawnZones(p);
          placeRedFlag();
          p.incIntAttribute("captures");
          p.addPoints(40);
          if (blueCaptures == GameSettings.getInt("MaxCaptures") || suddenDeath) {
            nominatedMaps.clear();
            endGame();
          } else {
            showScore();
          }
        }
        if (!blueFlagTaken) {
          placeBlueFlag();
        }
      }
    }
  }

  public void processPlayerMove(Player p) {
    Position pos = p.getPosition();
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();

    if (p.team != -1) {
      for (Mine m : World.getWorld().getAllMines()) {
        int mx = (m.x - 16) / 32;
        int my = (m.y - 16) / 32;
        int mz = (m.z - 16) / 32;

        if (m.active
            && (p.duelPlayer == null || p.duelPlayer == m.owner)
            && (m.owner.duelPlayer == null || m.owner.duelPlayer == p)
            && p.team != -1
            && m.team != -1
            && p.team != m.team
        ) {
          // Only get the MineRadius value if all requirements above have been satisfied
          float radius = 32 + (GameSettings.getFloat("MineRadius") * 32); // We add 32 because we don't include the mine itself for its radius

          if (m.x > x - radius
              && m.x < x + radius
              && m.y > y - radius
              && m.y < y + radius
              && m.z > z - radius
              && m.z < z + radius) {
            Level level = World.getWorld().getLevel();
            level.setBlock(mx, my, mz, 0);

            int r = 1;
            for (int cx = mx - r; cx <= mx + r; cx++) {
              for (int cy = my - r; cy <= my + r; cy++) {
                for (int cz = mz - r; cz <= mz + r; cz++) {
                  int oldBlock = level.getBlock(cx, cy, cz);
                  if (!level.isSolid(cx, cy, cz)
                      && oldBlock != Constants.BLOCK_TNT_RED
                      && oldBlock != Constants.BLOCK_TNT_BLUE
                      && !(cx == blueFlagX && cz == blueFlagY && cy == blueFlagZ)
                      && !(cx == redFlagX && cz == redFlagY && cy == redFlagZ)) {
                    level.setBlock(cx, cy, cz, 0);
                  }
                }
              }
            }
            m.owner.gotKill(p);
            p.sendToTeamSpawn();
            checkFirstBlood(m.owner, p);
            m.owner.incIntAttribute("mines");
            m.owner.removeMine(m);
            World.getWorld().removeMine(m);
            if (p.hasFlag) {
              dropFlag(p.team);
            }
            p.died(m.owner);
            updateKillFeed(m.owner, p, m.owner.parseName() + " mined " + p.parseName() + ".");
            m.owner.addPoints(GameSettings.getInt("MinePoints"));
          }
        }
      }
    }
    if (getMode() == Level.CTF && tournamentGameStarted) {
      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        Position op = t.getPosition();
        // Blocks are 32 "chunks" on a side... I think. Don't quote me on that.
        // 49 blocks in each direction except up because you have to account for feet
        // 49 is enough to not be able to tag through walls, while being as nice as possible
        // to laggy players. 57 is just enough in the z direction to not be able to tag through
        // 2 up jumping into roof.
        // These numbers were determined experimentally.
        if (op.getX() > x - 49
            && op.getX() < x + 49
            && op.getY() > y - 49
            && op.getY() < y + 49
            && op.getZ() > z - 57
            && op.getZ() < z + 57) {
          processTag(p, t, x, y, z);
        }
      }
    }
  }

  public void playerRespawn(Player player) {
    if (player.team > -1) {
      if (player.hasFlag) dropFlag(player, true, false);
    }
  }

  public void processTag(Player p1, Player p2, int x, int y, int z) {
    int t1 = p1.team;
    int t2 = p2.team;
    if (t1 != -1 && t2 != -1) {
      Player tagged = null;
      Player tagger = null;
      int x2 = Math.round((float) (x - 16) / 32);
      if ((x2 > map.divider && t1 == 0) || (x2 < map.divider && t1 == 1) && t2 != t1) {
        tagged = p1;
        tagger = p2;
      } else if ((x2 < map.divider && t1 == 0) || (x2 > map.divider && t1 == 1) && t2 != t1) {
        tagged = p2;
        tagger = p1;
      }
      if (t1 != t2
          && tagged != null
          && tagger != null
          && tagger.canKill(tagged, false)
          && !tagged.isSafe()) {
        tagger.gotKill(tagged);
        tagged.sendToTeamSpawn();
        tagged.markSafe();
        if (stalemateTags) {
          tagger.incStat("stalemateTags");
          dropFlag(tagged.team);
        }
        if (tagged.hasFlag) {
          dropFlag(tagged.team);
        }
        tagged.died(tagger);
        tagger.incIntAttribute("tags");
        tagger.addPoints(15);
        updateKillFeed(tagger, tagged, tagger.parseName() + " tagged " + tagged.parseName() + ".");
      }
    }
  }

  public boolean isTNT(int x, int y, int z) {
    for (Player t : World.getWorld().getPlayerList().getPlayers()) {
      if (t.tntX == x && t.tntY == y && t.tntZ == z) {
        return true;
      }
    }
    return false;
  }

  public boolean isMine(int x, int y, int z) {
    for(Mine m : World.getWorld().getAllMines()) {
      if ((m.x - 16) / 32 == x && (m.y - 16) / 32 == y && (m.z - 16) / 32 == z) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void setBlock(Player player, Level level, int x, int y, int z, int mode, int type) {
    int oldType = level.getBlock(x, y, z);
    int playerX = (player.getPosition().getX() - 16) / 32;
    int playerY = (player.getPosition().getY() - 16) / 32;
    int playerZ = (player.getPosition().getZ() - 16) / 32;
    int MAX_DISTANCE = 10;
    boolean ignore = false;
    boolean placedInSpawnZone = false;

    if (level.redSpawnZoneMin != null && level.redSpawnZoneMax != null) {
      int redMinX = level.redSpawnZoneMin.getX() / 32;
      int redMinZ = (level.redSpawnZoneMin.getZ() - 32) / 32;
      int redMinY = level.redSpawnZoneMin.getY() / 32;

      int redMaxX = level.redSpawnZoneMax.getX() / 32;
      int redMaxZ = (level.redSpawnZoneMax.getZ() - 32) / 32;
      int redMaxY = level.redSpawnZoneMax.getY() / 32;

      if ((x >= redMinX && x <= redMaxX) && (z >= redMinZ && z <= redMaxZ) && (y >= redMinY && y <= redMaxY)) placedInSpawnZone = true;
    }

    if (level.blueSpawnZoneMin != null && level.blueSpawnZoneMax != null) {
      int blueMinX = level.blueSpawnZoneMin.getX() / 32;
      int blueMinZ = (level.blueSpawnZoneMin.getZ() - 32) / 32;
      int blueMinY = level.blueSpawnZoneMin.getY() / 32;

      int blueMaxX = level.blueSpawnZoneMax.getX() / 32;
      int blueMaxZ = (level.blueSpawnZoneMax.getZ() - 32) / 32;
      int blueMaxY = level.blueSpawnZoneMax.getY() / 32;

      if ((x >= blueMinX && x <= blueMaxX) && (z >= blueMinZ && z <= blueMaxZ) && (y >= blueMinY && y <= blueMaxY)) placedInSpawnZone = true;
    }

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
      player.getActionSender().sendChatMessage("ID: " + level.getBlock(x, y, z) + "  Position: " + x + " " + z + " " + y);
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
      } else if (placedInSpawnZone) {
        // Allow detonator to explode last TNT, but revert the block change afterwards
        if (type == Constants.BLOCK_DETONATOR && mode == 1 && !ignore && player.hasTNT) {
          int radius = player.tntRadius;
          player.getActionSender().sendBlock(x, y, z, (short) oldType);
          explodeTNT(
              player, World.getWorld().getLevel(), player.tntX, player.tntY, player.tntZ, radius);
          player.hasTNT = false;
          player.tntX = 0;
          player.tntY = 0;
          player.tntZ = 0;
        } else {
          ignore = true;
          player.getActionSender().sendChatMessage("- &aYou may not place blocks at spawn.");
        }

        // Revert the block
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
      } else if (z > level.ceiling && !(type == Constants.BLOCK_DETONATOR && player.hasTNT)) {
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
      } else if (player.brush && type != Constants.BLOCK_TNT_RED && type != Constants.BLOCK_TNT_BLUE) {
        int height = 3;
        int radius = 3;

        for (int offsetZ = -height; offsetZ <= radius; offsetZ++) {
          for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
              if (level.getBlock(offsetX + x, offsetY + y, offsetZ + z) != 7
                  && !isTNT(offsetX + x, offsetY + y, offsetZ + z)
                  && !isMine(offsetX + x, offsetY + y, offsetZ + z)
                  && !(x + offsetX == redFlagX
                  && z + offsetZ == redFlagY
                  && y + offsetY == redFlagZ)
                  && !(x + offsetX == blueFlagX
                  && z + offsetZ == blueFlagY
                  && y + offsetY == blueFlagZ)
                  && Math.abs(offsetX) + Math.abs(offsetY) + Math.abs(offsetZ)
                  <= Math.abs(radius)) {
                level.setBlock(offsetX + x, offsetY + y, offsetZ + z, type);
              }
            }
          }
        }
      } else if (type == Constants.BLOCK_DETONATOR && mode == 1 && !ignore && player.hasTNT) {
        int radius = player.tntRadius;
        player.getActionSender().sendBlock(x, y, z, (short) oldType);
        explodeTNT(
            player, World.getWorld().getLevel(), player.tntX, player.tntY, player.tntZ, radius);
        player.hasTNT = false;
        player.tntX = 0;
        player.tntY = 0;
        player.tntZ = 0;
      } else if (level.isSolid(x, y, z)
          && (!player.isOp() || !player.placeSolid)
          && !GameSettings.getBoolean("Chaos")) {
        player.getActionSender().sendBlock(x, y, z, (short) level.getBlock(x, y, z));
      } else if (isTNT(x, y, z) && !ignore) { // Deleting tnt
        player.getActionSender().sendBlock(x, y, z, (short) Constants.BLOCK_TNT_RED); // TODO: Support for blue TNT
      } else if (isMine(x, y, z) && !ignore) { // Deleting mines
        player.getActionSender().sendBlock(x, y, z, (short) oldType);
      } else if ((type == Constants.BLOCK_TNT_RED || type == Constants.BLOCK_TNT_BLUE) && mode == 1 && !ignore) // Placing tnt
      {
        if (player.getIntAttribute("explodes") == 0) {
          player.getActionSender().sendChatMessage("- &bPlace a purple block to explode TNT.");
        }

        // Red player places blue TNT
        if (player.team == 0 && type == Constants.BLOCK_TNT_BLUE) {
          player.getActionSender().sendChatMessage("- &eYou are not allowed to place blue TNT!");
          player.getActionSender().sendBlock(x, y, z, (short) 0x00);
          return;
        }

        // Blue player places red TNT
        if (player.team == 1 && type == Constants.BLOCK_TNT_RED) {
          player.getActionSender().sendChatMessage("- &eYou are not allowed to place red TNT!");
          player.getActionSender().sendBlock(x, y, z, (short) 0x00);
          return;
        }

        if (player.team == -1) {
          player.getActionSender().sendChatMessage("- &eYou must join a team to place TNT!");
          player.getActionSender().sendBlock(x, y, z, (short) 0x00);
        } else {
          if (mode == 1) {
            if (!player.hasTNT
                && !(x == redFlagX && z == redFlagY && y == redFlagZ)
                && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
              player.hasTNT = true;
              player.tntX = x;
              player.tntY = y;
              player.tntZ = z;
              level.setBlock(x, y, z, type);
            } else if (!isTNT(x, y, z)
                && !(x == redFlagX && z == redFlagY && y == redFlagZ)
                && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
              player.getActionSender().sendBlock(x, y, z, (short) 0x00);
            } else if ((x == redFlagX && z == redFlagY && y == redFlagZ)
                || (x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
              player.getActionSender().sendBlock(x, y, z, (short) oldType);
            }
          }
        }
      } else if (type == Constants.BLOCK_FLAMETHROWER
          && mode == 1
          && !ignore) { // Toggle flamethrower
        player.toggleFlameThrower();
        player.getActionSender().sendBlock(x, y, z, (short) oldType);
      } else if (type == Constants.BLOCK_MINE && mode == 1 && !ignore) { // Placing mines
        if (player.team == -1) {
          player.getActionSender().sendChatMessage("- &eYou must join a team to place mines!");
          player.getActionSender().sendBlock(x, y, z, (short) 0x00);
        } else {
          if (player.mines.size() < GameSettings.getInt("MaxMines")
              && !(x == redFlagX && z == redFlagY && y == redFlagZ)
              && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
            final Mine mine = new Mine(x, y, z, player.team, player);
            player.mines.add(mine);
            player.getActionSender().sendChatMessage("- Say /d to defuse the mine.");
            level.setBlock(
                x, y, z, player.team == 0 ? Constants.BLOCK_MINE_RED : Constants.BLOCK_MINE_BLUE);
            World.getWorld().addMine(mine);
            new Thread(new MineActivator(mine, player)).start();
          } else if (!isMine(x, y, z)
              && !(x == redFlagX && z == redFlagY && y == redFlagZ)
              && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
            player.getActionSender().sendBlock(x, y, z, (short) 0x00);
          } else if ((x == redFlagX && z == redFlagY && y == redFlagZ)
              || (x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
            player.getActionSender().sendBlock(x, y, z, (short) oldType);
          }
        }
      } else if ((type == BlockConstants.LAVA
          || type == BlockConstants.WATER
          || type == BlockConstants.ADMINIUM
          || type == Constants.BLOCK_MINE_RED
          || type == Constants.BLOCK_MINE_BLUE)
          && !player.isOp()) {
        player.getActionSender().sendBlock(x, y, z, (short) 0);
        player.getActionSender().sendChatMessage("- &eYou can't place this block type!");
      } else if (getDropItem(x, y, z) != null) {
        DropItem i = getDropItem(x, y, z);
        i.pickUp(player);
      } else if ((x == redFlagX && z == redFlagY && y == redFlagZ)
          && mode == 1
          && !redFlagTaken
          && !ignore) {
        player.getActionSender().sendBlock(x, y, z, (short) Constants.BLOCK_RED_FLAG);
      } else if ((x == blueFlagX && z == blueFlagY && y == blueFlagZ)
          && mode == 1
          && !blueFlagTaken
          && !ignore) {
        player.getActionSender().sendBlock(x, y, z, (short) Constants.BLOCK_BLUE_FLAG);
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
      if (mode == 0 && !ignore) {
        processBlockRemove(player, x, y, z);
      }
    }
  }

  public void playerDisconnected(final Player p) {
    super.playerDisconnected(p);
    if (p.hasFlag) {
      dropFlag(p);
    }
    p.clearMines();
  }

  @Override
  public void step() {
    super.step();

    String setting = getMode() == Level.TDM ? "TDMTimeLimit" : "TimeLimit";
    int timeLimit = GameSettings.getInt(setting);
    if (timeLimit > 0) {
      long elapsedTime = System.currentTimeMillis() - gameStartTime;

      if (elapsedTime > timeLimit * 60 * 1000 && !suddenDeath) {
        if (getMode() == Level.CTF && redCaptures == blueCaptures) {
          World.getWorld().broadcast("- &eSudden death mode activated!");
          World.getWorld().broadcast("- &eThe next capture will win the game.");
          suddenDeath = true;
          checkForStalemate();
        } else {
          gameStartTime = System.currentTimeMillis();
          if (!voting) {
            endGame();
          }
        }
      }
    }
  }

  @Override
  public PlayerUI createPlayerUI(Player p) {
    return new CTFPlayerUI(this, p);
  }

  public static int getMode() {
    return World.getWorld().getLevel().mode;
  }
}