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

import org.opencraft.server.Server;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.game.impl.GameSettings.GameSetting;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

import java.util.Arrays;

public class SetCommand implements Command {

  private static final SetCommand INSTANCE = new SetCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static SetCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (player.isOp()) {
      if (params.getArgumentCount() == 0) {
        player.getActionSender().sendChatMessage("/set [name] [value]");
        for (GameSetting setting : GameSettings.getSettings().values()) {
          player.getActionSender().sendChatMessage(setting.name + " = " + setting.value);
        }
      } else if (params.getArgumentCount() >= 2) {
        // Reload store items if one was specified
        if (params.getStringArgument(0).equals("BigTNTPrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("BigTNT", price);
        } else if (params.getStringArgument(0).equals("BigTNTAmount") ||
                params.getStringArgument(0).equals("BigTNTRadius")) {
          int radius = Integer.parseInt(params.getStringArgument(1));

          if (radius > 7 || radius < 1) {
            player.getActionSender().sendChatMessage("BigTNT radius must be between 1-7.");
            return;
          }
          // Changing the amount/radius should update the item description, but not the price
          Server.getStore().updateItem("BigTNT", GameSettings.getInt("BigTNTPrice"));
        } else if (params.getStringArgument(0).equals("CreeperPrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("Creeper", price);
        } else if (params.getStringArgument(0).equals("GrenadePrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("Grenade", price);
        } else if (params.getStringArgument(0).equals("LinePrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("Line", price);
        } else if (params.getStringArgument(0).equals("RocketPrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("Rocket", price);
        } else if (params.getStringArgument(0).equals("SmokeGrenadePrice")) {
          int price = Integer.parseInt(params.getStringArgument(1));
          Server.getStore().updateItem("SmokeGrenade", price);
        }

        String args = params.getStringArgument(1);

        String type = (args.matches("\\d+")) ? "integer" : (args.equalsIgnoreCase("true")
                || args.equalsIgnoreCase("false")) ? "boolean" : "string";

        // Support spaces if the parameter is a string
        if (type == "string" && params.getArgumentCount() > 2) {
          args = String.join(" ", Arrays.copyOfRange(params.args, 1, params.getArgumentCount()));
        }

        if (GameSettings.set(params.getStringArgument(0), args)) {
          World.getWorld().broadcast("- &7Server setting " + params.getStringArgument(0) + " set to " + args);
          Server.log(player.getName() + " " + params.getStringArgument(0) + " set to " + args);
          GameSettings.save();
        }
      } else {
        if (params.getStringArgument(0).equals("default")) {
          GameSettings.reset();
        } else {
          player.getActionSender().sendChatMessage("Setting doesn't exist, or invalid value.");
        }
      }
    } else {
      player.getActionSender().sendChatMessage("You must be OP to do that!");
    }
  }
}
