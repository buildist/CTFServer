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
import org.opencraft.server.WebServer;
import org.opencraft.server.cmd.impl.AddSpawnCommand;
import org.opencraft.server.cmd.impl.BanIPCommand;
import org.opencraft.server.cmd.impl.BlockInfoCommand;
import org.opencraft.server.cmd.impl.BlueCommand;
import org.opencraft.server.cmd.impl.BountyCommand;
import org.opencraft.server.cmd.impl.ChatCommand;
import org.opencraft.server.cmd.impl.ClientsCommand;
import org.opencraft.server.cmd.impl.DeOperatorCommand;
import org.opencraft.server.cmd.impl.DeVIPCommand;
import org.opencraft.server.cmd.impl.DefuseCommand;
import org.opencraft.server.cmd.impl.DefuseTNTCommand;
import org.opencraft.server.cmd.impl.DropCommand;
import org.opencraft.server.cmd.impl.DuelAcceptCommand;
import org.opencraft.server.cmd.impl.DuelCommand;
import org.opencraft.server.cmd.impl.EndCommand;
import org.opencraft.server.cmd.impl.FlagDropCommand;
import org.opencraft.server.cmd.impl.FlamethrowerCommand;
import org.opencraft.server.cmd.impl.FollowCommand;
import org.opencraft.server.cmd.impl.ForceCommand;
import org.opencraft.server.cmd.impl.FreezeCommand;
import org.opencraft.server.cmd.impl.GlobalChatCommand;
import org.opencraft.server.cmd.impl.HelpCommand;
import org.opencraft.server.cmd.impl.HelpOpCommand;
import org.opencraft.server.cmd.impl.HiddenCommand;
import org.opencraft.server.cmd.impl.HideCommand;
import org.opencraft.server.cmd.impl.IgnoreCommand;
import org.opencraft.server.cmd.impl.JoinCommand;
import org.opencraft.server.cmd.impl.KickCommand;
import org.opencraft.server.cmd.impl.LavaCommand;
import org.opencraft.server.cmd.impl.LogCommand;
import org.opencraft.server.cmd.impl.MapEnvironmentCommand;
import org.opencraft.server.cmd.impl.MapImportCommand;
import org.opencraft.server.cmd.impl.MapListCommand;
import org.opencraft.server.cmd.impl.MapSetCommand;
import org.opencraft.server.cmd.impl.MeCommand;
import org.opencraft.server.cmd.impl.MuteCommand;
import org.opencraft.server.cmd.impl.NewGameCommand;
import org.opencraft.server.cmd.impl.NoCommand;
import org.opencraft.server.cmd.impl.NominateCommand;
import org.opencraft.server.cmd.impl.NoteCommand;
import org.opencraft.server.cmd.impl.NotesCommand;
import org.opencraft.server.cmd.impl.OpChatCommand;
import org.opencraft.server.cmd.impl.OperatorCommand;
import org.opencraft.server.cmd.impl.PInfoCommand;
import org.opencraft.server.cmd.impl.PayCommand;
import org.opencraft.server.cmd.impl.PingCommand;
import org.opencraft.server.cmd.impl.PlayerCommand;
import org.opencraft.server.cmd.impl.PmCommand;
import org.opencraft.server.cmd.impl.PointsCommand;
import org.opencraft.server.cmd.impl.QuoteCommand;
import org.opencraft.server.cmd.impl.RTVCommand;
import org.opencraft.server.cmd.impl.RagequitCommand;
import org.opencraft.server.cmd.impl.RedCommand;
import org.opencraft.server.cmd.impl.ReloadCommand;
import org.opencraft.server.cmd.impl.RemoveSpawnCommand;
import org.opencraft.server.cmd.impl.RestartCommand;
import org.opencraft.server.cmd.impl.RulesCommand;
import org.opencraft.server.cmd.impl.SayCommand;
import org.opencraft.server.cmd.impl.SetCommand;
import org.opencraft.server.cmd.impl.SetPathCommand;
import org.opencraft.server.cmd.impl.SolidCommand;
import org.opencraft.server.cmd.impl.SpecCommand;
import org.opencraft.server.cmd.impl.StartCommand;
import org.opencraft.server.cmd.impl.StatsCommand;
import org.opencraft.server.cmd.impl.StatusCommand;
import org.opencraft.server.cmd.impl.StoreCommand;
import org.opencraft.server.cmd.impl.TeamCommand;
import org.opencraft.server.cmd.impl.TeleportCommand;
import org.opencraft.server.cmd.impl.TntCommand;
import org.opencraft.server.cmd.impl.TutorialCommand;
import org.opencraft.server.cmd.impl.UnbanCommand;
import org.opencraft.server.cmd.impl.UnbanIPCommand;
import org.opencraft.server.cmd.impl.VIPCommand;
import org.opencraft.server.cmd.impl.VoteCommand;
import org.opencraft.server.cmd.impl.WarnCommand;
import org.opencraft.server.cmd.impl.WaterCommand;
import org.opencraft.server.cmd.impl.XBanCommand;
import org.opencraft.server.cmd.impl.YesCommand;
import org.opencraft.server.game.GameModeAdapter;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.BlockLog;
import org.opencraft.server.model.BlockLog.BlockInfo;
import org.opencraft.server.model.BuildMode;
import org.opencraft.server.model.ChatMode;
import org.opencraft.server.model.CustomBlockDefinition;
import org.opencraft.server.model.DropItem;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.MapController;
import org.opencraft.server.model.MapRatings;
import org.opencraft.server.model.Mine;
import org.opencraft.server.model.MineActivator;
import org.opencraft.server.model.MoveLog;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.Teleporter;
import org.opencraft.server.model.World;
import org.opencraft.server.persistence.SavePersistenceRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

public class CTFGameMode extends GameModeAdapter<Player> {

  public static int blockSpawnX;
  public static int blockSpawnY;
  public static int blockSpawnZ;
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
  public int redCaptures;
  public int blueCaptures;
  public boolean redFlagTaken = false;
  public boolean blueFlagTaken = false;

  public boolean antiStalemate;

  public int payloadPosition = -1;

  public int bluePlayers = 0;
  public int redPlayers = 0;

  public long gameStartTime = System.currentTimeMillis();
  public boolean tournamentGameStarted = false;

  public Level startNewMap;
  public boolean voting = false;
  public boolean isFirstBlood = true;
  public boolean ready = false;
  public ArrayList<String> rtvYesPlayers = new ArrayList<String>();
  public ArrayList<String> rtvNoPlayers = new ArrayList<String>();
  public ArrayList<String> mutedPlayers = new ArrayList<String>();
  public int rtvVotes = 0;
  public ArrayList<String> nominatedMaps = new ArrayList<String>();
  public String currentMap = null;
  public String previousMap = null;
  private Level map;
  private ArrayList<DropItem> items = new ArrayList<DropItem>(8);
  private String statusMessage;

  public CTFGameMode() {
    registerCommand("accept", DuelAcceptCommand.getCommand());
    registerCommand("addspawn", AddSpawnCommand.getCommand());
    registerCommand("b", BlockInfoCommand.getCommand());
    registerCommand("ban", XBanCommand.getCommand());
    registerCommand("banip", BanIPCommand.getCommand());
    registerCommand("blue", BlueCommand.getCommand());
    registerCommand("bounty", BountyCommand.getCommand());
    registerCommand("c", ChatCommand.getCommand());
    registerCommand("commands", HelpCommand.getCommand());
    registerCommand("d", DefuseCommand.getCommand());
    registerCommand("defuse", DefuseCommand.getCommand());
    registerCommand("defusetnt", DefuseTNTCommand.getCommand());
    registerCommand("deop", DeOperatorCommand.getCommand());
    registerCommand("devip", DeVIPCommand.getCommand());
    registerCommand("drop", DropCommand.getCommand());
    registerCommand("dt", DefuseTNTCommand.getCommand());
    registerCommand("duel", DuelCommand.getCommand());
    registerCommand("end", EndCommand.getCommand());
    registerCommand("f", FlamethrowerCommand.getCommand());
    registerCommand("fd", FlagDropCommand.getCommand());
    registerCommand("follow", FollowCommand.getCommand());
    registerCommand("force", ForceCommand.getCommand());
    registerCommand("freeze", FreezeCommand.getCommand());
    registerCommand("g", GlobalChatCommand.getCommand());
    registerCommand("help", TutorialCommand.getCommand());
    registerCommand("helpop", HelpOpCommand.getCommand());
    registerCommand("hidden", HiddenCommand.getCommand());
    registerCommand("hide", HideCommand.getCommand());
    registerCommand("ignore", IgnoreCommand.getCommand());
    registerCommand("join", JoinCommand.getCommand());
    registerCommand("k", KickCommand.getCommand());
    registerCommand("kick", KickCommand.getCommand());
    registerCommand("lava", LavaCommand.getCommand());
    registerCommand("log", LogCommand.getCommand());
    registerCommand("mapenvironment", MapEnvironmentCommand.getCommand());
    registerCommand("mapimport", MapImportCommand.getCommand());
    registerCommand("maps", MapListCommand.getCommand());
    registerCommand("mapset", MapSetCommand.getCommand());
    registerCommand("me", MeCommand.getCommand());
    registerCommand("mute", MuteCommand.getCommand());
    registerCommand("newgame", NewGameCommand.getCommand());
    registerCommand("no", NoCommand.getCommand());
    registerCommand("note", NoteCommand.getCommand());
    registerCommand("notes", NotesCommand.getCommand());
    registerCommand("nominate", NominateCommand.getCommand());
    registerCommand("op", OperatorCommand.getCommand());
    registerCommand("opchat", OpChatCommand.getCommand());
    registerCommand("pay", PayCommand.getCommand());
    registerCommand("ping", PingCommand.getCommand());
    registerCommand("playercount", PlayerCommand.getCommand());
    registerCommand("players", ClientsCommand.getCommand());
    registerCommand("pm", PmCommand.getCommand());
    registerCommand("points", PointsCommand.getCommand());
    registerCommand("pstats", PInfoCommand.getCommand());
    registerCommand("ragequit", RagequitCommand.getCommand());
    registerCommand("removespawn", RemoveSpawnCommand.getCommand());
    registerCommand("quote", QuoteCommand.getCommand());
    registerCommand("red", RedCommand.getCommand());
    registerCommand("reload", ReloadCommand.getCommand());
    registerCommand("restart", RestartCommand.getCommand());
    registerCommand("rtv", RTVCommand.getCommand());
    registerCommand("rules", RulesCommand.getCommand());
    registerCommand("say", SayCommand.getCommand());
    registerCommand("set", SetCommand.getCommand());
    registerCommand("setpath", SetPathCommand.getCommand());
    registerCommand("solid", SolidCommand.getCommand());
    registerCommand("spec", SpecCommand.getCommand());
    registerCommand("start", StartCommand.getCommand());
    registerCommand("stats", StatsCommand.getCommand());
    registerCommand("status", StatusCommand.getCommand());
    registerCommand("store", StoreCommand.getCommand());
    registerCommand("t", TntCommand.getCommand());
    registerCommand("team", TeamCommand.getCommand());
    registerCommand("tnt", TntCommand.getCommand());
    registerCommand("tp", TeleportCommand.getCommand());
    registerCommand("unban", UnbanCommand.getCommand());
    registerCommand("unbanip", UnbanIPCommand.getCommand());
    registerCommand("users", ClientsCommand.getCommand());
    registerCommand("vip", VIPCommand.getCommand());
    registerCommand("vote", VoteCommand.getCommand());
    registerCommand("water", WaterCommand.getCommand());
    registerCommand("warn", WarnCommand.getCommand());
    registerCommand("who", StatusCommand.getCommand());
    registerCommand("yes", YesCommand.getCommand());
  }

  public int getRedPlayers() {
    int redPlayers = 0;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team == 0) {
        redPlayers++;
      }
    }
    return redPlayers;
  }

  public int getBluePlayers() {
    int bluePlayers = 0;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team == 1) {
        bluePlayers++;
      }
    }
    return bluePlayers;
  }

  public void updateStatusMessage() {
    if (getMode() == Level.PAYLOAD) {
      return;
    }

    String redFlag = redFlagTaken ? " &6[!]" : "";
    String blueFlag = blueFlagTaken ? " &6[!]" : "";
    setStatusMessage("Map: " + map.id
        + " | &cRed: " + redCaptures + redFlag + " &f| &9Blue: " + blueCaptures + blueFlag);
  }

  public void setStatusMessage(String message) {
    statusMessage = message;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      sendStatusMessage(p);
    }
  }

  public void sendAnnouncement(String message) {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.getSession().ccUser) {
        sendAnnouncement(p, message);
      }
    }
  }

  public void sendAnnouncement(final Player p, final String message) {
    if (p.getSession().isExtensionSupported("MessageTypes")) {
      new Thread(new Runnable() {

        public void run() {
          try {
              p.getActionSender().sendChatMessage(message, 100);
              Thread.sleep(4000);
              p.getActionSender().sendChatMessage("", 100);
          } catch (InterruptedException ex) {
          }
        }
      }).start();
    }
  }

  public void sendStatusMessage(Player p) {
    p.getActionSender().sendStatusMessage(statusMessage);
  }

  @Override
  public void playerConnected(final Player player) {
    Server.log(player.getName() + " (" + player.getSession().getIP() + ") joined the game");
    if (!Configuration.getConfiguration().isTest() && !GameSettings.getBoolean("Tournament")) {
      WebServer.run(new Runnable() {
        @Override
        public void run() {
          try {
            String urlMessage = URLEncoder.encode("[b][color=#00aa00]" + player.getName() + " " +
                "joined the game[/color][/b]", "UTF-8");
            Server.httpGet(Constants.URL_SENDCHAT + "&msg=" + urlMessage);
          } catch (Exception ex) {

          }
        }
      });
    }

    player.setAttribute("ip", player.getSession().getIP());
    player.muted = isMuted(player.getName());
    String rank;
    try {
      int r = Integer.parseInt(player.getAttribute("rank").toString());
      if (r != 0) {
        rank = " &f(Rank " + r + ", " + player.getAttribute("games").toString() + " games played)";
      } else {
        rank = "";
      }
    } catch (Exception ex) {
      rank = "";
    }
    World.getWorld().broadcast("&a" + player.getName() + " joined the game" + rank);
    String welcome = Configuration.getConfiguration().getWelcomeMessage();
    if (!player.isNewPlayer) {
      if (welcome != null && !welcome.equals("null")) {
        player.getActionSender().sendChatMessage("&a" + welcome);
      }
      player.getActionSender().sendChatMessage("&bSay /join to start playing, or /spec to " +
          "spectate");
      if (getMode() == Level.CTF) {
        player.getActionSender().sendChatMessage("&aSay /help to learn how to play");
      } else if (getMode() == Level.PAYLOAD) {

      } else {
        player.getActionSender().sendChatMessage("&aThis is a Team Deathmatch map. Say /help to " +
            "learn how to play");
      }
      player.getActionSender().sendChatMessage("&aSay /rules to read the rules");
    } else {
      String helpText;
      if (getMode() == Level.CTF) {
        player.getActionSender().sendChatMessage("&bWelcome to Capture the Flag! Here's how you " +
            "play.");
        helpText = Constants.HELP_TEXT;
      } else {
        player.getActionSender().sendChatMessage("&bWelcome to Team Deathmatch! Here's how you " +
            "play.");
        helpText = Constants.TDM_HELP_TEXT;
      }
      player.getActionSender().sendChatMessage("&e" + helpText);
      player.getActionSender().sendChatMessage("&bSay /join to start playing or /spec to spectate" +
          ". /help will show these instructions again.");
    }
    if (!player.getSession().ccUser) {
      player.getActionSender().sendChatMessage("-- &bWe recommend using the &aClassiCube &bclient" +
          " (www.classicube.net) for more features.");
    }
    if (player.getSession().isExtensionSupported("MessageTypes")) {
      ((CTFGameMode) World.getWorld().getGameMode()).sendStatusMessage(player);
      player.getActionSender().sendChatMessage(Constants.SERVER_NAME, 1);
    }
  }

  public void explodeTNT(Player p, Level level, int x, int y, int z, int r, boolean lethal,
                         boolean tk, boolean deleteSelf, String type) {
    if (deleteSelf) {
      level.setBlock(x, y, z, 0);
    }
    if (p.tntRadius == 3) {
      p.bigTNTRemaining--;
    }
    if (p.bigTNTRemaining <= 0 && p.tntRadius == 3) {
      p.tntRadius = 2;
      p.getActionSender().sendChatMessage("- &eYour big TNT has expired!");
    }
    int n = 0;
    if (lethal) {
      float px = x + 0.5f, py = y + 0.5f, pz = z + 0.5f;
      float pr = r + 0.5f;
      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        float tx = (t.getPosition().getX()) / 32f;
        float ty = (t.getPosition().getY()) / 32f;
        float tz = (t.getPosition().getZ()) / 32f;
        if (Math.abs(px - tx) < pr && Math.abs(py - ty) < pr && Math.abs(pz - tz) < pr && (p.team
            != t.team || (tk && (t == p || !t.hasFlag))) && !t.isSafe() && p.canKill(t, true) && t
            .isVisible) {
          t.markSafe();
          n++;
          World.getWorld().broadcast("- " + p.parseName() + " exploded " + t.getColoredName()
              + (type == null ? "" : " &f(" + type + ")"));
          p.gotKill(t);
          t.sendToTeamSpawn();
          t.died(p);
          if (!tk)
            checkFirstBlood(p, t);
          if (t.team != -1 && t.team != p.team) {
            p.setAttribute("explodes", (Integer) p.getAttribute("explodes") + 1);
            p.addStorePoints(5);
          }
          if (t.hasFlag) {
            dropFlag(t.team);
          }
        }
      }
    }
    for (int cx = x - r; cx <= x + r; cx++) {
      for (int cy = y - r; cy <= y + r; cy++) {
        for (int cz = z - r; cz <= z + r; cz++) {
          if (isExplodableBlock(level, cx, cy, cz)) {
            level.setBlock(cx, cy, cz, (byte) 0);
          }
          defuseMineIfCan(p, cx, cy, cz);
        }
      }
    }
    if (n == 2) {
      World.getWorld().broadcast("- &bDouble Kill");
    } else if (n == 3) {
      World.getWorld().broadcast("- &bTriple Kill");
    } else if (n > 3) {
      World.getWorld().broadcast("- &b" + n + "x Kill");
    }
  }

  public boolean isExplodableBlock(Level level, int x, int y, int z) {
    int oldBlock = level.getBlock(x, y, z);
    return !level.isSolid(x, y, z) && oldBlock != 46 && !(x == blueFlagX && z ==
            blueFlagY && y == blueFlagZ) && !(x == redFlagX && z == redFlagY && y ==
            redFlagZ) && !isMine(x, y, z) && !isPayload(x, y, z);
  }

  private void defuseMineIfCan(Player p, int x, int y, int z) {
    if (isMine(x, y, z)) {
      Mine m = World.getWorld().getMine(x, y, z);
      if (m == null) { // Shouldn't get here, but whatever. Just in case.
        return;
      }
      if (m.team != p.team) {
        World.getWorld().removeMine(m);
        World.getWorld().getLevel().setBlock((m.x - 16) / 32, (m.y - 16) / 32, (m.z - 16) /
                32, 0);
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
    int heading = (int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256)) - 90;
    int pitch = 0;

    int distance = GameSettings.getInt("FlameThrowerStartDistanceFromPlayer");
    int length = GameSettings.getInt("FlameThrowerLength");

    double px = (pos.getX());
    double py = (pos.getY());
    double pz = (pos.getZ()) - 1;

    double vx = Math.cos(Math.toRadians(heading));
    double vz = Math.tan(Math.toRadians(pitch));
    double vy = Math.sin(Math.toRadians(heading));
    double x = px;
    double y = py;
    double z = pz;
    for (int i = 0; i < length + distance; i++) {
      int bx = (int) Math.round(x);
      int by = (int) Math.round(y);
      int bz = (int) Math.round(z);

      // Check to make sure we are not above the build height.
      if (bz > World.getWorld().getLevel().ceiling) {
        return;
      }

      int oldBlock = World.getWorld().getLevel().getBlock(bx, by, bz);

      if (i < distance) { // Can't kill people where there's no fire
        if (oldBlock != 0 && oldBlock != 11) { // If it ain't air (or lava), kill it cause we got to burn through first.
          return;
        }
      } else { // Processing actual fire blocks
        // Defuse mine if it's there
        defuseMineIfCan(p, bx, by, bz);
        // Can't go through sand, glass, obsidian, water, or non explodable blocks
        if (oldBlock == BlockConstants.WATER ||
                oldBlock == BlockConstants.STILL_WATER ||
                oldBlock == BlockConstants.SAND ||
                oldBlock == BlockConstants.GLASS ||
                oldBlock == BlockConstants.OBSIDIAN ||
                !isExplodableBlock(World.getWorld().getLevel(), bx, by, bz)) {
          return;
        }

        for (Player t : World.getWorld().getPlayerList().getPlayers()) {
          Position blockPos = t.getPosition().toBlockPos();
          if (blockPos.getX() == bx && blockPos.getY() == by && (blockPos.getZ() == bz + 1 || blockPos.getZ() == bz) &&
                  (p.team != t.team) && !t.isSafe() && p.canKill(t, false)) {
            World.getWorld().broadcast("- " + p.parseName() + " cooked " + t.getColoredName());
            p.gotKill(t);
            t.sendToTeamSpawn();
            t.markSafe();
            t.died(p);
            checkFirstBlood(p, t);
            p.addStorePoints(5);
            if (t.hasFlag) {
              dropFlag(t.team);
            }
          }
        }
      }

      x += vx;
      y += vy;
      z += vz;
    }

  }

  public void showScore() {
    if (getMode() == Level.TDM) {
      World.getWorld().broadcast("- Current score: Red has " + redCaptures + " kills; blue has "
          + blueCaptures + " kills");

    } else if (getMode() == Level.CTF){
      World.getWorld().broadcast("- Current score: Red has " + redCaptures + " captures; blue has" +
          " " + blueCaptures + " captures");
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
    }
  }

  public void placeBlueFlag() {
    if (getMode() == Level.CTF) {
      World.getWorld().getLevel().setBlock(blueFlagX, blueFlagZ, blueFlagY, Constants.BLOCK_BLUE_FLAG);
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
      redFlagX = Integer.parseInt(map.props.getProperty("redFlagX"));
      redFlagY = Integer.parseInt(map.props.getProperty("redFlagY"));
      redFlagZ = Integer.parseInt(map.props.getProperty("redFlagZ"));
      redFlagDropped = false;
    }
  }

  public void resetBlueFlagPos() {
    if (getMode() == Level.CTF) {
      blueFlagX = Integer.parseInt(map.props.getProperty("blueFlagX"));
      blueFlagY = Integer.parseInt(map.props.getProperty("blueFlagY"));
      blueFlagZ = Integer.parseInt(map.props.getProperty("blueFlagZ"));
      blueFlagDropped = false;
    }
  }

  public void updatePayload(int nextPosition) {
    if (payloadPosition != -1) {
      World.getWorld().getLevel().setBlock(
          World.getWorld().getLevel().getPayloadPath().get(payloadPosition), 0);
    }
    payloadPosition = nextPosition;
    World.getWorld().getLevel().setBlock(
        World.getWorld().getLevel().getPayloadPath().get(payloadPosition), Constants.BLOCK_PAYLOAD);
  }

  public void openSpawns() {
    if (getMode() == Level.CTF || getMode() == Level.PAYLOAD) {
      Level map = World.getWorld().getLevel();
      int bDoorX = Integer.parseInt(map.props.getProperty("blueSpawnX"));
      int bDoorY = Integer.parseInt(map.props.getProperty("blueSpawnY")) - 2;
      int bDoorZ = Integer.parseInt(map.props.getProperty("blueSpawnZ"));
      int rDoorX = Integer.parseInt(map.props.getProperty("redSpawnX"));
      int rDoorY = Integer.parseInt(map.props.getProperty("redSpawnY")) - 2;
      int rDoorZ = Integer.parseInt(map.props.getProperty("redSpawnZ"));
      map.setBlock(rDoorX, rDoorZ, rDoorY, (byte) 0x00);
      map.setBlock(bDoorX, bDoorZ, bDoorY, (byte) 0x00);
    }
  }

  public void startGame(Level newMap) {
    final Level oldMap = map;
    if (newMap == null) {
      map = MapController.randomLevel();
    } else {
      map = newMap;
    }
    previousMap = currentMap;
    currentMap = map.id;
    MoveLog.getInstance().logMapChange(map.id);
    new Thread(new Runnable() {

      public void run() {
        try {
          gameStartTime = System.currentTimeMillis();
          tournamentGameStarted = !GameSettings.getBoolean("Tournament");
          for (Player player : World.getWorld().getPlayerList().getPlayers()) {
            player.team = -1;
            player.hasVoted = false;
            player.hasNominated = false;
            player.hasFlag = false;
            player.hasTNT = false;
            player.flamethrowerEnabled = false;
            player.flamethrowerUnits = 200;
            player.accumulatedStorePoints = 0;
            for (CustomBlockDefinition blockDef : oldMap.customBlockDefinitions) {
              player.getActionSender().sendRemoveBlockDefinition(blockDef.id);
            }
          }
          World.getWorld().clearMines();
          startNewMap = null;
          blockSpawnX = (map.getSpawnPosition().getX() - 16) / 32;
          blockSpawnY = (map.getSpawnPosition().getY() - 16) / 32;
          blockSpawnZ = (map.getSpawnPosition().getZ() - 16) / 32;
          redPlayers = 0;
          bluePlayers = 0;
          World.getWorld().setLevel(map);
          resetRedFlagPos();
          resetBlueFlagPos();
          updateStatusMessage();
          updateLeaderboard();
          voting = false;
          rtvVotes = 0;
          rtvYesPlayers.clear();
          rtvNoPlayers.clear();
          nominatedMaps.clear();
          isFirstBlood = true;
          for (Player p : World.getWorld().getPlayerList().getPlayers()) {
            p.joinTeam("spec", false);
          }
          try {
            Thread.sleep(5 * 1000);
          } catch (InterruptedException ex) {
          }
          World.getWorld().broadcast("- &6Say /join to start playing, or /spec to spectate.");
          redFlagTaken = false;
          blueFlagTaken = false;
          redCaptures = 0;
          blueCaptures = 0;
          ready = true;
          updateStatusMessage();
          placeBlueFlag();
          placeRedFlag();
          if (getMode() == Level.PAYLOAD) {
            payloadPosition = -1;
            updatePayload(0);
          }
          openSpawns();
        } catch (Exception ex) {
          Server.log(ex);
          voting = false;
        }
      }
    }).start();
    Server.saveLog();
  }

  private void checkFirstBlood(Player attacker, Player defender) {
    if (isFirstBlood && defender.team != -1) {
      World.getWorld().broadcast("- " + attacker.getColoredName() + " &4took the first blood!");
      attacker.setAttribute("tags", (Integer) attacker.getAttribute("tags") + 10);
      attacker.addStorePoints(50);
      isFirstBlood = false;
    }
  }

  private Player[] getTopPlayers() {
    HashMap<Integer, Player> leaderboard = new HashMap<Integer, Player>(16);
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team != -1) {
        leaderboard.put(p.accumulatedStorePoints, p);
      }
    }

    NavigableSet<Integer> set = new TreeSet<Integer>(leaderboard.keySet());
    Iterator<Integer> itr = set.descendingIterator();
    Player[] top = new Player[3];
    int i = 0;
    while (itr.hasNext()) {
      top[i] = leaderboard.get(itr.next());
      i++;
      if (i >= 3) {
        break;
      }
    }
    return top;
  }

  public void updateLeaderboard() {
    Player[] top = getTopPlayers();
    for (int i = 0; i < 3; i++) {
      String msg;
      if (top[i] != null) {
        msg = (i + 1) + ". " + top[i].getColoredName() + " &f- " + top[i].accumulatedStorePoints;
      } else {
        msg = "";
      }
      int type = 10 + (3 - i);
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        if (p.getSession().isExtensionSupported("MessageTypes")) {
          p.getActionSender().sendChatMessage(msg, type);
        }
      }
    }
  }

  public void endGame() {
    new Thread(new Runnable() {
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
            World.getWorld().broadcast("- &6The game has ended; the " + winner + " team wins!");
          }
          if (getMode() == Level.CTF) {
            World.getWorld().broadcast("- &6Red had " + redCaptures + " captures, blue had " +
                blueCaptures + ".");
          } else {
            World.getWorld().broadcast("- &6Red had " + redCaptures + " kills, blue had " +
                blueCaptures + ".");
          }
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
          Player[] top = getTopPlayers();
          World.getWorld().broadcast("- &3Top players for the round:");
          if (top[0] == null) {
            World.getWorld().broadcast("- &3Nobody");
          }
          for (int j = 0; j < 3; j++) {
            Player p = top[j];
            if (p == null) {
              break;
            }
            World.getWorld().broadcast("- &2" + p.getName() + " - " + p.accumulatedStorePoints);

          }
          for (Player player : World.getWorld().getPlayerList().getPlayers()) {
            player.team = -1;
            player.hasFlag = false;
            player.hasTNT = false;
            if (player.flamethrowerEnabled) {
              World.getWorld().getLevel().clearFire(player.linePosition, player.lineRotation);
            }
            player.flamethrowerEnabled = false;
            player.flamethrowerTime = 0;
            player.rocketTime = 0;
            player.sendToTeamSpawn(false);
          }
          rtvVotes = 0;
          rtvYesPlayers.clear();
          rtvNoPlayers.clear();
          if (GameSettings.getBoolean("Tournament"))
            return;
          World.getWorld().broadcast("- &aMap voting is now open for 40 seconds...");
          World.getWorld().broadcast("- &aSay /vote [mapname] to select the next map!");
          MapController.resetVotes();
          voting = true;
          int count = nominatedMaps.size();
          if (count > 3) {
            count = 3;
          }
          ArrayList<String> mapNames = MapController.getRandomMapNames(3 - count, new
              String[]{currentMap, previousMap});
          mapNames.addAll(nominatedMaps);
          String msg = "";
          for (String map : mapNames) {
            msg += map + ", ";
          }
          World.getWorld().broadcast("- &a" + msg);
          World.getWorld().broadcast("- &3Did you like the map you just played (" + currentMap +")? Say /yes or /no followed by a reason (optional) to vote!");
          new Thread(new Runnable() {
            public void run() {
              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                try {
                  new SavePersistenceRequest(p).perform();
                } catch (IOException ex) {
                }
              }
            }
          }).start();
          Thread.sleep(40 * 1000);
          Level newLevel = MapController.getMostVotedForMap();
          ready = false;
          String rating = MapRatings.getRating(currentMap);
          World.getWorld().broadcast("- &3This map's approval rating is now " + rating);
          World.getWorld().broadcast("- &3See the ratings at http://jacobsc.tf/mapratings.");
          World.getWorld().broadcast("- &e" + newLevel.id + " had the most votes. Starting new " +
              "game!");
          Thread.sleep(7 * 1000);
          startGame(newLevel);
        } catch (Exception ex) {
          voting = false;
          Server.log(ex);
        }
      }
    }).start();
  }

  public void checkForStalemate() {
    if (redFlagTaken && blueFlagTaken) {
      World.getWorld().broadcast("- &eAnti-stalemate mode activated!");
      World.getWorld().broadcast("- &eIf your teammate gets tagged you'll drop the flag");
      antiStalemate = true;
    }
  }

  public void checkForUnbalance(Player p) {
    if (!GameSettings.getBoolean("Tournament")) {
      if (redPlayers < bluePlayers - 2 && p.team == 1) {
        World.getWorld().broadcast("- " + p.parseName() + " was moved to red team for game " +
            "balance.");
        p.joinTeam("red");
      } else if (bluePlayers < redPlayers - 2 && p.team == 0) {
        World.getWorld().broadcast("- " + p.parseName() + " was moved to blue team for game " +
            "balance.");
        p.joinTeam("blue");
      }
    }
  }

  public void dropFlag(int team) {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team == team) {
        dropFlag(p);
      }
    }
    updateStatusMessage();
    antiStalemate = false;
  }

  public void dropFlag(Player p) {
    dropFlag(p, false, false);
  }

  public void dropFlag(Player p, final boolean instant, final boolean isVoluntary) {
    if (p.hasFlag) {
      p.hasFlag = false;
      World.getWorld().broadcast("- " + p.parseName() + " dropped the flag!");
      sendAnnouncement(p.parseName() + " dropped the flag!");
      Position playerPos = p.getPosition().toBlockPos();
      final boolean _antiStalemate = this.antiStalemate;
      if (p.team == 0) {
        blueFlagTaken = false;
        blueFlagDropped = true;
        setBlueFlagPos(playerPos.getX(), playerPos.getZ() - 1, playerPos.getY());
        blueFlagDroppedThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              if ((!_antiStalemate && !instant) || isVoluntary) {
                Thread.sleep(10 * 1000);
              }
              World.getWorld().getLevel().setBlock(blueFlagX, blueFlagZ, blueFlagY, 0);
              resetBlueFlagPos();
              placeBlueFlag();
              World.getWorld().broadcast("- &eThe blue flag has been returned!");
            } catch (InterruptedException ex) {
              return;
            }
          }
        });
        placeBlueFlag();
        blueFlagDroppedThread.start();
      } else {
        redFlagTaken = false;
        redFlagDropped = true;
        setRedFlagPos(playerPos.getX(), playerPos.getZ() - 1, playerPos.getY());
        redFlagDroppedThread = new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              if ((!_antiStalemate && !instant) || isVoluntary) {
                Thread.sleep(10 * 1000);
              }
              World.getWorld().getLevel().setBlock(redFlagX, redFlagZ, redFlagY, 0);
              resetRedFlagPos();
              placeRedFlag();
              World.getWorld().broadcast("- &eThe red flag has been returned!");
            } catch (InterruptedException ex) {
              return;
            }
          }
        });
        placeRedFlag();
        redFlagDroppedThread.start();
      }
    }
  }

  public void processBlockRemove(Player p, int x, int z, int y) {
    if (x == redFlagX && y == redFlagY && z == redFlagZ) {
      if (p.team == 1) {
        if (!redFlagTaken) {
          //red flag taken
          if (getRedPlayers() == 0 || getBluePlayers() == 0) {
            placeRedFlag();
            p.getActionSender().sendChatMessage("- &eFlag can't be captured when one team has 0 " +
                "people");
          } else if (p.duelPlayer != null) {
            placeRedFlag();
            p.getActionSender().sendChatMessage("- &eYou can't take the flag while dueling");
          } else {
            World.getWorld().broadcast("- &eRed flag taken by " + p.parseName() + "!");
            sendAnnouncement("&eRed flag taken by " + p.parseName() + "!");
            p.getActionSender().sendChatMessage("- &eClick your own flag to capture, or use /fd " +
                "to drop the flag and pass to a teammate,");
            p.hasFlag = true;
            redFlagTaken = true;
            checkForStalemate();
            this.updateStatusMessage();
            resetRedFlagPos();
            if (redFlagDroppedThread != null) {
              redFlagDroppedThread.interrupt();
            }
          }

        }
      } else {
        //blue flag returned
        if (p.hasFlag && !redFlagTaken && !redFlagDropped) {
          World.getWorld().broadcast("- &eBlue flag captured by " + p.parseName() + " for the red" +
              " team!");
          sendAnnouncement("&eBlue flag captured by " + p.parseName() + "!");
          redCaptures++;
          p.hasFlag = false;
          blueFlagTaken = false;
          placeBlueFlag();
          p.setAttribute("captures", (Integer) p.getAttribute("captures") + 1);
          p.addStorePoints(20);
          this.updateStatusMessage();
          if (redCaptures == GameSettings.getInt("MaxCaptures")) {
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
          //blue flag taken
          if (getRedPlayers() == 0 || getBluePlayers() == 0) {
            placeBlueFlag();
            p.getActionSender().sendChatMessage("- &eFlag can't be captured when one team has 0 " +
                "people");
          } else if (p.duelPlayer != null) {
            placeBlueFlag();
            p.getActionSender().sendChatMessage("- &eYou can't take the flag while dueling");
          } else {
            World.getWorld().broadcast("- &eBlue flag taken by " + p.parseName() + "!");
            sendAnnouncement("&eBlue flag taken by " + p.parseName() + "!");
            p.getActionSender().sendChatMessage("- &eClick your own flag to capture, or use /fd " +
                "to drop the flag and pass to a teammate,");
            p.hasFlag = true;
            blueFlagTaken = true;
            checkForStalemate();
            this.updateStatusMessage();
            resetBlueFlagPos();
            if (blueFlagDroppedThread != null) {
              blueFlagDroppedThread.interrupt();
            }
          }
        }
      } else {
        //red flag returned
        if (p.hasFlag && !blueFlagTaken && !blueFlagDropped) {
          World.getWorld().broadcast("- &eRed flag captured by " + p.parseName() + " for the blue" +
              " team!");
          sendAnnouncement("&eRed flag captured by " + p.parseName() + "!");
          blueCaptures++;
          p.hasFlag = false;
          redFlagTaken = false;
          placeRedFlag();
          p.setAttribute("captures", (Integer) p.getAttribute("captures") + 1);
          p.addStorePoints(20);
          this.updateStatusMessage();
          if (blueCaptures == GameSettings.getInt("MaxCaptures")) {
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
    int blockX = (x - 16) / 32;
    int blockY = (y - 16) / 32;
    int blockZ = (z - 16) / 32;
    if (p.team != -1) {
      Enumeration<Mine> en = World.getWorld().getAllMines();
      while (en.hasMoreElements()) {
        Mine m = en.nextElement();
        int mx = (m.x - 16) / 32;
        int my = (m.y - 16) / 32;
        int mz = (m.z - 16) / 32;
        if (m.active == true && (p.duelPlayer == null || p.duelPlayer == m.owner) && (m.owner
            .duelPlayer == null || m.owner.duelPlayer == p) && p.team != -1 && m.team != -1 && p
            .team != m.team && m.x > x - 96 && m.x < x + 96 && m.y > y - 96 && m.y < y + 96 && m
            .z > z - 96 && m.z < z + 96 && !p.shield) {
          Level level = World.getWorld().getLevel();
          int r = 1;
          level.setBlock(mx, my, mz, 0);
          for (int cx = mx - r; cx <= mx + r; cx++) {
            for (int cy = my - r; cy <= my + r; cy++) {
              for (int cz = mz - r; cz <= mz + r; cz++) {
                int oldBlock = level.getBlock(cx, cy, cz);
                if (!level.isSolid(cx, cy, cz) && oldBlock != 46 && !(cx == blueFlagX && cz ==
                    blueFlagY && cy == blueFlagZ) && !(cx == redFlagX && cz == redFlagY && cy ==
                    redFlagZ)) {
                  level.setBlock(cx, cy, cz, (byte) 0);
                }
              }
            }
          }
          World.getWorld().broadcast("- " + m.owner.parseName() + " mined " + p.parseName() + ".");
          m.owner.gotKill(p);
          p.sendToTeamSpawn();
          checkFirstBlood(m.owner, p);
          m.owner.setAttribute("mines", (Integer) m.owner.getAttribute("mines") + 1);
          m.owner.removeMine(m);
          World.getWorld().removeMine(m);
          if (p.hasFlag) {
            dropFlag(p.team);
          }
          p.died(m.owner);
        }
      }
    }
    Teleporter te = World.getWorld().getTPEntrance(blockX, blockY, blockZ);
    if (te != null) {
      p.getActionSender().sendTeleport(new Position(te.inX, te.inY, te.inZ), p.getRotation());
    }
    if (blockX == blockSpawnX && blockY == blockSpawnY && blockZ == blockSpawnZ) {
      if (p.hasFlag) {
        dropFlag(p.team);
      }
    }
    if (getMode() == Level.CTF && tournamentGameStarted) {
      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        if (t.getPosition().getX() > x - 64 && t.getPosition().getX() < x + 64 && t.getPosition()
            .getY() > y - 64 && t.getPosition().getY() < y + 64 && t.getPosition().getZ() > z -
            64 && t.getPosition().getZ() < z + 64) {
          processTag(p, t, x, y, z);
        }
      }
    }
  }

  public void processTag(Player p1, Player p2, int x, int y, int z) {
    int t1 = p1.team;
    int t2 = p2.team;
    if (t1 != -1 && t2 != -1) {
      Player tagged = null;
      Player tagger = null;
      int x2 = Math.round((x - 16) / 32);
      if ((x2 > map.divider && t1 == 0) || (x2 < map.divider && t1 == 1) && t2 != t1) {
        tagged = p1;
        tagger = p2;
      } else if ((x2 < map.divider && t1 == 0) || (x2 > map.divider && t1 == 1) && t2 != t1) {
        tagged = p2;
        tagger = p1;
      }
      if (t1 != t2 && tagged != null && tagger != null && tagger.canKill(tagged, false) &&
          !tagged.isSafe() && !tagged.shield) {
        World.getWorld()
            .broadcast("- " + tagger.parseName() + " tagged " + tagged.parseName() + ".");
        tagger.gotKill(tagged);
        tagged.sendToTeamSpawn();
        tagged.markSafe();
        if (antiStalemate) {
          tagger.incStat("stalemateTags");
          dropFlag(tagged.team);
        }
        if (tagged.hasFlag) {
          dropFlag(tagged.team);
        }
        tagged.died(tagger);
        tagger.setAttribute("tags", (Integer) tagger.getAttribute("tags") + 1);
        tagger.addStorePoints(5);
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
    Enumeration en = World.getWorld().getAllMines();
    while (en.hasMoreElements()) {
      Mine m = (Mine) en.nextElement();
      if ((m.x - 16) / 32 == x && (m.y - 16) / 32 == y && (m.z - 16) / 32 == z) {
        return true;
      }
    }
    return false;
  }

  public boolean isPayload(int x, int y, int z) {
    if (payloadPosition == -1) {
      return false;
    }

    Position position = World.getWorld().getLevel().getPayloadPath().get(payloadPosition);
    return position.getX() == x && position.getY() == y && position.getZ() == z;
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
    if (player.placeBlock != -1 && (player.placeBlock != 7 || player.placeBlock == 7 && type ==
        1)) {
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
    } else if (player.buildMode == BuildMode.TELE_ENTRANCE) {
      player.teleX1 = x;
      player.teleY1 = y;
      player.teleZ1 = z;
      player.getActionSender().sendChatMessage("- &eNow place the exit");
      player.buildMode = BuildMode.TELE_EXIT;
      World.getWorld().getLevel().setBlock(x, y, z, 0);
    } else if (player.buildMode == BuildMode.TELE_EXIT) {
      Teleporter tele = new Teleporter();
      tele.owner = player;
      tele.inX = player.teleX1;
      tele.inY = player.teleY1;
      tele.inZ = player.teleZ1;
      tele.outX = x;
      tele.outY = y;
      tele.outZ = z;
      World.getWorld().addTP(tele);
      player.buildMode = BuildMode.NORMAL;
      World.getWorld().getLevel().setBlock(player.teleX1, player.teleY1, player.teleZ1, 11);
      World.getWorld().getLevel().setBlock(x, y, z, 9);
    } else if (player.buildMode == BuildMode.BLOCK_INFO) {
      player.getActionSender().sendChatMessage("Position: " + x + " " + z + " " + y);
      BlockInfo info = BlockLog.getInfo(x, y, z);
      if (info == null) {
        player.getActionSender().sendChatMessage("- &aNo one has changed this block yet.");
      } else {
        player.getActionSender().sendChatMessage("- &aBlock last changed by: " + info.player
            .getName());
      }
      player.getActionSender().sendBlock(x, y, z, (byte) oldType);
      player.buildMode = BuildMode.NORMAL;
    } else {
      if (player.team == -1 && !(player.isOp()) && !player.isVIP()) {
        ignore = true;
        player.getActionSender().sendChatMessage("- &eYou must join a team to build!");
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (byte) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (byte) 0);
        }
      } else if(!tournamentGameStarted) {
        ignore = true;
        player.getActionSender().sendChatMessage("- &aThe game has not started yet.");
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (byte) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (byte) 0);
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
          player.getActionSender().sendChatMessage("- &cWARNING: You will be kicked automatically" +
              " if you continue building here.");
        } else if (player.outOfBoundsBlockChanges == 16) {
          player.getActionSender().sendLoginFailure("\"Lag pillaring\" is not allowed");
          player.getSession().close();
        }
        if (mode == 0) {
          player.getActionSender().sendBlock(x, y, z, (byte) oldType);
        } else {
          player.getActionSender().sendBlock(x, y, z, (byte) 0);
        }
      } else if (player.headBlockPosition != null && x == player.headBlockPosition.getX() && y ==
          player.headBlockPosition.getY() && z == player.headBlockPosition.getZ()) {
        ignore = true;
        player.getActionSender().sendBlock(x, y, z, (byte) oldType);
      } else if (player.brush && type != 46) {
        int height = 3;
        int radius = 3;
        for (int offsetZ = -height; offsetZ <= radius; offsetZ++) {
          for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
              if (level.getBlock(offsetX + x, offsetY + y, offsetZ + z) != 7
                  && !isTNT(offsetX + x, offsetY + y, offsetZ + z)
                  && !isMine(offsetX + x, offsetY + y, offsetZ + z)
                  && !isPayload(offsetX + x, offsetY + y, offsetZ + z)
                  && !(x + offsetX == redFlagX && z + offsetZ == redFlagY && y + offsetY == redFlagZ)
                  && !(x + offsetX == blueFlagX && z + offsetZ == blueFlagY && y + offsetY == blueFlagZ)
                  && Math.abs(offsetX) + Math.abs(offsetY) + Math.abs (offsetZ) <= Math.abs(radius)) {
                level.setBlock(offsetX + x, offsetY + y, offsetZ + z, type);
              }
            }
          }
        }
      } else if (type == Constants.BLOCK_DETONATOR && mode == 1 && !ignore && player.hasTNT)
      {
        int radius = player.tntRadius;
        explodeTNT(player, World.getWorld().getLevel(), player.tntX, player.tntY, player.tntZ,
            radius);
        player.getActionSender().sendBlock(x, y, z, (byte) oldType);
        player.hasTNT = false;
        player.tntX = 0;
        player.tntY = 0;
        player.tntZ = 0;
      } else if (level.isSolid(x, y, z) && (!player.isOp() || !player.placeSolid) &&
          !GameSettings.getBoolean("Chaos")) {
        player.getActionSender().sendBlock(x, y, z, (byte) level.getBlock(x, y, z));
      } else if (isTNT(x, y, z) && !ignore) { //Deleting tnt
        player.getActionSender().sendBlock(x, y, z, (byte) Constants.BLOCK_TNT);
      } else if (isMine(x, y, z) && !ignore) { // Deleting mines
        player.getActionSender().sendBlock(x, y, z, (byte) oldType);
      } else if (isPayload(x, y, z) && !ignore) {
        player.getActionSender().sendBlock(x, y, z, (byte) oldType);
      } else if (type == 46 && mode == 1 && !ignore) //Placing tnt
      {
        if (player.getAttribute("explodes").toString().equals("0")) {
          player.getActionSender().sendChatMessage("- &bPlace a purple block to explode TNT.");
        }
        if (player.team == -1) {
          player.getActionSender().sendChatMessage("- &eYou need to join a team to place TNT!");
          player.getActionSender().sendBlock(x, y, z, (byte) 0x00);
        } else {
          if (mode == 1) {
            if (!player.hasTNT && !(x == redFlagX && z == redFlagY && y == redFlagZ) && !(x ==
                blueFlagX && z == blueFlagY && y == blueFlagZ)) {
              player.hasTNT = true;
              player.tntX = x;
              player.tntY = y;
              player.tntZ = z;
              level.setBlock(x, y, z, type);
            } else if (
                !isTNT(x, y, z)
                && !isPayload(x, y, z)
                && !(x == redFlagX && z == redFlagY && y == redFlagZ)
                && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
              player.getActionSender().sendBlock(x, y, z, (byte) 0x00);
            } else if ((x == redFlagX && z == redFlagY && y == redFlagZ) || (x == blueFlagX && z
                == blueFlagY && y == blueFlagZ)) {
              player.getActionSender().sendBlock(x, y, z, (byte) oldType);
            }
          }
        }
      } else if (type == Constants.BLOCK_MINE && mode == 1 && !ignore) { //Placing mines
        if (player.team == -1) {
          player.getActionSender().sendChatMessage("- &eYou need to join a team to place mines!");
          player.getActionSender().sendBlock(x, y, z, (byte) 0x00);
        } else {
          if (player.mines.size() < GameSettings.getInt("MaxMines") && !(x == redFlagX && z ==
              redFlagY && y == redFlagZ) && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
            final Mine mine = new Mine(x, y, z, player.team, player);
            player.mines.add(mine);
            player.getActionSender().sendChatMessage("- Say /d to defuse the mine.");
            level.setBlock(x, y, z,
                player.team == 0 ? Constants.BLOCK_MINE_RED : Constants.BLOCK_MINE_BLUE);
            World.getWorld().addMine(mine);
            new Thread(new MineActivator(mine, player)).start();
          } else if (
              !isMine(x, y, z)
              && !isPayload(x, y, z)
              && !(x == redFlagX && z == redFlagY && y == redFlagZ)
              && !(x == blueFlagX && z == blueFlagY && y == blueFlagZ)) {
            player.getActionSender().sendBlock(x, y, z, (byte) 0x00);
          } else if ((x == redFlagX && z == redFlagY && y == redFlagZ) || (x == blueFlagX && z ==
              blueFlagY && y == blueFlagZ)) {
            player.getActionSender().sendBlock(x, y, z, (byte) oldType);
          }
        }
      } else if (
          (type == BlockConstants.LAVA
              || type == BlockConstants.WATER
              || type == BlockConstants.ADMINIUM
              || type == Constants.BLOCK_MINE_RED
              || type == Constants.BLOCK_MINE_BLUE)
          && !player.isOp()) {
        player.getActionSender().sendBlock(x, y, z, (byte) 0);
        player.getActionSender().sendChatMessage("- &eYou can't place this block type!");
      } else if (getDropItem(x, y, z) != null) {
        DropItem i = getDropItem(x, y, z);
        i.pickUp(player);
      } else if ((x == redFlagX && z == redFlagY && y == redFlagZ) && mode == 1 && !redFlagTaken
          && !ignore) {
        player.getActionSender().sendBlock(x, y, z, (byte) Constants.BLOCK_RED_FLAG);
      } else if ((x == blueFlagX && z == blueFlagY && y == blueFlagZ) && mode == 1 &&
          !blueFlagTaken && !ignore) {
        player.getActionSender().sendBlock(x, y, z, (byte) Constants.BLOCK_BLUE_FLAG);
      } else if (/*type < 50 + 16 && */type > -1) {
        if (!ignore) {
          level.setBlock(x, y, z, (mode == 1 ? type : 0));
          BlockLog.logBlockChange(player, x, y, z);
        } else {
          player.getActionSender().sendBlock(x, y, z, (byte) oldType);
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
    Server.log(p.getName() + " left the game");
    if (!Configuration.getConfiguration().isTest() && !GameSettings.getBoolean("Tournament")) {
      WebServer.run(new Runnable() {
        @Override
        public void run() {
          try {
            String urlMessage = URLEncoder.encode("[b][color=#00aa00]" + p.getName() + " left the" +
                " game[/color][/b]", "UTF-8");
            Server.httpGet(Constants.URL_SENDCHAT + "&msg=" + urlMessage);
          } catch (Exception ex) {

          }
        }
      });
    }
    if (p.hasFlag) {
      dropFlag(p);
    }

    p.clearMines();

    if (p.team == 0) {
      ((CTFGameMode) World.getWorld().getGameMode()).redPlayers--;
    } else if (p.team == 1) {
      ((CTFGameMode) World.getWorld().getGameMode()).bluePlayers--;
    }
    if (p.duelPlayer != null) {
      p.duelPlayer.duelPlayer = null;
      p.duelPlayer = null;
    }
    World.getWorld().broadcast("&a" + p.getName() + " left the game");

    if (World.getWorld().getPlayerList().size() == 0)
      rtvVotes = 0;
  }

  public void broadcastChatMessage(final Player player, final String message) {
    if (player.lastMessage != null && message.equals(player.lastMessage) && System
        .currentTimeMillis() - player.lastMessageTime < 750) {
      player.getActionSender().sendChatMessage("&7Duplicate chat message not sent.");
      return;
    }
    player.lastMessage = message;
    player.lastMessageTime = System.currentTimeMillis();
    
    // Toggle chat mode ("#", "$", "@player").
    if (message.equals("#") && player.isOp()) {
      if(player.chatMode != ChatMode.OPERATOR) {
        player.chatMode = ChatMode.OPERATOR;
        player.getActionSender().sendChatMessage("&7Switched to op chat, say # again to return to normal.");
      } else {
        player.chatMode = ChatMode.DEFAULT;
        player.getActionSender().sendChatMessage("&7Switched to normal chat.");        
      }
      return;
    } else if(message.equals("$")) {
      if(player.chatMode != ChatMode.TEAM) {
        player.chatMode = ChatMode.TEAM;
        player.getActionSender().sendChatMessage("&7Switched to team chat, say $ again to return to normal.");
      } else {
        player.chatMode = ChatMode.DEFAULT;
        player.getActionSender().sendChatMessage("&7Switched to normal chat.");        
      }
      return;
    } else if(message.startsWith("@") && message.trim().split(" ").length == 1) {
      if (message.length() > 1) {
        Player messagePlayer = Player.getPlayer(message.substring(1), player.getActionSender());
        if (messagePlayer != null) {
          player.chatMode = ChatMode.PRIVATE;
          player.chatPlayer = messagePlayer;
          player.getActionSender().sendChatMessage("&7Switched to private chat with " + messagePlayer.getName() + ", say @ to return to normal.");
        } else {
          player.getActionSender().sendChatMessage("- &ePlayer not found.");
        }
      } else if (player.chatMode == ChatMode.PRIVATE) {
        player.chatMode = ChatMode.DEFAULT;
        player.getActionSender().sendChatMessage("&7Switched to normal chat.");
      } else {
        player.getActionSender().sendChatMessage("@name message");
      }
      return;
    }
    
    ChatMode messageChatMode = player.chatMode;
    String messageToSend = message;
    Player messagePlayer = player.chatPlayer;
    
    // Set temp chat mode for this message("#message", "$message", "@player message").
    if (message.startsWith("@") && message.length() > 1 && message.trim().split(" ").length > 1) {
      String[] parts = message.split(" ");
      Player other = Player.getPlayer(parts[0].substring(1), player.getActionSender());
      if (other == null) {
        return;
      }
      String text = "";
      for (int i = 1; i < parts.length; i++) {
        text += " " + parts[i];
      }
      text = text.trim();
      if (text.isEmpty()) {
        player.getActionSender().sendChatMessage("@name message");
        return;
      }
      messageChatMode = ChatMode.PRIVATE;
      messageToSend = text;
      messagePlayer = other;
    } else if (message.startsWith("#") && message.length() > 1) {
      messageChatMode = player.chatMode == ChatMode.OPERATOR ? ChatMode.DEFAULT : ChatMode.OPERATOR;
      messageToSend = message.substring(1);
    } else if (message.startsWith("$") && message.length() > 1) {
      messageChatMode = player.chatMode == ChatMode.TEAM ? ChatMode.DEFAULT : ChatMode.TEAM;
      messageToSend = message.substring(1);
    }
    
    switch (messageChatMode) {
      case TEAM:
        World.getWorld().sendTeamChat(player, messageToSend);
        break;
      case OPERATOR:
        World.getWorld().sendOpChat(player, messageToSend);
        break;
      case PRIVATE:
        World.getWorld().sendPM(player, messagePlayer, messageToSend);
        break;
      default:
        String error = null;
        if (player.muted) {
          error = "You're muted!";
        }
        if (error != null) {
          player.getActionSender().sendChatMessage("- &e" + error);
        } else {
          World.getWorld().sendChat(player, messageToSend);
          if (!Configuration.getConfiguration().isTest() && !GameSettings.getBoolean("Tournament")) {
            WebServer.run(new Runnable() {
              @Override
              public void run() {
                try {
                  String urlMessage = URLEncoder.encode(message, "UTF-8");
                  String urlName = URLEncoder.encode(player.getName(), "UTF-8");
                  Server.httpGet(Constants.URL_SENDCHAT + "&username=" + urlName + "&msg=" +
                      urlMessage);
                } catch (UnsupportedEncodingException ex) {
                  Server.log(ex);
                }
              }
            });
          }
        }
        Server.log(player.getName() + ": " + message);
        break;
    }
  }

  public void addDropItem(DropItem i) {
    items.add(i);
  }

  public void removeDropItem(DropItem i) {
    items.remove(i);
  }

  public DropItem getDropItem(int x, int y, int z) {
    for (DropItem i : items) {
      if (x == i.posX && y == i.posY && z == i.posZ) {
        return i;
      }
    }
    return null;
  }

  public int getMode() {
    return World.getWorld().getLevel().mode;
  }

  public void mute(Player p) {
    mutedPlayers.add(p.getName());
  }

  public void unmute(Player p) {
    mutedPlayers.remove(p.getName());
  }

  public boolean isMuted(String name) {
    return mutedPlayers.contains(name);
  }
}
