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
import tf.jacobsc.utils.RatingKt;
import tf.jacobsc.utils.RatingType;

public class StatsCommand implements Command {
  private static final StatsCommand INSTANCE = new StatsCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static StatsCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    player
        .getActionSender()
        .sendChatMessage(
            "- &eWins: "
                + player.getIntAttribute("wins")
                + " - "
                + "Games Played: "
                + player.getIntAttribute("games")
                + " ");
    player
        .getActionSender()
        .sendChatMessage(
            "- &eTags: "
                + player.getIntAttribute("tags")
                + " - "
                + "Captures: "
                + player.getIntAttribute("captures")
                + " ");
    player
        .getActionSender()
        .sendChatMessage(
            "- &eExplodes: "
                + player.getIntAttribute("explodes")
                + " - "
                + "Mines: "
                + player.getIntAttribute("mines")
                + " ");
    player.getActionSender().sendChatMessage("- &eRagequits: " + player.getIntAttribute("ragequits"));

    Integer trGames = player.getRatedGamesFor(RatingType.Team);
    Integer drGames = player.getRatedGamesFor(RatingType.Duel);
    Integer crGames = player.getRatedGamesFor(RatingType.Casual);
    player.getActionSender().sendChatMessage("- &eTR: " + RatingKt.showFullRatingWithGames(player.getTeamRating(), trGames));
    player.getActionSender().sendChatMessage("- &eDR: " + RatingKt.showFullRatingWithGames(player.getDuelRating(), drGames));
//    player.getActionSender().sendChatMessage("- &eCasual: " + RatingKt.showFullRatingWithGames(player.getCasualRating(), crGames));
  }
}
