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

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import tf.jacobsc.utils.RatingKt;
import tf.jacobsc.utils.RatingType;

import java.util.ArrayList;
import java.util.List;

public class POVCommand implements Command {

  /** The instance of this command. */
  private static final POVCommand INSTANCE = new POVCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static POVCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    // Check if player using command is a spectator
    if (player.team == -1) {
      if (params.getArgumentCount() == 1) {
        if (params.getStringArgument(0).equals("-reset")) {
          player.following = null;
          return;
        }

        if (params.getStringArgument(0).equals("-next")) {
          List<Player> players = new ArrayList<>();

          for (Player pl : World.getWorld().getPlayerList().getPlayers()) {
            if (pl.team == 0 || pl.team == 1) {
              players.add(pl);
            }
          }

          if (players.size() == 0) {
            return;
          }

          if (player.followingIndex >= players.size() - 1) {
            player.followingIndex = -1;
          }

          player.followingIndex++;

          Player other = players.get(player.followingIndex);

          if (other != null) {
            player.getActionSender().sendTeleport(other.getPosition(), other.getRotation());
            player.setPosition(other.getPosition());
            player.setRotation(other.getRotation());
          }

          return;
        }

        if (params.getStringArgument(0).equals("-back")) {
          List<Player> players = new ArrayList<>();

          for (Player pl : World.getWorld().getPlayerList().getPlayers()) {
            if (pl.team == 0 || pl.team == 1) {
              players.add(pl);
            }
          }

          if (players.size() == 0) {
            return;
          }

          if (player.followingIndex <= 0) {
            player.followingIndex = players.size();
          }

          player.followingIndex--;

          Player other = players.get(player.followingIndex);

          if (other != null) {
            player.getActionSender().sendTeleport(other.getPosition(), other.getRotation());
            player.setPosition(other.getPosition());
            player.setRotation(other.getRotation());
          }

          return;
        }

        Player other = Player.getPlayer(params.getStringArgument(0), player.getActionSender());

        if (other != null) {
          player.following = other;
          return;
        }

        // Player not found
        player.getActionSender().sendChatMessage(params.getStringArgument(0) + " was not found");
      } else {
        player.getActionSender().sendChatMessage("Wrong number of arguments");
        player.getActionSender().sendChatMessage("/pov <name> - Switches to <name>'s POV");
        player.getActionSender().sendChatMessage("/pov -next - Switches to the next player's POV");
        player.getActionSender().sendChatMessage("/pov -back - Switches to the last player's POV");
        player.getActionSender().sendChatMessage("/pov -reset - Reverts back to your own POV");
      }
    } else {
      player.getActionSender().sendChatMessage("You must be a spectator to do that!");
    }
  }
}
