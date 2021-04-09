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
import org.opencraft.server.WebServer;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

import java.util.ArrayList;
import java.util.List;

public class MuteCommand implements Command {

  private static final MuteCommand INSTANCE = new MuteCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static MuteCommand getCommand() {
    return INSTANCE;
  }

  private void mute(Player player, Player otherPlayer, boolean all) {
    // Mute in game
    if (!otherPlayer.muted) {
      otherPlayer.muted = true;
      Server.mutePlayer(otherPlayer.getName());
      Server.log(player.getName() + " muted " + otherPlayer.getName());
      World.getWorld().broadcast("- " + otherPlayer.parseName() + " has been muted!");
      otherPlayer.getActionSender().sendChatMessage("- &eYou have been muted!");
      player.getActionSender().sendChatMessage("- &eSay /mute [name] again to unmute them");
    } else {
      otherPlayer.muted = false;
      Server.unMutePlayer(otherPlayer.getName());
      Server.log(player.getName() + " unmuted " + otherPlayer.getName());
      World.getWorld().broadcast("- " + otherPlayer.parseName() + " has been ummuted!");
      otherPlayer.getActionSender().sendChatMessage("- &eYou are no longer muted");
    }
    // Mute them on the website
    if (!all) {
      String name = otherPlayer.getName().toLowerCase() + "[website]";
      if (WebServer.blockedWords.contains(name)) {
        WebServer.blockedWords.remove(name);
        player.getActionSender().sendChatMessage("- &eUnmuted " + name);
      } else {
        WebServer.blockedWords.add(name);
        player.getActionSender().sendChatMessage("- &eMuted " + name);
      }
    }
  }

  public void execute(Player player, CommandParameters params) {
    if (player.isOp() || player.isVIP()) {
      boolean muteAll = params.getStringArgument(0).equals("all");
      List<Player> other = new ArrayList<Player>();
      // Mute everybody
      if (muteAll) {
        // Make sure player is OP before muting all
        if (player.isOp()) {
          for (Player p : World.getWorld().getPlayerList().getPlayers()) {
            // Don't mute VIP's or OP's.
            if (!p.isOp() && !p.isVIP()) {
              other.add(p);
            }
          }
        } else { // Doesn't have mute all permissions
          player.getActionSender().sendChatMessage("- &eYou must be op to do that!");
        }
      } else { // Isn't a mute all
        Player toMute = Player.getPlayer(params.getStringArgument(0), player.getActionSender());
        if (toMute != null) {
          if (!player.isOp() && toMute.isOp()) { // Does not have permission to mute
            player.getActionSender().sendChatMessage("- &eYou must be op to mute another op!");
          } else {
            other.add(toMute);
          }
        }
      }

      // Go through list
      for (Player otherPlayer : other) {
        if (otherPlayer != null) {
          mute(player, otherPlayer, muteAll);
        }
      }
    } else {
      player.getActionSender().sendChatMessage("- &eYou must be op to do that!");
    }
  }
}
