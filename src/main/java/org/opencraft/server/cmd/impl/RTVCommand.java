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

public class RTVCommand implements Command {
  /** The instance of this command. */
  private static final RTVCommand INSTANCE = new RTVCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static RTVCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (!GameSettings.getBoolean("Tournament")) {
      int totalAFK = 0;
      for (Player pl : World.getWorld().getPlayerList().getPlayers()) {
        if (pl.AFK) totalAFK++;
      }
      int requiredVotes = (World.getWorld().getPlayerList().size() - totalAFK) / 2;
      if (World.getWorld().getPlayerList().size() % 2 != 0) requiredVotes++;

      if (World.getWorld().getGameMode().voting)
        player.getActionSender().sendChatMessage("- &eYou can't /rtv during map voting.");
      else if (World.getWorld().getGameMode().rtvYesPlayers.contains(player.getSession().getIP()))
        player.getActionSender().sendChatMessage("- &eYou have already voted.");
      else if (player.team == -1)
        player.getActionSender().sendChatMessage("- &eYou can't /rtv while not on a team.");
      else {
        if (World.getWorld().getGameMode().rtvNoPlayers.contains(player.getSession().getIP())) {
          World.getWorld().getGameMode().rtvVotes++;
          World.getWorld().getGameMode().rtvNoPlayers.remove(player.getSession().getIP());
        }

        int votes = ++World.getWorld().getGameMode().rtvVotes;
        World.getWorld()
            .broadcast(
                "- "
                    + player.getColoredName()
                    + " &3wants to rock the vote &f"
                    + "("
                    + votes
                    + " votes, "
                    + requiredVotes
                    + " required)");
        World.getWorld()
            .broadcast(
                "- &3/rtv to vote, /nominate [mapname] to nominate a map, /no "
                    + "to stay on this map");
        World.getWorld().getGameMode().rtvYesPlayers.add(player.getSession().getIP());

        if (votes >= requiredVotes) {
          World.getWorld().getGameMode().endGame();
        }
      }
    }
  }
}
