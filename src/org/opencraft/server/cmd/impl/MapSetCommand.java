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
package org.opencraft.server.cmd.impl;

import org.opencraft.server.Configuration;
import org.opencraft.server.Server;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

import java.util.Enumeration;

public class MapSetCommand implements Command {

  private static final MapSetCommand INSTANCE = new MapSetCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static MapSetCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (player.isOp()) {
      Level level = World.getWorld().getLevel();
      if (!level.id.contains("/")) {
        player.getActionSender().sendChatMessage("- &eChanging a default map. Be careful!");
      }
      if (params.getArgumentCount() == 0) {
        player.getActionSender().sendChatMessage("/mapset [name] [value]");
        Enumeration<Object> keys = level.props.keys();
        while (keys.hasMoreElements()) {
          Object key = keys.nextElement();
          player.getActionSender().sendChatMessage(key + " = " + level.props.get(key));
        }
      } else if (params.getArgumentCount() >= 2) {
        String k = params.getStringArgument(0);
        String v = "";
        for (int i = 1; i < params.getArgumentCount(); i++) {
          v += params.getStringArgument(i);
          if (i != params.getArgumentCount() - 1) {
            v += " ";
          }
        }
        Server.log(player.getName() + " " + k + " set to " + v);
        doPropertyChange(k, v.trim());
        if (k.endsWith("Color")) {
          for (Player p : World.getWorld().getPlayerList().getPlayers()) {
            p.getActionSender().sendMapColors();
          }
        }
      }
    } else {
      player.getActionSender().sendChatMessage("You need to be op to do that!");
    }
  }

  public static void doPropertyChange(String k, String v) {
    Level level = World.getWorld().getLevel();
    if (v.equals("null")) {
      level.props.remove(k);
    } else {
      level.props.setProperty(k, v);
    }
    level.saveProps();
    level.loadProps();
    World.getWorld().broadcast("- &7Map setting " + k + " set to " + v);
  }
}
