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
package org.opencraft.server.net.packet.handler.impl;

import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.model.*;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

/**
 * A packet handler which handles movement packets.
 *
 * @author Graham Edgecombe
 */
public class MovementPacketHandler implements PacketHandler<MinecraftSession> {
  @Override
  public void handlePacket(MinecraftSession session, Packet packet) {
    session.getPlayer().lastPacketTime = System.currentTimeMillis();
    if (!session.isAuthenticated()) {
      return;
    }

    final Player player = session.getPlayer();
    Position oldPosition = player.getPosition();

    final int oldX = oldPosition.getX();
    final int oldY = oldPosition.getY();
    final int oldZ = oldPosition.getZ();
    final int x = packet.getNumericField("x").intValue();
    final int y = packet.getNumericField("y").intValue();
    final int z = packet.getNumericField("z").intValue();

    Position position = new Position(x, y, z);
    Position blockPosition = position.toBlockPos();

    int dx = Math.abs(x - oldX);
    int dy = Math.abs(y - oldY);
    int dz = Math.abs(z - oldZ);
    if ((dx > 400 || dy > 400 || dz > 400)) // respawning
    {
      World.getWorld().getGameMode().playerRespawn(player);
    } else if (player.frozen) { // frozen
      player.getActionSender().sendTeleport(player.getPosition(), player.getRotation());
      return;
    }

    if (dx != 0 || dy != 0 || dz != 0) { // for AFK kick
      player.moveTime = System.currentTimeMillis();
    }

    for (SmokeZone zone : World.getWorld().getAllSmokeZones()) {
      int minX = zone.minX;
      int minZ = zone.minZ;
      int minY = zone.minY;

      int maxX = zone.maxX - 1;
      int maxZ = zone.maxZ;
      int maxY = zone.maxY - 1;

      // If player is within the zone boundaries
      if ((x / 32 >= minX && x / 32 <= maxX)
              && (z / 32 >= minZ && z / 32 <= maxZ)
              && (y / 32 >= minY && y / 32 <= maxY)) {
                short fogDensity = 0;
                if (zone.density == 255) fogDensity = 1;
                if (zone.density == 191) fogDensity = 7;
                if (zone.density == 127) fogDensity = 14;
                if (zone.density == 64) fogDensity = 28;

                player.getActionSender().sendMapProperty(4, fogDensity);
                player.getActionSender().sendMapColor(2, (short)34, (short)34, (short)34);
                player.isInSmokeZone = true;
      } else {
        if (player.isInSmokeZone) {
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

    // Kill floor
    boolean belowKillFloor = (z - 16) / 32 < World.getWorld().getLevel().floor;
    if (belowKillFloor && !player.isSafe()) {
      player.markSafe();
      World.getWorld().broadcast("- " + player.parseName() + " died!");
      player.sendToTeamSpawn();
      if (World.getWorld().getGameMode() instanceof  CTFGameMode && player.hasFlag) {
        CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
        ctf.dropFlag(player, true, false);
      }
    }
    boolean isOnGround = World.getWorld().getLevel().getBlock(
        blockPosition.getX(), blockPosition.getY(), blockPosition.getZ() - 2) > 0;
    if (isOnGround && !belowKillFloor) {
      player.safePosition = position;
    }

    // Check if the player has entered either spawn zones with the flag
    if (player.hasFlag) {
      Level level = World.getWorld().getLevel();

      if (player.team == 0 && level.redSpawnZoneMin != null && level.redSpawnZoneMax != null) {
        int minX = level.redSpawnZoneMin.getX() - 32;
        int minZ = level.redSpawnZoneMin.getZ();
        int minY = level.redSpawnZoneMin.getY() - 32;

        int maxX = level.redSpawnZoneMax.getX() + 32;
        int maxZ = level.redSpawnZoneMax.getZ();
        int maxY = level.redSpawnZoneMax.getY() + 32;

        // If player is within the zone boundaries
        if ((x >= minX && x <= maxX)
                && (z >= minZ && z <= maxZ)
                && (y >= minY && y <= maxY)) {
          player.isLegal = false;

          player.getActionSender().sendTeleport(new Position(player.lastLegalPosition.getX(), player.lastLegalPosition.getY(), player.getPosition().getZ()), player.getRotation());
          player.getActionSender().sendChatMessage("&cGo back!", 101);
          player.getActionSender().sendChatMessage("&7You may not enter spawn with the flag.", 102);

          return;
        } else if (!player.isLegal) {
          player.getActionSender().sendChatMessage("", 101);
          player.getActionSender().sendChatMessage("", 102);
          player.isLegal = true;
        }
      }

      else if (player.team == 1 && level.blueSpawnZoneMin != null && level.blueSpawnZoneMax != null) {
        int minX = level.blueSpawnZoneMin.getX() - 32;
        int minZ = level.blueSpawnZoneMin.getZ();
        int minY = level.blueSpawnZoneMin.getY() - 32;

        int maxX = level.blueSpawnZoneMax.getX() + 32;
        int maxZ = level.blueSpawnZoneMax.getZ();
        int maxY = level.blueSpawnZoneMax.getY() + 32;

        // If player is within the zone boundaries
        if ((x >= minX && x <= maxX)
                && (z >= minZ && z <= maxZ)
                && (y >= minY && y <= maxY)) {
          player.isLegal = false;

          player.getActionSender().sendTeleport(new Position(player.lastLegalPosition.getX(), player.lastLegalPosition.getY(), player.getPosition().getZ()), player.getRotation());
          player.getActionSender().sendChatMessage("&cGo back!", 101);
          player.getActionSender().sendChatMessage("&7You may not enter spawn with the flag.", 102);

          return;
        } else if (!player.isLegal) {
          player.getActionSender().sendChatMessage("", 101);
          player.getActionSender().sendChatMessage("", 102);
          player.isLegal = true;
        }
      }
    }

    player.lastLegalPosition = position;

    final int rotation = packet.getNumericField("rotation").intValue();
    final int look = packet.getNumericField("look").intValue();
    player.setPosition(position);
    player.setRotation(new Rotation(rotation, look));
    MoveLog.getInstance().logPosition(player);
    if (session.isExtensionSupported("HeldBlock"))
      player.heldBlock = packet.getNumericField("id").byteValue();
  }
}
