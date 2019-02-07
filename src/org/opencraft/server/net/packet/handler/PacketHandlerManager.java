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
package org.opencraft.server.net.packet.handler;

import org.opencraft.server.Server;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.OCSession;
import org.opencraft.server.net.packet.Packet;

import java.util.Map;
import org.opencraft.server.game.impl.GameSettings;

/**
 * A class which manages <code>PacketHandler</code>s.
 *
 * @author Graham Edgecombe
 */
public class PacketHandlerManager<SessionType extends OCSession> {

  /** An array of packet handlers. */
  private PacketHandler[] handlers = new PacketHandler[256];

  /** Default private constructor. */
  @SuppressWarnings("unchecked")
  protected PacketHandlerManager(Map<Integer, String> map) {
    try {
      Map<Integer, String> handlers = map;
      for (Map.Entry<Integer, String> handler : handlers.entrySet()) {
        this.handlers[handler.getKey()] =
            (PacketHandler) Class.forName(handler.getValue()).newInstance();
      }
    } catch (Exception ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  /**
   * Handles a packet.
   *
   * @param session The session.
   * @param packet The packet.
   */
  public void handlePacket(SessionType session, Packet packet) {
    PacketHandler handler = handlers[packet.getDefinition().getOpcode()];
    if (GameSettings.getBoolean("Debug")
        && packet.definition.getOpcode() != 8
        && packet.definition.getOpcode() != 43) {
      Server.d(packet.definition.getOpcode() + " <--  " + ((MinecraftSession) session).getIP());
      Server.d(packet.toString());
    }
    if (handler != null) {
      handler.handlePacket(session, packet);
    } else {
      Server.log("[E] Unhandled packet : " + packet + ".");
    }
  }
}
