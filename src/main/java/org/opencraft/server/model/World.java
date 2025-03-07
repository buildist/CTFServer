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

import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.game.GameMode;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.heartbeat.HeartbeatManager;
import org.opencraft.server.io.LevelGzipper;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.persistence.LoadPersistenceRequest;
import org.opencraft.server.persistence.SavePersistenceRequest;
import org.opencraft.server.persistence.SavedGameManager;
import org.opencraft.server.util.PlayerList;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Manages the in-game world.
 *
 * @author Graham Edgecombe
 */
public final class World {

  /** The singleton instance. */
  private static final World INSTANCE;

  /** Static initializer. */
  static {
    World w = null;
    try {
      w = new World();
    } catch (Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
    INSTANCE = w;
    w.gameMode.startGame(null);
  }

  /** The player list. */
  private final PlayerList playerList = new PlayerList();

  private ArrayList<Mine> mines = new ArrayList<>(64);
  private ArrayList<SmokeZone> smokeZones = new ArrayList<>(64);

  /** The level. */
  private Level level;
  /** The game mode. */
  private GameMode gameMode;

  /** Default private constructor. */
  private World() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    gameMode = new CTFGameMode();
  }

  /**
   * Gets the world instance.
   *
   * @return The world instance.
   */
  public static World getWorld() {
    return INSTANCE;
  }

  public void addMine(Mine m) {
    mines.add(m);
  }

  public void removeMine(Mine m) {
    mines.remove(m);
  }

  public void clearMines() {
    mines.clear();
  }

  public Mine getMine(int x, int y, int z) {
    for (Mine m : mines) {
      if ((m.x - 16) / 32 == x && (m.y - 16) / 32 == y && (m.z - 16) / 32 == z) return m;
    }
    return null;
  }

  public Iterable<Mine> getAllMines() {
    return new ArrayList<>(mines);
  }

  public void addSmokeZone(SmokeZone z) {
    smokeZones.add(z);
  }

  public void removeSmokeZone(SmokeZone z) {
    smokeZones.remove(z);

    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
        player.getSession().getActionSender().sendRemoveSelectionCuboid(z.id);

      int minX = z.minX;
      int minZ = z.minZ;
      int minY = z.minY;

      int maxX = z.maxX;
      int maxZ = z.maxZ;
      int maxY = z.maxY;

      // If player is within the zone boundaries at the time of removal
      if ((player.getPosition().getX() / 32 >= minX && player.getPosition().getX() / 32 <= maxX)
              && (player.getPosition().getZ() / 32 >= minZ && player.getPosition().getZ() / 32 <= maxZ)
              && (player.getPosition().getY() / 32 >= minY && player.getPosition().getY() / 32 <= maxY)) {
        player.getActionSender().sendMapProperty(4, World.getWorld().getLevel().viewDistance);
        player.getActionSender().sendMapColor(2, Constants.DEFAULT_COLORS[2][0], Constants.DEFAULT_COLORS[2][1], Constants.DEFAULT_COLORS[2][2]);

        short[][] colors = World.getWorld().getLevel().colors;
        if (colors[2][0] == -1) {
          player.getActionSender().sendMapColor(2, Constants.DEFAULT_COLORS[2][0], Constants.DEFAULT_COLORS[2][1], Constants.DEFAULT_COLORS[2][2]);
        } else {
          player.getActionSender().sendMapColor(2, colors[2][0], colors[2][1], colors[2][2]);
        }

        player.isInSmokeZone = false;
      }
    }
  }

  public void clearSmokeZones() {
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      for (SmokeZone z : smokeZones) {
        player.getSession().getActionSender().sendRemoveSelectionCuboid(z.id);
      }
    }

    smokeZones.clear();
  }

  public Iterable<SmokeZone> getAllSmokeZones() {
    return new ArrayList<>(smokeZones);
  }

  public int getNumberOfSmokeZones() {
    return smokeZones.size();
  }

  /**
   * Gets the current game mode.
   *
   * @return The current game mode.
   */
  public GameMode getGameMode() {
    return gameMode;
  }

  /**
   * Gets the player list.
   *
   * @return The player list.
   */
  public PlayerList getPlayerList() {
    return playerList;
  }

  /**
   * Gets the level.
   *
   * @return The level.
   */
  public Level getLevel() {
    return level;
  }

  public void setLevel(Level l) {
    if (level != null) {
      // Clear solid types for next map
      for (int id : level.usedSolidTypes) {
        for (Player p : getPlayerList().getPlayers()) {
          p.getActionSender().sendBlockPermissions(id, true, true);
        }
      }
    }

    level = l;
    BlockLog.clear();
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      LevelGzipper.getLevelGzipper().gzipLevel(player.getSession());
    }
  }

  /**
   * Registers a session.
   *
   * @param session The session.
   * @param username The username.
   * @param verificationKey The verification key.
   */
  public void register(MinecraftSession session, String username, String verificationKey) {

    if (Server.isIPBanned(session.getIP())) {
      session.getActionSender().sendLoginFailure("You're banned!");
      session.close();
      return;
    } else if (Configuration.getConfiguration().isVerifyingNames()) {
      Server.d(("Verifying " + username));
      long salt = HeartbeatManager.getHeartbeatManager().getSalt();
      String hash = new StringBuilder().append(String.valueOf(salt)).append(username).toString();
      MessageDigest digest;
      try {
        digest = MessageDigest.getInstance("MD5");
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("No MD5 algorithm!");
      }
      digest.update(hash.getBytes());
      byte[] bytes = digest.digest();
      String hex = new BigInteger(1, bytes).toString(16);
      if (!verificationKey.equals(hex) && !verificationKey.equals('0' + hex)) {
        session.getActionSender().sendLoginFailure("Login failed, please try again");
        session.close();
        return;
      }
    }

    if (username.equals("ChurroS")) {
      username = "Matt";
    }

    // Check if name is valid
    char[] nameChars = username.toCharArray();
    for (char nameChar : nameChars) {
      if (nameChar < ' ' || nameChar > '\177') {
        session.getActionSender().sendLoginFailure("Invalid name!");
        session.close();
        return;
      }
    }

    // Disconnect any existing players with the same name
    for (Player p : playerList.getPlayers()) {
      if (p.getName().equalsIgnoreCase(username)) {
        p.getSession().getActionSender().sendLoginFailure("Logged in from another computer.");
        break;
      }
    }
    // Attempt to add the player
    final Player player = new Player(session, username);

    if (!playerList.add(player)) {
      player.getSession().getActionSender().sendLoginFailure("The server is full!");
      session.close();
      return;
    }

    // Final setup
    session.setPlayer(player);
    final Configuration c = Configuration.getConfiguration();
    boolean op = false;
    try {
      new LoadPersistenceRequest(player).perform();
    } catch (IOException ex) {
      Server.log(ex);
    }

    if (player.isOp()) op = true;
    else op = false;
    if (player.getAttribute("rules") == null) player.isNewPlayer = true;
    if (player.getAttribute("banned") != null && player.getAttribute("banned").equals("true")) {
      session.close();
      return;
    }

    // If whitelist is enabled, only allow whitelisted players and OPs/VIPs
    if (GameSettings.getBoolean("Whitelist")) {
      if (!Server.isWhitelisted(username) && !player.isOp() && !player.isVIP()) {
        player.getSession().getActionSender().sendLoginFailure(GameSettings.getString("WhitelistMessage"));
        session.close();
        return;
      }
    }

    session.getActionSender().sendLoginResponse(
        Constants.PROTOCOL_VERSION, c.getName(), c.getMessage() + "&0-hax -push" + level.getMotd(), op);
    if (!session.isExtensionSupported("HackControl")) {
      session
          .getActionSender()
          .sendLoginFailure("Enable \"Enhanced\" mode in CC launcher settings to play");
      session.close();
      return;
    }
    session.getActionSender().sendHackControl(true);
    LevelGzipper.getLevelGzipper().gzipLevel(session);
  }

  /**
   * Unregisters a session.
   *
   * @param session The session.
   */
  public void unregister(MinecraftSession session) {
    if (session.isAuthenticated()) {
      playerList.remove(session.getPlayer());
      World.getWorld().getGameMode().playerDisconnected(session.getPlayer());
      SavedGameManager.getSavedGameManager()
          .queuePersistenceRequest(new SavePersistenceRequest(session.getPlayer()));
      session.setPlayer(null);
    }
  }

  /**
   * Completes registration of a session.
   *
   * @param session The session.
   */
  public void completeRegistration(MinecraftSession session) {
    if (!session.isAuthenticated()) {
      session.close();
      return;
    }

    if (session.isExtensionSupported("InventoryOrder")) {
      if (!BlockManager.getBlockManager().isCustom(BlockConstants.TNT)) {
        session.getActionSender().sendInventoryOrder(BlockConstants.TNT, 0);
      }
    }

    // Notify game mode
    World.getWorld().getGameMode().playerConnected(session.getPlayer());

    // Add default blocks into the hotbar
    if (!session.levelSent && session.isExtensionSupported("HeldBlock")) {
      session.getActionSender().sendHotbar((short) Constants.BLOCK_TNT_RED, 0);
      session.getActionSender().sendHotbar((short) Constants.BLOCK_DETONATOR, 1);
      session.getActionSender().sendHotbar((short) Constants.BLOCK_MINE, 2);
      session.getActionSender().sendHotbar((short) 1, 3);
      session.getActionSender().sendHotbar((short) Constants.BLOCK_VINE, 4);
      session.getActionSender().sendHotbar((short) 20, 5);
      session.getActionSender().sendHotbar((short) 45, 6);
      session.getActionSender().sendHotbar((short) 21, 7);
      session.getActionSender().sendHotbar((short) 29, 8);
    }

    // If applicable, show where the spawn zones are
    // Min must start be the lowest coords (e.g, 0,0,0) and max must be the highest coords (e.g, 128,128,128)
    // End result as sourced from wiki: { EndX-StartX+1, EndY-StartY+1, EndZ-StartZ+1 }

    if (level.redSpawnZoneMin != null && level.redSpawnZoneMax != null) {
      session.getActionSender().sendSelectionCuboid(
              0,
              "RedSpawnZone",
              (short) (level.redSpawnZoneMin.getX() / 32),
              (short) ((level.redSpawnZoneMin.getZ() / 32) - 1),
              (short) (level.redSpawnZoneMin.getY() / 32),
              (short) ((level.redSpawnZoneMax.getX() / 32) + 1),
              (short) (level.redSpawnZoneMax.getZ() / 32),
              (short) ((level.redSpawnZoneMax.getY() / 32) + 1),
              (short) 255,
              (short) 0,
              (short) 0,
              (short) 30
      );
    }

    if (level.blueSpawnZoneMin != null && level.blueSpawnZoneMax != null) {
      session.getActionSender().sendSelectionCuboid(
              1,
              "BlueSpawnZone",
              (short) (level.blueSpawnZoneMin.getX() / 32),
              (short) ((level.blueSpawnZoneMin.getZ() / 32) - 1),
              (short) (level.blueSpawnZoneMin.getY() / 32),
              (short) ((level.blueSpawnZoneMax.getX() / 32) + 1),
              (short) (level.blueSpawnZoneMax.getZ() / 32),
              (short) ((level.blueSpawnZoneMax.getY() / 32) + 1),
              (short) 0,
              (short) 0,
              (short) 255,
              (short) 30
      );
    }

    // Player has finished loading
    session.levelSent = true;
  }

  /**
   * Broadcasts a chat message.
   *
   * @param player The source player.
   * @param message The message.
   */
  public void broadcast(Player player, String message) {
    for (Player otherPlayer : playerList.getPlayers()) {
      otherPlayer.getSession().getActionSender().sendChatMessage(message);
    }
  }

  public void broadcastOp(String message) {
    for (Player otherPlayer : playerList.getPlayers()) {
      if (otherPlayer.isOp()) otherPlayer.getSession().getActionSender().sendChatMessage(message);
    }
  }

  public void broadcast(String message) {
    for (Player player : playerList.getPlayers()) {
      player.getSession().getActionSender().sendChatMessage(message);
    }
  }

  public void sendPM(Player player, Player other, String text) {
    if (!other.isIgnored(player)) {
      if (!text.equals("")) {
        other.getActionSender().sendChatMessage("&5(PM) " + player.parseName() + ">&f" + text);
        player.getActionSender().sendChatMessage("&5-->" + other.parseName() + ">&f" + text);
        Server.log(player.getName() + "-->" + other.getName() + ": " + text);
      } else player.getActionSender().sendChatMessage("- &ePlease include a message.");
    }
  }

  public void sendOpChat(Player player, String text) {
    Server.log(player.getName() + " [opchat]:  " + text);
    for (Player t : World.getWorld().getPlayerList().getPlayers()) {
      if (t.isOp()) {
        t.getActionSender().sendChatMessage("[OP] " + player.getColoredName() + "&f: " + text);
      }
    }
  }

  public void sendTeamChat(Player player, String text) {
    if (!player.muted) {
      Server.log(player.getName() + " [team]:  " + text);
      for (Player t : World.getWorld().getPlayerList().getPlayers()) {
        if (t.team == player.team && !t.isIgnored(player)) {
          t.getActionSender().sendChatMessage("[Team] " + player.getColoredName() + "&f: " + text);
        }
      }
    }
  }

  public void sendChat(Player player, String message) {
    String chr = player.getNameChar();
    if (player.isVIP() && !player.isOp()) {
      chr += "[VIP] ";
    }
    for (Player t : World.getWorld().getPlayerList().getPlayers()) {
      if (!t.isIgnored(player)) {
        t.getActionSender().sendChatMessage(chr + player.getName() + "&f: " + message);
      }
    }
  }
}
