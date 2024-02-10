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
package org.opencraft.server.game;

import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.WebServer;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.impl.*;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.ChatMode;
import org.opencraft.server.model.CustomBlockDefinition;
import org.opencraft.server.model.DropItem;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.MapController;
import org.opencraft.server.model.MoveLog;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUI;
import org.opencraft.server.model.World;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import tf.jacobsc.ctf.server.commands.QualityCommand;
import tf.jacobsc.utils.DuelKt;
import tf.jacobsc.utils.RatingKt;
import tf.jacobsc.utils.RatingType;

public abstract class GameMode {

  private final Map<String, Command> commands = new HashMap<String, Command>();

  public static int blockSpawnX;
  public static int blockSpawnY;
  public static int blockSpawnZ;
  public boolean tournamentGameStarted = false;
  public long gameStartTime = System.currentTimeMillis();
  public Level startNewMap;
  public boolean voting = false;
  public boolean isFirstBlood = true;
  public boolean ready = false;
  public int bluePlayers = 0;
  public int redPlayers = 0;
  public ArrayList<String> rtvYesPlayers = new ArrayList<>();
  public ArrayList<String> rtvNoPlayers = new ArrayList<>();
  public int rtvVotes = 0;
  public ArrayList<String> nominatedMaps = new ArrayList<>();
  public final ArrayList<KillFeedItem> killFeed = new ArrayList<>();
  public String currentMap = null;
  public String previousMap = null;
  public Level map;
  private ArrayList<DropItem> items = new ArrayList<>(8);

  public GameMode() {
    registerCommand("accept", DuelAcceptCommand.getCommand());
    registerCommand("addspawn", AddSpawnCommand.getCommand());
    registerCommand("b", BlockInfoCommand.getCommand());
    registerCommand("ban", XBanCommand.getCommand());
    registerCommand("banip", BanIPCommand.getCommand());
    registerCommand("blue", BlueCommand.getCommand());
    registerCommand("bounty", BountyCommand.getCommand());
    registerCommand("c", ChatCommand.getCommand());
    registerCommand("clients", ClientsCommand.getCommand());
    registerCommand("coinflip", CoinFlipCommand.getCommand());
    registerCommand("commands", CommandsCommand.getCommand());
    registerCommand("deop", DeOperatorCommand.getCommand());
    registerCommand("devip", DeVIPCommand.getCommand());
    registerCommand("drop", DropCommand.getCommand());
    registerCommand("duel", DuelCommand.getCommand());
    registerCommand("end", EndCommand.getCommand());
    registerCommand("follow", FollowCommand.getCommand());
    registerCommand("force", ForceCommand.getCommand());
    registerCommand("freeze", FreezeCommand.getCommand());
    registerCommand("g", GlobalChatCommand.getCommand());
    registerCommand("help", HelpCommand.getCommand());
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
    registerCommand("setscore", SetScoreCommand.getCommand());
    registerCommand("setspawn", SetspawnCommand.getCommand());
    registerCommand("setspawnzone", SetSpawnZoneCommand.getCommand());
    registerCommand("solid", SolidCommand.getCommand());
    registerCommand("spec", SpecCommand.getCommand());
    registerCommand("start", StartCommand.getCommand());
    registerCommand("quality", QualityCommand.INSTANCE);
    registerCommand("stats", StatsCommand.getCommand());
    registerCommand("status", StatusCommand.getCommand());
    registerCommand("store", StoreCommand.getCommand());
    registerCommand("streamermode", StreamerModeCommand.getCommand());
    registerCommand("team", TeamCommand.getCommand());
    registerCommand("tp", TeleportCommand.getCommand());
    registerCommand("unban", UnbanCommand.getCommand());
    registerCommand("unbanip", UnbanIPCommand.getCommand());
    registerCommand("vip", VIPCommand.getCommand());
    registerCommand("vote", VoteCommand.getCommand());
    registerCommand("water", WaterCommand.getCommand());
    registerCommand("warn", WarnCommand.getCommand());
    registerCommand("who", StatusCommand.getCommand());
    registerCommand("whitelist", WhitelistCommand.getCommand());
    registerCommand("yes", YesCommand.getCommand());
    registerCommand("lb", LeaderBoardCommand.getCommand());
  }

  public void registerCommand(String name, Command command) {
    commands.put(name, command);
  }

  public Map<String, Command> getCommands() {
    return commands;
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

  public void sendAnnouncement(String message) {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.getSession().ccUser) {
        sendAnnouncement(p, message);
      }
    }
  }

  public void sendAnnouncement(final Player p, final String message) {
    if (p.getSession().isExtensionSupported("MessageTypes")) {
      new Thread(
          new Runnable() {

            public void run() {
              try {
                p.announcement = message;
                p.getActionSender().sendChatMessage(message, 100);
                Thread.sleep(4000);
                if (p.announcement.equals(message)) {
                  // Don't clear if it has since been updated again.
                  p.announcement = "";
                  p.getActionSender().sendChatMessage("", 100);
                }
              } catch (InterruptedException ex) {
              }
            }
          })
          .start();
    }
  }

  public void tick() {
  }

  public void playerConnected(Player player) {
    Server.log(player.getName() + " (" + player.getSession().getIP() + ") joined the game");
    if (!Configuration.getConfiguration().isTest() && !GameSettings.getBoolean("Tournament")) {
      WebServer.run(
          new Runnable() {
            @Override
            public void run() {
              try {
                String urlMessage =
                    URLEncoder.encode(
                        "[b][color=#00aa00]"
                            + player.getName()
                            + " "
                            + "joined the game[/color][/b]",
                        "UTF-8");
                Server.httpGet(Constants.URL_SENDCHAT + "&msg=" + urlMessage);
              } catch (Exception ex) {
              }
            }
          });
    }
    WebServer.sendDiscordMessage(player.getName() + " joined the game", null);

    player.setAttribute("ip", player.getSession().getIP());
    player.muted = Server.isMuted(player.getName());
    String rank;
    try {
      int r = Integer.parseInt(player.getAttribute("rank").toString());
      if (r != 0) {
        rank = " &f(Rank " + r + ", " + player.getIntAttribute("games") + " games played)";
      } else {
        rank = "";
      }
    } catch (Exception ex) {
      rank = "";
    }

    if (player.getRatedGamesFor(RatingType.Team) > 10) {
      rank += " (TR:" + RatingKt.displayRating(player.getTeamRating()) + ")";
    }

    World.getWorld().broadcast("&a" + player.getName() + " joined the game" + rank);
    if (!player.getSession().ccUser) {
      player
          .getActionSender()
          .sendChatMessage(
              "-- &bWe recommend using the &aClassiCube &bclient"
                  + " (www.classicube.net) for more features.");
    }
    if (player.getSession().isExtensionSupported("MessageTypes")) {
      synchronized (killFeed) {
        World.getWorld().getGameMode().sendKillFeed(player);
      }
    }
  }

  public void playerDisconnected(final Player p) {
    Server.log(p.getName() + " left the game");
    if (!Configuration.getConfiguration().isTest() && !GameSettings.getBoolean("Tournament")) {
      WebServer.run(
          new Runnable() {
            @Override
            public void run() {
              try {
                String urlMessage =
                    URLEncoder.encode(
                        "[b][color=#00aa00]" + p.getName() + " left the" + " game[/color][/b]",
                        "UTF-8");
                Server.httpGet(Constants.URL_SENDCHAT + "&msg=" + urlMessage);
              } catch (Exception ex) {}
            }
          });
    }

    // calculate a rating loss for them
    RatingKt.checkForTeamAbandonment(p);
    if (GameSettings.getBoolean("Tournament") && p.team >= 0 && World.getWorld()
        .getGameMode().tournamentGameStarted) {
    }

    WebServer.sendDiscordMessage(p.getName() + " left the game", null);

    if (p.team == 0) {
      redPlayers--;
    } else if (p.team == 1) {
      bluePlayers--;
    }
    DuelKt.abandonDuel(p);
    World.getWorld().broadcast("&a" + p.getName() + " left the game");

    if (World.getWorld().getPlayerList().size() == 0) {
      rtvVotes = 0;
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
    if (World.getWorld().getPlayerList().size() > 0) {
      WebServer.sendDiscordMessage("Switching map: " + map.id, null);
    }
    MoveLog.getInstance().logMapChange(map.id);
    new Thread(
        new Runnable() {

          public void run() {
            try {
              gameStartTime = System.currentTimeMillis();
              tournamentGameStarted = !GameSettings.getBoolean("Tournament");
              for (Player player : World.getWorld().getPlayerList().getPlayers()) {
                player.team = -1;
                player.hasVoted = false;
                player.hasNominated = false;
                player.currentRoundPointsEarned = 0;
                player.setPoints(GameSettings.getInt("InitialPoints"));

                // Remove custom blocks
                for (CustomBlockDefinition blockDef : oldMap.customBlockDefinitions) {
                  player.getActionSender().sendRemoveBlockDefinition(blockDef.id);
                }

                // Remove red spawn zone, if applicable
                if (oldMap.redSpawnZoneMin != null && oldMap.redSpawnZoneMax != null) {
                  player.getActionSender().sendRemoveSelectionCuboid(0);
                }

                // Remove blue spawn zone, if applicable
                if (oldMap.blueSpawnZoneMin != null && oldMap.blueSpawnZoneMax != null) {
                  player.getActionSender().sendRemoveSelectionCuboid(1); // Blue spawn zone
                }
              }
              clearDropItems();
              World.getWorld().clearMines();
              startNewMap = null;
              blockSpawnX = (map.getSpawnPosition().getX() - 16) / 32;
              blockSpawnY = (map.getSpawnPosition().getY() - 16) / 32;
              blockSpawnZ = (map.getSpawnPosition().getZ() - 16) / 32;
              redPlayers = 0;
              bluePlayers = 0;
              World.getWorld().setLevel(map);
              clearKillFeed();
              voting = false;
              rtvVotes = 0;
              rtvYesPlayers.clear();
              rtvNoPlayers.clear();
              nominatedMaps.clear();
              isFirstBlood = true;
              for (Player p : World.getWorld().getPlayerList().getPlayers()) {
                p.joinTeam("spec", false);
              }
              resetGameMode();
              try {
                Thread.sleep(5 * 1000);
              } catch (InterruptedException ex) {
              }
              World.getWorld()
                  .broadcast("- &6Say /join to start playing, or /spec to spectate.");
              ready = true;
            } catch (Exception ex) {
              Server.log(ex);
              voting = false;
            }
          }
        })
        .start();
    Server.saveLog();
  }

  protected void checkFirstBlood(Player attacker, Player defender) {
    if (isFirstBlood && defender.team != -1) {
      World.getWorld().broadcast("- " + attacker.getColoredName() + " &4took the first blood!");
      attacker.addPoints(GameSettings.getInt("FirstBloodReward"));
      isFirstBlood = false;
    }
  }

  public Player[] getTopPlayers(int number) {
    HashMap<Integer, Player> leaderboard = new HashMap<Integer, Player>(16);
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.team != -1) {
        leaderboard.put(p.currentRoundPointsEarned, p);
      }
    }

    NavigableSet<Integer> set = new TreeSet<Integer>(leaderboard.keySet());
    Iterator<Integer> itr = set.descendingIterator();
    Player[] top = new Player[number];
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

  private void clearKillFeed() {
    synchronized (killFeed) {
      killFeed.clear();
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        if (p.getSession().isExtensionSupported("MessageTypes")) {
          for (int i = 0; i < 3; i++) {
            p.getActionSender().sendChatMessage("", 11 + i);
          }
        }
      }
    }
  }

  public void sendKillFeed(Player p) {
    if (p == null) {
      return;
    }
    if (p.getSession().isExtensionSupported("MessageTypes")) {
      for (int i = 0; i < 3; i++) {
        if (i >= killFeed.size()) {
          p.getActionSender().sendChatMessage("", 11 + i);
        } else {
          p.getActionSender().sendChatMessage(killFeed.get(i).getMessage(), 11 + i);
        }
      }
    }
  }

  public void pruneKillFeed() {
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
  }

  public void checkForUnbalance(Player p) {
    if (!GameSettings.getBoolean("Tournament")) {
      if (redPlayers < bluePlayers - 2 && p.team == 1) {
        World.getWorld()
            .broadcast("- " + p.parseName() + " was moved to red team for game " + "balance.");
        p.joinTeam("red");
      } else if (bluePlayers < redPlayers - 2 && p.team == 0) {
        World.getWorld()
            .broadcast("- " + p.parseName() + " was moved to blue team for game " + "balance.");
        p.joinTeam("blue");
      }
    }
  }

  public void broadcastChatMessage(final Player player, final String message) {
    if (player.AFK) {
      World.getWorld().broadcast("- " + player.parseName() + " is no longer AFK");
    }
    player.moveTime = System.currentTimeMillis();
    player.AFK = false;
    if (player.lastMessage != null
        && message.equals(player.lastMessage)
        && System.currentTimeMillis() - player.lastMessageTime < 750) {
      player.getActionSender().sendChatMessage("&7Duplicate chat message not sent.");
      return;
    }
    player.lastMessage = message;
    player.lastMessageTime = System.currentTimeMillis();

    // Toggle chat mode ("#", "$", "@player").
    if (message.equals("#") && player.isOp()) {
      if (player.chatMode != ChatMode.OPERATOR) {
        player.chatMode = ChatMode.OPERATOR;
        player
            .getActionSender()
            .sendChatMessage("&7Switched to op chat, say # again to return to normal.");
      } else {
        player.chatMode = ChatMode.DEFAULT;
        player.getActionSender().sendChatMessage("&7Switched to normal chat.");
      }
      return;
    } else if (message.equals("$")) {
      if (player.chatMode != ChatMode.TEAM) {
        player.chatMode = ChatMode.TEAM;
        player
            .getActionSender()
            .sendChatMessage("&7Switched to team chat, say $ again to return to normal.");
      } else {
        player.chatMode = ChatMode.DEFAULT;
        player.getActionSender().sendChatMessage("&7Switched to normal chat.");
      }
      return;
    } else if (message.startsWith("@") && message.trim().split(" ").length == 1) {
      if (message.length() > 1) {
        Player messagePlayer = Player.getPlayer(message.substring(1), player.getActionSender());
        if (messagePlayer != null) {
          player.chatMode = ChatMode.PRIVATE;
          player.chatPlayer = messagePlayer;
          player
              .getActionSender()
              .sendChatMessage(
                  "&7Switched to private chat with "
                      + messagePlayer.getName()
                      + ", say @ to return to normal.");
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
          if (!Configuration.getConfiguration().isTest()) {
            WebServer.run(
                new Runnable() {
                  @Override
                  public void run() {
                    try {
                      String urlMessage = URLEncoder.encode(message, "UTF-8");
                      String urlName = URLEncoder.encode(player.getName(), "UTF-8");
                      Server.httpGet(
                          Constants.URL_SENDCHAT + "&username=" + urlName + "&msg=" + urlMessage);
                    } catch (UnsupportedEncodingException ex) {
                      Server.log(ex);
                    }
                  }
                });
          }
          WebServer.sendDiscordMessage(message, player.getName());
        }
        Server.log(player.getName() + ": " + message);
        break;
    }
  }

  public void step() {
    pruneKillFeed();
  }

  private void clearDropItems() {
    items.clear();
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

  protected abstract void resetGameMode();

  public abstract void setBlock(Player player, Level level, int x, int y, int z, int mode,
      int type);

  public abstract boolean isSolidBlock(Level level, int x, int y, int z);

  public abstract void playerChangedTeam(Player player);

  public abstract void endGame();

  public abstract PlayerUI createPlayerUI(Player p);

  public void processPlayerMove(Player p) {

  }

  public void playerRespawn(Player p) {

  }

  public static class KillFeedItem {

    public final long time = System.currentTimeMillis();
    public final Player source;
    public final Player target;
    public final int count;
    public final boolean isKill;
    public final String message;

    public KillFeedItem(Player source, Player target, int count, boolean isKill, String message) {
      this.source = source;
      this.target = target;
      this.count = count;
      this.isKill = isKill;
      this.message = message;
    }

    public String getMessage() {
      if (this.message != null) {
        return this.message;
      }
      String message = "";
      if (source != null) {
        message += source.getColoredName() + " &f";
      }
      message += (isKill ? PlayerUI.KILL_ICON : PlayerUI.HIT_ICON)
          + " " + target.getColoredName();
      if (count > 1) {
        message += " &f(x" + count + ")";
      }
      return message;
    }
  }
}
