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


import org.opencraft.server.Server;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

import java.util.Date;

/**
 * A packet handler which handles the construction packet.
 *
 * @author Graham Edgecombe
 */
public class ConstructionPacketHandler implements PacketHandler<MinecraftSession> {
  @Override
  public void handlePacket(MinecraftSession session, Packet packet) {
    if (!session.isAuthenticated()) {
      return;
    }
    Player p = session.getPlayer();
    long time = new Date().getTime() - p.lastBlockTimestamp;
    int type = Server.getUnsigned(packet.getNumericField("type").intValue());
    if (time < 5 && type == 22)
      p.kickForHacking();
    else {
      int x = packet.getNumericField("x").intValue();
      int y = packet.getNumericField("y").intValue();
      int z = packet.getNumericField("z").intValue();
      int mode = packet.getNumericField("mode").intValue();
      World.getWorld().getGameMode().setBlock(p, World.getWorld().getLevel(), x, y, z, mode, type);
    }
    p.lastBlockTimestamp = new Date().getTime();
  }
}
