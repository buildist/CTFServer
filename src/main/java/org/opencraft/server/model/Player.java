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
package org.opencraft.server.model;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.net.ActionSender;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.PingList;
import org.opencraft.server.persistence.LoadPersistenceRequest;
import org.opencraft.server.persistence.SavePersistenceRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents a connected player.
 *
 * @author Graham Edgecombe
 */
public class Player extends Entity {

  public static short NAME_ID = 0;

  // Shared
  private final MinecraftSession session;
  private final String name;
  private final Map<String, Object> attributes = new HashMap<String, Object>();
  public short nameId;
  public boolean isNewPlayer = false;
  public boolean appendingChat = false;
  public String partialChatMessage = "";
  public String lastMessage;
  public String announcement = "";
  public long lastMessageTime;
  public long lastPacketTime;
  public int heldBlock = 0;
  public boolean joinedDuringTournamentMode;
  public boolean muted = false;
  public boolean frozen = false;
  public boolean AFK = false;
  public long moveTime = 0;
  public int team = -1;
  public int outOfBoundsBlockChanges = 0;
  public int placeBlock = -1;
  public boolean placeSolid = false;
  public boolean isHidden = false;
  public boolean isLegal = true;
  public Position lastLegalPosition = new Position(0, 0, 0);
  public int boxStartX = -1;
  public int boxStartY = -1;
  public int boxStartZ = -1;
  public int buildMode;
  public Position linePosition;
  public Rotation lineRotation;
  public long flamethrowerTime = 0;
  public float flamethrowerFuel = Constants.FLAME_THROWER_FUEL;
  private boolean flamethrowerEnabled = false;
  public long creeperTime;
  public long grenadeTime;
  public long lineTime;
  public long rocketTime;
  public int headBlockType = 0;
  public Position headBlockPosition = null;
  public int currentRoundPointsEarned = 0;
  public Player duelChallengedBy = null;
  public Player duelPlayer = null;
  public int kills = 0;
  public int deaths = 0;
  public int captures = 0;
  public int duelKills = 0;
  public int bountySet = 0;
  public Player bountied = null;
  public Player bountiedBy = null;
  public int bountyKills = 0;
  public int bountyAmount = 0;
  public boolean bountyMode = false;
  public int lastAmount = 0;
  public boolean bountyActive = false;
  public HashSet<String> ignorePlayers = new HashSet<String>();
  private ActionSender actionSender = null;
  private Player instance;
  private Thread followThread;
  private AtomicBoolean follow = new AtomicBoolean(false);
  public ChatMode chatMode = ChatMode.DEFAULT;
  public Player chatPlayer;
  public boolean sendCommandLog = false;
  public final PingList pingList = new PingList();
  private final PlayerUI ui;
  public Position safePosition = new Position(0, 0, 0);
  private int currentRoundPoints = Constants.INITIAL_PLAYER_POINTS;

  // CTF
  public final LinkedList<Mine> mines = new LinkedList<Mine>();
  public int killstreak = 0;
  private long safeTime = 0;
  public boolean hasTNT = false;
  public int tntX;
  public int tntY;
  public int tntZ;
  public int tntRadius = 2;
  public boolean hasFlag = false;
  public boolean brush = false;
  public boolean hasVoted = false;
  public boolean hasNominated = false;
  // STORE STUFF
  public int bigTNTRemaining = 0;

  // Laser Tag
  private int ammo;
  private int health;
  public boolean isDead = false;
  public boolean isReloading = false;
  public int reloadStep = 0;

  public Player(MinecraftSession session, String name) {
    this.session = session;
    this.name = name;
    instance = this;
    nameId = NAME_ID;
    NAME_ID++;
    if (NAME_ID == 256) {
      NAME_ID = 0;
    }
    ui = World.getWorld().getGameMode().createPlayerUI(this);
    setAmmo(GameSettings.getInt("Ammo"));
    setHealth(GameSettings.getInt("Health"));
    setPoints(GameSettings.getInt("InitialPoints"));
  }

  public boolean canSee(Player otherPlayer) {
    return !otherPlayer.isHidden && (otherPlayer.team != -1 || team == -1);
  }

  public int getAmmo() {
    return ammo;
  }

  public void setAmmo(int value) {
    this.ammo = value;
  }

  public int getHealth() {
    return health;
  }

  public void setHealth(int value) {
    this.health = value;
  }

  public static Position getSpawnPos() {
    Level l = World.getWorld().getLevel();
    boolean done = false;
    int x = 0;
    int z = l.ceiling - 4;
    int y = 0;
    for (int i = 0; i < 1000; i++) {
      x = (int) (Math.random() * l.getWidth());
      y = (int) (Math.random() * l.getDepth());
      int block = 0;

      for (z = l.ceiling - 4; z > 0; z--) {
        block = l.getBlock(x, y, z);
        if (block != 0) {
          break;
        }
      }
      if (block != 0) {
        done = true;
        z += 2;
        break;
      }
    }
    return new Position(x * 32 + 16, y * 32 + 16, z * 32 + 16);
  }

  public static Player getPlayer(String name, ActionSender source) {
    Player player = null;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.getName().toLowerCase().equals(name.toLowerCase())) {
        return p;
      }
      if (p.getName().toLowerCase().contains(name.toLowerCase())) {
        if (player == null) {
          player = p;
        } else {
          player = null;
          if (source != null) {
            source.sendChatMessage("- &e\"" + name + "\" matches multiple players.");
          }
          break;
        }
      }
    }
    return player;
  }

  public static Player setAttributeFor(String name, String k, String v, ActionSender source) {
    Player player = getPlayer(name, source);
    if (player != null) {
      player.setAttribute(k, v);
      return player;
    } else {
      Player p = new Player(null, name);
      try {
        new LoadPersistenceRequest(p).perform();
        p.setAttribute(k, v);
        new SavePersistenceRequest(p).perform();
      } catch (Exception e) {
        source.sendChatMessage("- &eError setting attribute: " + e.toString());
        Server.log(e);
      }
      return null;
    }
  }

  public static String getAttributeFor(String name, String k, ActionSender source) {
    Player player = getPlayer(name, source);
    if (player != null) {
      if (player.getAttribute(k) == null) {
        return null;
      } else {
        return player.getAttribute(k).toString();
      }
    } else {
      Player p = new Player(null, name);
      try {
        new LoadPersistenceRequest(p).perform();
        if (p.getAttribute(k) == null) {
          return null;
        } else {
          return p.getAttribute(k).toString();
        }
      } catch (Exception e) {
        source.sendChatMessage("- &eError getting attribute: " + e.toString());
        Server.log(e);
      }
      return null;
    }
  }

  public void removeMine(Mine m) {
    synchronized (mines) {
      mines.remove(m);
    }
  }

  public void clearMines() {
    synchronized (mines) {
      for (Mine m : mines) {
        World.getWorld().removeMine(m);
        World.getWorld().getLevel().setBlock((m.x - 16) / 32, (m.y - 16) / 32, (m.z - 16) / 32, 0);
      }
      mines.clear();
    }
  }

  public void ignore(Player p) {
    if (p.isOp()) {
      getActionSender().sendChatMessage("- &eYou can't ignore operators.");
    } else {
      String name = p.name;
      if (!ignorePlayers.contains(name)) {
        ignorePlayers.add(name);
        getActionSender()
            .sendChatMessage(
                "- &eNow ignoring " + p.parseName() + ". Use this " + "command again to stop");
      } else {
        ignorePlayers.remove(name);
        getActionSender().sendChatMessage("- &eNo longer ignoring " + p.getColoredName());
      }
    }
  }

  public boolean isIgnored(Player p) {
    return ignorePlayers.contains(p.name);
  }

  public void toggleFlameThrower() {
    if (!World.getWorld().getGameMode().tournamentGameStarted) {
      this.getActionSender().sendChatMessage("- &eThe game has not started!");
      return;
    }

    if (team == -1) {
      return;
    }

    if (isFlamethrowerEnabled()) {
      disableFlameThrower();
    } else {
      enableFlameThrower();
    }
    flamethrowerTime = System.currentTimeMillis();
  }

  public void enableFlameThrower() {
    this.flamethrowerEnabled = true;
    this.getActionSender().sendChatMessage("- &eFlame thrower enabled.");
  }

  public void disableFlameThrower() {
    World.getWorld().getLevel().clearFire(this, this.linePosition, this.lineRotation);
    this.flamethrowerEnabled = false;
    this.getActionSender().sendChatMessage("- &eFlame thrower disabled.");
  }

  public boolean isFlamethrowerEnabled() {
    return this.flamethrowerEnabled;
  }

  public void gotKill(Player defender) {
    if (defender.team == -1 || defender.team == team) {
      return;
    }

    kills++;
    killstreak++;
    Killstats.kill(this, defender);
    if (killstreak % 5 == 0) {
      World.getWorld()
          .broadcast("- " + getColoredName() + " &bhas a killstreak of " + killstreak + "!");
    }
    setIfMax("maxKillstreak", killstreak);
    if (duelPlayer == defender) {
      duelKills++;
      if (duelKills == 3) {
        World.getWorld()
            .broadcast(
                "- "
                    + getColoredName()
                    + " &bhas defeated "
                    + duelPlayer.getColoredName()
                    + " &bin a duel!");
        incStat("duelWins");
        duelPlayer.incStat("duelLosses");

        duelChallengedBy = null;

        duelPlayer.duelChallengedBy = null;

        duelPlayer.duelPlayer = null;
        duelPlayer = null;

        sendToTeamSpawn();
      }
    }
    KillLog.getInstance().logKill(this, defender);
  }

  public void follow(final Player p) {
    if (p == null && followThread != null) {
      follow.set(false);
    } else if (p != null) {
      if (followThread != null) {
        follow.set(false);
        followThread.interrupt();
        try {
          followThread.join(1);
        } catch (InterruptedException ex) {
          return;
        }
      }
      follow.set(true);
      followThread =
          new Thread(
              () -> {
                while (follow.get()) {
                  Position pos = p.getPosition();
                  Rotation r = p.getRotation();
                  getActionSender().sendTeleport(pos, r);
                  try {
                    Thread.sleep(1000);
                  } catch (InterruptedException ex) {
                  }
                }
              });
      followThread.start();
    }
  }

  public void died(Player attacker) {
    deaths++;
    if (killstreak >= 10) {
      World.getWorld()
          .broadcast(
              "- "
                  + attacker.getColoredName()
                  + " &bended "
                  + getColoredName()
                  + "&b's killstreak of "
                  + killstreak);
    }
    killstreak = 0;
    attacker.setIfMax("maxKillstreakEnded", killstreak);
    incStat("deaths");
    World.getWorld().getGameMode().checkForUnbalance(this);
    if (isFlamethrowerEnabled()) {
      disableFlameThrower();
    }
    if (this.bountyMode) {
      if (this.team == -1) {
        this.bountiedBy.addPoints(this.bountyAmount);
        this.bountied = null;
        this.bountiedBy = null;
        this.bountyMode = false;
      } else {
        if (this == attacker) {
          // nothing
        } else {
          if (attacker == this.bountiedBy) {
            // nothing
          } else {
            if (this.bountied == this) {
              attacker.bountyKills++;
              if (attacker.bountyKills == attacker.lastAmount + 5) {
                World.getWorld()
                    .broadcast(
                        "- "
                            + attacker.getColoredName()
                            + " &bhas collected "
                            + "the bounty of "
                            + this.bountyAmount
                            + "on "
                            + this.getColoredName()
                            + "!");
                attacker.addPoints(this.bountyAmount);
                this.bountied = null;
                this.bountiedBy = null;
                this.bountyMode = false;
                this.bountyAmount = 0;
                attacker.lastAmount = attacker.bountyKills;
              }
            }
          }
        }
      }
    }
  }

  public String getNameChar() {
    if (isOp()) {
      if (team == 0) {
        return "&4";
      } else if (team == 1) {
        return "&1";
      } else {
        return "&8";
      }
    } else {
      if (team == 0) {
        return "&c";
      } else if (team == 1) {
        return "&9";
      } else {
        return "&7";
      }
    }
  }

  public boolean isVIP() {
    return (!GameSettings.getBoolean("Tournament")
        && getAttribute("VIP") != null
        && getAttribute("VIP").equals("true"))
        || isOp();
  }

  public boolean isOp() {
    return (getAttribute("IsOperator") != null && getAttribute("IsOperator").equals("true"));
  }

  public String parseName() {
    return getNameChar() + name + "&e";
  }

  public void makeInvisible() {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (this != p) {
        p.getActionSender().sendRemoveEntity(instance);
      }
    }
  }

  public void makeVisible() {
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (this != p) {
        p.getActionSender().sendExtSpawn(instance);
      }
    }
  }

  public void autoJoinTeam() {
    GameMode gameMode = World.getWorld().getGameMode();
    String team;
    if (gameMode.redPlayers > gameMode.bluePlayers) {
      team = "blue";
    } else if (gameMode.bluePlayers > gameMode.redPlayers) {
      team = "red";
    } else {
      if (Math.random() < 0.5) {
        team = "red";
      } else {
        team = "blue";
      }
    }
    joinTeam(team);
  }

  public void joinTeam(String team) {
    joinTeam(team, true);
  }

  public void joinTeam(String team, boolean sendMessage) {
    if (!(team.equals("red") || team.equals("blue") || team.equals("spec"))) {
      return;
    }
    if (this.team == -1 && !team.equals("spec")) {
      getActionSender()
          .sendChatMessage(
              "- &aThis map was contributed by: " + World.getWorld().getLevel().getCreator());
    }
    if (isHidden && !team.equals("spec")) {
      Server.log(getName() + " is now unhidden");
      makeVisible();
      getActionSender().sendChatMessage("- &eYou are now visible");
      isHidden = false;
    }
    Level l = World.getWorld().getLevel();
    GameMode gameMode = World.getWorld().getGameMode();
    if (gameMode.voting) {
      return;
    }
    if (this.team == 0) {
      gameMode.redPlayers--;
    } else if (this.team == 1) {
      gameMode.bluePlayers--;
    }
    int diff = gameMode.redPlayers - gameMode.bluePlayers;
    boolean unbalanced = false;
    if (!GameSettings.getBoolean("Tournament")) {
      if (diff >= 1 && team.equals("red")) {
        unbalanced = true;
      } else if (diff <= -1 && team.equals("blue")) {
        unbalanced = true;
      }
    }
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p != this) {
        p.getActionSender().sendRemovePlayer(this);
      }
    }
    gameMode.playerChangedTeam(this);
    if (team.equals("red")) {
      if (this.team == -1) {
        makeVisible();
      }
      if (unbalanced && gameMode.redPlayers > gameMode.bluePlayers) {
        gameMode.bluePlayers++;
        this.team = 1;
        team = "blue";
        getActionSender().sendChatMessage("- Red team is full.");
      } else {
        gameMode.redPlayers++;
        this.team = 0;
      }
    } else if (team.equals("blue")) {
      if (this.team == -1) {
        makeVisible();
      }
      if (unbalanced && gameMode.bluePlayers > gameMode.redPlayers) {
        gameMode.redPlayers++;
        this.team = 0;
        team = "red";
        this.getActionSender().sendChatMessage("- Blue team is full.");
      } else {
        gameMode.bluePlayers++;
        this.team = 1;
      }
    } else {
      this.team = -1;
      if (duelPlayer != null) {
        duelPlayer.duelPlayer = null;
        duelPlayer = null;
      }
    }
    clearMines();
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      if (p.canSee(this)) {
        p.getActionSender().sendAddPlayer(this, p == this);
      }

      if (p == this) {
        continue;
      }

      if (!canSee(p)) {
        getActionSender().sendRemoveEntity(p);
      } else {
        getActionSender().sendExtSpawn(p);
      }
    }

    if (sendMessage) {
      World.getWorld().broadcast("- " + parseName() + " joined the " + team + " team");
    }

    session.getActionSender().sendHackControl(Configuration.getConfiguration().isTest() || this.team == -1);

    Position position = getTeamSpawn();
    getActionSender().sendTeleport(position, getTeamSpawnRotation());
    setPosition(position);

    if (isNewPlayer) {
      setAttribute("rules", "true");
      isNewPlayer = false;
    }
  }

  public void setInt(String a, int value) {
    setAttribute(a, value);
  }

  public void setIfMax(String a, int value) {
    if (value > getInt(a)) {
      setInt(a, value);
    }
  }

  public int getInt(String a) {
    return (Integer) getAttribute(a);
  }

  public void incStat(String a) {
    if (getAttribute(a) == null) {
      setAttribute(a, 0);
    }
    setAttribute(a, (Integer) getAttribute(a) + 1);
  }

  public void addPoints(int n) {
    currentRoundPointsEarned += n;
    currentRoundPoints += n;
  }

  public int getPoints() {
    return currentRoundPoints;
  }

  public void setPoints(int n) {
    currentRoundPoints = n;
  }

  public void subtractPoints(int n) {
    currentRoundPoints -= n;
  }

  public void kickForHacking() {
    getActionSender().sendLoginFailure("You were kicked for hacking!");
    getSession().close();
    World.getWorld().broadcast("- &e" + getName() + " was kicked for hacking!");
  }

  public void sendToTeamSpawn() {
    // If player dies while flamethrower is on, don't leave remnants on the map.
    if (isFlamethrowerEnabled()) {
      World.getWorld().getLevel().clearFire(this, linePosition, lineRotation);
    }
    getActionSender().sendTeleport(getTeamSpawn(), new Rotation(team == 0 ? 64 : 192, 0));
  }

  public Position getTeamSpawn() {
    if (World.getWorld().getLevel().mode == Level.TDM) {
      return World.getWorld().getLevel().getTDMSpawn();
    }
    switch (team) {
      case 0:
        return World.getWorld().getLevel().redSpawnPosition;
      case 1:
        return World.getWorld().getLevel().blueSpawnPosition;
      case -1:
        return Math.random() < 0.5
            ? World.getWorld().getLevel().redSpawnPosition
            : World.getWorld().getLevel().blueSpawnPosition;
      default:
        return null;
    }
  }

  public Rotation getTeamSpawnRotation() {
    switch (team) {
      case 0:
        return World.getWorld().getLevel().redSpawnRotation;
      case 1:
        return World.getWorld().getLevel().blueSpawnRotation;
      case -1:
        return Math.random() < 0.5
            ? World.getWorld().getLevel().redSpawnRotation
            : World.getWorld().getLevel().blueSpawnRotation;
      default:
        return null;
    }
  }

  public String getSkinUrl() {
    return null;
  }

  /**
   * Sets an attribute of this player.
   *
   * @param name  The name of the attribute.
   * @param value The value of the attribute.
   * @return The old value of the attribute, or <code>null</code> if there was no previous attribute
   * with that name.
   */
  public Object setAttribute(String name, Object value) {
    return attributes.put(name, value);
  }

  /**
   * Gets an attribute.
   *
   * @param name The name of the attribute.
   * @return The attribute, or <code>null</code> if there is not an attribute with that name.
   */
  public Object getAttribute(String name) {
    return attributes.get(name);
  }

  /**
   * Checks if an attribute is set.
   *
   * @param name The name of the attribute.
   * @return <code>true</code> if set, <code>false</code> if not.
   */
  public boolean isAttributeSet(String name) {
    return attributes.containsKey(name);
  }

  /**
   * Removes an attribute.
   *
   * @param name The name of the attribute.
   * @return The old value of the attribute, or <code>null</code> if an attribute with that name did
   * not exist.
   */
  public Object removeAttribute(String name) {
    return attributes.remove(name);
  }

  @Override
  public String getName() {
    return name;
  }

  public String getColoredName() {
    return getNameChar() + name;
  }

  public String getListName() {
    String playerHasFlag = hasFlag ? "&6[!] " : "";

    String playerSuffix = "";
    if (AFK) {
      playerSuffix = "    &7(&bAFK&7)";
    } else if (muted) {
      playerSuffix = "    &7(&bMuted&7)";
    }
    if (AFK && muted) {
      playerSuffix = "    &7(&bAFK, Muted&7)";
    }

    String listName =
        playerHasFlag + getColoredName() + "    &f" + currentRoundPoints + playerSuffix;
    return listName.substring(0, Math.min(64, listName.length()));
  }

  public String getTeamName() {
    if (team == 0) {
      return "&cRed";
    } else if (team == 1) {
      return "&9Blue";
    } else {
      return "&7Spectators";
    }
  }

  /**
   * Gets the player's session.
   *
   * @return The session.
   */
  public MinecraftSession getSession() {
    return session;
  }

  /**
   * Gets this player's action sender.
   *
   * @return The action sender.
   */
  public ActionSender getActionSender() {
    if (session != null) {
      return session.getActionSender();
    } else {
      return actionSender;
    }
  }

  public void setActionSender(ActionSender actionSender) {
    this.actionSender = actionSender;
  }

  /**
   * Gets the attributes map.
   *
   * @return The attributes map.
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void step(int ticks) {
    if (World.getWorld().getPlayerList().size() >= Configuration.getConfiguration()
        .getMaximumPlayers()) {
      if (System.currentTimeMillis() - moveTime < 100 && AFK) {
        World.getWorld().broadcast("- " + parseName() + " is no longer AFK");
        AFK = false;
      }
      // TODO: Find a way to check if player is still AFK after changing levels
      if (System.currentTimeMillis() - moveTime > 10 * 60000 && !AFK && moveTime != 0) {
        World.getWorld().broadcast("- " + parseName() + " is auto-AFK (Not moved in 10 minutes)");
        AFK = true;
      } else if (System.currentTimeMillis() - moveTime > 60 * 60000 && AFK && moveTime != 0) {
        World.getWorld().broadcast("- " + parseName() + " was auto-kicked (AFK for 60 minutes)");
        getActionSender().sendLoginFailure("You were auto-kicked for being AFK for 60+ minutes.");
        getSession().close();
      }
    }

    World.getWorld().getGameMode().processPlayerMove(this);
    if (isFlamethrowerEnabled()) {
      int duration = GameSettings.getInt("FlameThrowerDuration");
      // ticks a second
      float rate = (float) Constants.FLAME_THROWER_FUEL / duration;
      long time = System.currentTimeMillis();
      long dt = time - flamethrowerTime;
      // Rate in seconds, dt in milliseconds
      flamethrowerFuel -= rate * dt / 1000;
      flamethrowerTime = time;
      if (flamethrowerFuel <= 0) { // Out of fuel
        disableFlameThrower();
        flamethrowerFuel = 0;
      }
      // Was flame thrower disabled because they ran out of fuel?
      if (isFlamethrowerEnabled()) {
        if (!getPosition().equals(linePosition)
            || !getRotation().equals(lineRotation)) {
          if (linePosition != null) {
            World.getWorld().getLevel().clearFire(this, linePosition, lineRotation);
          }
          World.getWorld().getLevel().drawFire(this, getPosition(), getRotation());
          linePosition = getPosition();
          lineRotation = getRotation();
        }
        ((CTFGameMode) World.getWorld()
            .getGameMode())
            .processFlamethrower(this, linePosition, lineRotation);
      }
    } else {
      if (flamethrowerFuel != (float) Constants.FLAME_THROWER_FUEL) {
        int chargeTime = GameSettings.getInt("FlameThrowerRechargeTime");
        float rechargeRate = (float) Constants.FLAME_THROWER_FUEL / chargeTime;
        long time = System.currentTimeMillis();
        long dt = time - flamethrowerTime;
        // Recharge rate in seconds, dt in milliseconds
        flamethrowerFuel += rechargeRate * dt / 1000;
        flamethrowerTime = time;
        if (flamethrowerFuel >= Constants.FLAME_THROWER_FUEL) {
          flamethrowerFuel = Constants.FLAME_THROWER_FUEL;
          getActionSender().sendChatMessage("- &eFlame thrower charged.");
        }
      }
    }
    ui.setFlamethrower(Math.round(flamethrowerFuel));

      /* if(hasFlag) {
          headBlockType = team == 0 ? 28 : 21;
      }
      else */
    if (duelPlayer != null) {
      headBlockType = 41;
    } else {
      headBlockType = 0;
    }
    if (headBlockType != 0) {
      Position blockPos = getPosition().toBlockPos();
      Position newPosition = new Position(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 3);
      if (!newPosition.equals(headBlockPosition)) {
        if (headBlockPosition != null) {
          World.getWorld().getLevel().setBlock(headBlockPosition, 0);
        }
        if (World.getWorld().getLevel().getBlock(newPosition) == 0) {
          headBlockPosition = newPosition;
          World.getWorld().getLevel().setBlock(headBlockPosition, headBlockType);
        } else {
          headBlockPosition = null;
        }
      }
    } else if (headBlockPosition != null) {
      World.getWorld().getLevel().setBlock(headBlockPosition, 0);
    }
    ui.step(ticks);
  }

  public boolean canKill(Player p, boolean sendMessage) {
    if (duelPlayer != null && p != duelPlayer) {
      if (sendMessage) {
        getActionSender()
            .sendChatMessage(
                "- &eYou can't kill "
                    + p.parseName()
                    + " since you are"
                    + " dueling "
                    + duelPlayer.parseName()
                    + ". Only they can hurt you right now.");
      }
      return false;
    } else if (duelPlayer == null && p.duelPlayer != null) {
      if (sendMessage) {
        getActionSender()
            .sendChatMessage(
                "- &eYou can't kill "
                    + p.parseName()
                    + " since they "
                    + "are dueling "
                    + p.duelPlayer.parseName()
                    + ". They can't capture your flag or kill "
                    + "anyone else right now.");
      }
      return false;
    } else {
      // no point in sending a message anymore as specs are invisible
      return p.team != -1;
    }
  }

  public void markSafe() {
    safeTime = System.currentTimeMillis();
  }

  public boolean isSafe() {
    return System.currentTimeMillis() - safeTime < Constants.SAFE_TIME;
  }
}
