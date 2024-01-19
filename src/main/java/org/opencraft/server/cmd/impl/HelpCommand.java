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

public class HelpCommand implements Command {

  private static final HelpCommand INSTANCE = new HelpCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static HelpCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if (params.getArgumentCount() == 0) {
      player.getActionSender().sendChatMessage("&7-");
      player.getActionSender().sendChatMessage("&eType &b/help game &eto learn how to play.");
      player.getActionSender().sendChatMessage("&eType &b/help tnt &eto learn about &cTNT&e.");
      player.getActionSender().sendChatMessage("&eType &b/help mines &eto learn about &8mines&e.");
      player.getActionSender().sendChatMessage("&eType &b/help tagging &eto learn how tagging works.");
      player.getActionSender().sendChatMessage("&eType &b/help points &eto learn how to earn and spend points.");
      player.getActionSender().sendChatMessage("&eType &b/help stalemate &eto learn how anti-stalemate mode works.");
      player.getActionSender().sendChatMessage("&eType &b/help rating &eto learn how rating works.");
      player.getActionSender().sendChatMessage("&eType &b/commands &efor a list of commands.");
      player.getActionSender().sendChatMessage("&7-");
      player.getActionSender().sendChatMessage("&a* You may need to scroll up to see the full message *");
    } else if (params.getStringArgument(0).equals("game")) {
      player.getActionSender().sendChatMessage("&7- &eThe aim of the game is to get the most flags by the end of the round. Rounds typically last for &b20 minutes &eor until a team reaches &b5 captures &efirst. In the event of a draw, the next capture wins. Click on the other team's flag to pick it up; capture it by clicking on your own flag. The team with the most captures by the end of the round wins!");
    } else if (params.getStringArgument(0).equals("tnt")) {
      player.getActionSender().sendChatMessage("&7- &eThe most common way to kill other players is with &cTNT&e. Place a &cTNT &eand then a &5detonator &eblock to explode it. If a player is within a &b2-block &eradius of the &cTNT&e, it will kill them. You may only have &b1 &cTNT &eactive at a time.");
    } else if (params.getStringArgument(0).equals("mines")) {
      player.getActionSender().sendChatMessage("&7- &8Mines &eare blocks that explode when players get within a &b2.5-block &eradius of them. You may only have &b2 &8mines &eactive at a time. Type &b/d &eto defuse your own &8mines&e. You can defuse enemy &8mines &eby exploding them. You will not die if you get close to &8mines &eplaced by you or your teammates.");
    } else if (params.getStringArgument(0).equals("tagging")) {
      player.getActionSender().sendChatMessage("&7- &eMost maps have a &bdivider &ein the middle of them to differentiate between sides. If you run into players from the enemy team on &byour &eside, you will tag and kill them. Likewise, if you run into them on &btheir &eside, you will be tagged and killed. &eIf the game is in &banti-stalemate mode &eand a player is tagged, the flag carrier from the tagged player's team will automatically drop the flag.");
    } else if (params.getStringArgument(0).equals("points")) {
      player.getActionSender().sendChatMessage("&7- &eAll players start with &b40 &epoints. &cTNT &ekills give &b5 &epoints, the first kill of the game &egives &b50 &epoints, &btagging &egives &b15 &epoints and &bflag captures &egive &b40 &epoints. You can also find points in &6crates&e. &eYou may use your points in the &b/store&e. &ePoints reset at the end of every round.");
    } else if (params.getStringArgument(0).equals("stalemate")) {
      player.getActionSender().sendChatMessage("&7- &eWhen both teams have an even amount of flag captures or both teams are currently holding the flag, the game goes into &banti-stalemate mode&e. This means that the first flag carrier to die will have their flag respawn immediately. If a player from your team is tagged, your flag carrier will automatically drop the flag.");
    } else if (params.getStringArgument(0).equals("rating")) {
      player.getActionSender().sendChatMessage("&7- &eThe rating system uses TrueSkill. TrueSkill is probability based and estimates your skill with a probability range. There are 2 completely separate rating values for each player, team rating and duel rating. Team rating is only calculated at the end of a tournament game. Duel rating is calculated for duel wins/losses and on domination when tournament mode is active. Abandoning a rated game or duel will make you lose rating. The duel rating shows by default unless tournament mode is active then the team rating is displayed. The &b/quality &ecommand can be used to help balance games. Match qualities range from 0-100%. A quality of 100% means the system thinks the game is very even.");
    }
  }
}
