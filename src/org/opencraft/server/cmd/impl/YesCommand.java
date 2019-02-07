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

import java.net.URLEncoder;
import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.WebServer;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.MapRatings;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class YesCommand implements Command {
  /** The instance of this command. */
  private static final YesCommand INSTANCE = new YesCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static YesCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (World.getWorld().getGameMode().voting) {
      final String mapName = World.getWorld().getLevel().id;
      MapRatings.setPlayerRating(player.getName(), mapName, 1);
      player.getActionSender().sendChatMessage(" - &eThanks for your feedback!");
      String text = "";
      if (params.getArgumentCount() > 0) {
        for (int i = 0; i < params.getArgumentCount(); i++) {
          text += params.getStringArgument(i);
          if (i != params.getArgumentCount() - 1) {
            text += " ";
          }
        }
      }
      final String message =
          player.getName()
              + " voted Yes: "
              + text
              + "\n\nCurrent rating: "
              + MapRatings.getRating(mapName);
      WebServer.run(
          new Runnable() {
            @Override
            public void run() {
              try {
                String urlMessage = URLEncoder.encode(message, "UTF-8");
                Server.httpGet(
                    Constants.URL_MAP_COMMENT + "&map=" + mapName + "&message=" + urlMessage);
              } catch (Exception ex) {
                Server.log(ex);
              }
            }
          });
    }
  }
}
