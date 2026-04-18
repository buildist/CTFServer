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
import org.opencraft.server.game.GameMode;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class CaptainCommand implements Command {

  private static final CaptainCommand INSTANCE = new CaptainCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static CaptainCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if (player.isOp()) {
      if (params.getArgumentCount() >= 2) {
        String name = params.getStringArgument(0);
        String team = params.getStringArgument(1);

        Player other = Player.getPlayer(name, player.getActionSender());

        if (other != null) {
          if (team.equalsIgnoreCase("red")) {
            other.joinTeam("red");
            World.getWorld().getGameMode().redCaptain = other;
            World.getWorld().broadcast("&f- " + other.getColoredName() + " &eis now the captain for &cbred team&e.");
          } else if (team.equalsIgnoreCase("blue")) {
            other.joinTeam("blue");
            World.getWorld().getGameMode().blueCaptain = other;
            World.getWorld().broadcast("&f- " + other.getColoredName() + " &eis now the captain for &bblue team&e.");
          } else {
            player.getActionSender().sendChatMessage("Invalid team specified.");
            return;
          }
        }
      } else {
        player.getActionSender().sendChatMessage("Wrong number of arguments");
        player.getActionSender().sendChatMessage("/captain <name> <team>");
      }
    } else {
      player.getActionSender().sendChatMessage("You must be OP to do that!");
    }
  }
}
