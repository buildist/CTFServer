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
import org.opencraft.server.model.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImportMapCommand implements Command {

  private static final ImportMapCommand INSTANCE = new ImportMapCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static ImportMapCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(final Player player, final CommandParameters params) {
    // Only trust these users to import maps
    if (player.getName().equalsIgnoreCase("Jacob_") ||
            player.getName().equalsIgnoreCase("jack") ||
            player.getName().equalsIgnoreCase("Venk"))
    {
      if (params.getArgumentCount() > 1) {
        player.getActionSender().sendChatMessage("Downloading map...");
        new Thread(
                new Runnable() {
                  @Override
                  public void run() {
                    try {
                      String mapName = params.getStringArgument(0);
                      String urlString = params.getStringArgument(1);

                      // Special use case for Dropbox. dl=0 does not actually download the .cw file
                      if (urlString.contains("dropbox"))
                        urlString = urlString.replace("dl=0", "dl=1");

                      String path = "./maps/" + mapName + ".cw"; // Use '/importmap more/<map> <url>' to import 'more/' maps

                      try (InputStream in = new URL(urlString).openStream()) {
                        Files.copy(in, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
                      }

                      player.getActionSender().sendChatMessage("&aSaved to " + path);
                      player.getActionSender().sendChatMessage("Use /newgame " + mapName + " to switch" + " to it");
                      player.getActionSender().sendChatMessage("Use /importprops to import the .properties!");
                    } catch (Exception ex) {
                      player.getActionSender().sendChatMessage("Error downloading map:");
                      player.getActionSender().sendChatMessage(ex.toString());
                      Server.log(ex);
                    }
                  }
                })
                .start();
      } else {
        player.getActionSender().sendChatMessage("Wrong number of arguments");
        player.getActionSender().sendChatMessage("/importmap <map> <url>");
      }
    } else {
      player.getActionSender().sendChatMessage("Only Jacob_, jack, or Venk may import maps.");
    }
  }
}
