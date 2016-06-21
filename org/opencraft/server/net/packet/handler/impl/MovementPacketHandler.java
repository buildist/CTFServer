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

import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.model.MoveLog;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUntagger;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.World;
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

    Position blockPosition = new Position(x, y, z).toBlockPos();

    int dx = Math.abs(x - oldX);
    int dy = Math.abs(y - oldY);
    int dz = Math.abs(z - oldZ);
    if ((dx > 400 || dy > 400 || dz > 400)) //respawning
    {
      if (player.team > -1) {
        if (player.hasFlag)
          ((CTFGameMode) World.getWorld().getGameMode()).dropFlag(player, true, false);
                /*if(!(blockPosition.getX() == player.teleportBlockPosition.getX() &&
                blockPosition.getY() == player.teleportBlockPosition.getY())) {
                    player.getActionSender().sendTeleport(player.getPosition(), player
                    .getRotation());
                    return;
                }*/
      }
    } else if (player.frozen) { //frozen
      player.getActionSender().sendTeleport(player.getPosition(), player.getRotation());
      return;
    } else if (player.respawning) {
      double distanceFromSpawn = blockPosition.dist2(player.teleportBlockPosition); //team spawn
      double distanceFromRespawn = blockPosition.dist2(player.respawnPosition); //where they were
      // killed
      if (distanceFromSpawn < distanceFromRespawn) {
        player.respawning = false;
        System.out.println(player.getName() + " respawned");
      }
    }

    if (dx != 0 || dy != 0 || dz != 0) { //for AFK kick
      player.moveTime = System.currentTimeMillis();
    }

    //kill floor
    if ((z - 16) / 32 < World.getWorld().getLevel().floor && !player.safe) {
      if (World.getWorld().getLevel().id.equals("tdm_abyss")) {
        player.safe = true;
        new Thread(new PlayerUntagger(player)).start();
        player.getActionSender().sendTeleport(new Position(player.getPosition().getX(), player
            .getPosition().getY(), 128 * 32 + 16), player.getRotation());
      } else {
        player.safe = true;
        new Thread(new PlayerUntagger(player)).start();
        World.getWorld().broadcast("- " + player.parseName() + " died!");
        player.sendToTeamSpawn();
        if (player.hasFlag) {
          CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
          ctf.dropFlag(player, true, false);
        }
      }
    }
    final int rotation = packet.getNumericField("rotation").intValue();
    final int look = packet.getNumericField("look").intValue();
    player.setPosition(new Position(x, y, z));
    player.setRotation(new Rotation(rotation, look));
    MoveLog.getInstance().logPosition(player);
    if (session.isExtensionSupported("HeldBlock"))
      player.heldBlock = packet.getNumericField("held_block").byteValue();
  }

}