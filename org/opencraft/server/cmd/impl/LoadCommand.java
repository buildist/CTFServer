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

import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

import java.io.File;
//import java.nio.file.FileSystem;
//import java.nio.file.Files;

public class LoadCommand  {/*

  private static final LoadCommand INSTANCE = new LoadCommand();

  public static LoadCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if ((player.isOp())) {
      if (params.getArgumentCount() != 1) {
        player.getActionSender().sendChatMessage("/load [mapname]");
        return;
      }
      String map = params.getStringArgument(1);
      File datFile = new File(Constants.ROOT_PATH + "/maps/more/"+map+".dat");
      File lvlFile = new File(Constants.ROOT_PATH + "/maps/more/"+map+".lvl");
      File propertiesFile = new File(Constants.ROOT_PATH + "/maps/more/"+map+".properties");
      if (propertiesFile.exists()) {
        if (datFile.exists()) {
          Files.move()
        } else if(lvlFile.exists()) {

        }
      }
      player.getActionSender().sendChatMessage("- &eMap not found.");
    } else
      player.getActionSender().sendChatMessage("You must be OP to do that");
  }
*/}
