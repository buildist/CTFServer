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
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A class which handles message and comamnd packets.
 *
 * @author Graham Edgecombe
 */
public class MessagePacketHandler implements PacketHandler<MinecraftSession> {

  @Override
  public void handlePacket(MinecraftSession session, Packet packet) {
    if (!session.isAuthenticated()) {
      return;
    }
    String message = packet.getStringField("message");
    int id = packet.getNumericField("id").byteValue();
    if (message.contains("&")) {
      session.getPlayer().kickForHacking();
      return;
    }

    if (message.charAt(message.length() - 1) == '>'
        || message.charAt(message.length() - 1) == '<') {
      message = message.substring(0, message.length() - 1);
      session.getPlayer().appendingChat = true;
      session.getPlayer().partialChatMessage += message + " ";
      session.getActionSender().sendChatMessage("&7" + message);
      return;
    } else if(id == 1) {
      session.getPlayer().appendingChat = true;
      session.getPlayer().partialChatMessage += message;
      return;
    } else if (session.getPlayer().appendingChat) {
      message = session.getPlayer().partialChatMessage + message;
      session.getPlayer().partialChatMessage = "";
    }

    message = Server.cleanColorCodes(message);
    if (message.startsWith("/")) {
      // interpret as command
      String tokens = message.substring(1);
      String[] parts = tokens.split(" ");
      final Map<String, Command> commands = World.getWorld().getGameMode().getCommands();
      Command c = commands.get(parts[0].toLowerCase());
      if (c != null) {
        parts[0] = null;
        List<String> partsList = new ArrayList<String>();
        for (String s : parts) {
          if (s != null) {
            partsList.add(s);
          }
        }
        parts = partsList.toArray(new String[0]);
        c.execute(session.getPlayer(), new CommandParameters(parts));
        for (Player p : World.getWorld().getPlayerList().getPlayers()) {
          if (p.sendCommandLog) {
            p.getActionSender()
                .sendChatMessage("&2" + session.getPlayer().getName() + " issued " + message);
          }
        }
      } else {
        session.getActionSender().sendChatMessage("Invalid command /" + parts[0] + ".");
      }
    } else {
      World.getWorld().getGameMode().broadcastChatMessage(session.getPlayer(), message);
    }
  }
}
