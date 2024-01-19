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
import org.opencraft.server.model.Player;
import org.opencraft.server.persistence.LoadPersistenceRequest;
import tf.jacobsc.utils.RatingKt;

public class PInfoCommand implements Command {

  private static final PInfoCommand INSTANCE = new PInfoCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static PInfoCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    Player target = params.getArgumentCount() > 0
        ? Player.getPlayer(params.getStringArgument(0), player.getActionSender())
        : player;
    if (target == null) {
      target = new Player(null, params.getStringArgument(0));
      try {
        new LoadPersistenceRequest(target).perform();
      } catch (Exception e) {
        target = null;
      }
    }
    if (target != null) {
      player.getActionSender().sendChatMessage("- &aStats for " + target.getName());
      if(player.isOp())
        player.getActionSender().sendChatMessage(
            "- &eIP:"+Player.getAttributeFor(target.getName(), "ip", player.getActionSender()));
      player
          .getActionSender()
          .sendChatMessage(
              "- &eWins: "
                  + target.getIntAttribute("wins")
                  + " - "
                  + "Games Played: "
                  + target.getIntAttribute("games")
                  + " ");
      player
          .getActionSender()
          .sendChatMessage(
              "- &eTags: "
                  + target.getIntAttribute("tags")
                  + " - "
                  + "Captures: "
                  + target.getIntAttribute("captures")
                  + " ");
      player
          .getActionSender()
          .sendChatMessage(
              "- &eExplodes: "
                  + target.getIntAttribute("explodes")
                  + " - "
                  + "Mines: "
                  + target.getIntAttribute("mines")
                  + " ");
      player
          .getActionSender()
          .sendChatMessage("- &eRagequits: " + target.getIntAttribute("ragequits"));
      player.getActionSender().sendChatMessage("- &eTeam Rating: " + RatingKt.displayFullRating(target.getTeamRating()));
      player.getActionSender().sendChatMessage("- &eDuel Rating: " + RatingKt.displayFullRating(target.getDuelRating()));
      player.getActionSender().sendChatMessage("- &ePoints: " + target.getPoints());
    }
  }
}
