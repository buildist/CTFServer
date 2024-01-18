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
import org.opencraft.server.model.World;
import tf.jacobsc.utils.DuelRatingSystem;

/**
 * Official /deop command **NEEDS PERSISTENCE
 *
 * <p>*
 *
 * @author S�ren Enevoldsen
 */
public class DuelAcceptCommand implements Command {

  /** The instance of this command. */
  private static final DuelAcceptCommand INSTANCE = new DuelAcceptCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static DuelAcceptCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(final Player player, CommandParameters params) {
    if (player.duelChallengedBy == null) {
      player.getActionSender().sendChatMessage("- &eNo one has challenged you to a duel.");
    } else if (player.hasFlag || player.duelChallengedBy.hasFlag) {
      player
          .getActionSender()
          .sendChatMessage(
              "- &eYou can't duel when you have the flag or your" + " opponent has it.");
    } else if (player.duelPlayer != null || player.duelChallengedBy.duelPlayer != null) {
      player.getActionSender().sendChatMessage("You're already in a duel!");
    } else if (player.team == -1 || player.duelChallengedBy.team == -1) {
      player.getActionSender().sendChatMessage("You must be on a team to duel!");
    } else {
      int balance = DuelRatingSystem.INSTANCE.matchQuality(player, player.duelChallengedBy);

      player
          .getActionSender()
          .sendChatMessage(
              "- &bYou are now dueling "
                  + player.duelChallengedBy.getColoredName()
                  + "&b! Kill them 3 times to win.");
      player
          .duelChallengedBy
          .getActionSender()
          .sendChatMessage(
              "- "
                  + player.getColoredName()
                  + "&b accepted your request to duel! Kill them 3 times to win.");
      World.getWorld()
          .broadcast(
              "- "
                  + player.duelChallengedBy.getColoredName()
                  + " &bis now "
                  + "dueling "
                  + player.getColoredName()
                  + "&b! Balance: " + balance + "%");

      player.duelPlayer = player.duelChallengedBy;
      player.duelChallengedBy.duelPlayer = player;

      player.duelKills = 0;
      player.duelChallengedBy.duelKills = 0;
    }
  }
}
