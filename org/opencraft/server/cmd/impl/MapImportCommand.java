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

import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;

public class MapImportCommand implements Command {

    private static final MapImportCommand INSTANCE = new MapImportCommand();

    /**
     * Gets the singleton instance of this command.
     *
     * @return The singleton instance of this command.
     */
    public static MapImportCommand getCommand() {
        return INSTANCE;
    }

    @Override
    public void execute(final Player player, final CommandParameters params) {
        if (player.isOp()) {
            player.getActionSender().sendChatMessage("Downloading map...");
            new Thread(new Runnable() {
                @Override
                public void run() {                    
                    try {
                        String mapName = params.getStringArgument(0);
                        String urlString = "http://persignum.com/download.php?password=IJobS0d3Mb&mapname="+mapName;
                        URL url = new URL(urlString);
                        ReadableByteChannel ch = Channels.newChannel(url.openStream());
                        if(mapName.contains("/"))
                            mapName = mapName.substring(mapName.indexOf("/")+1);
                        String path = "maps/more/"+mapName+".lvl";
                        FileOutputStream out = new FileOutputStream(path);
                        out.getChannel().transferFrom(ch, 0, Long.MAX_VALUE);
                        player.getActionSender().sendChatMessage("Saved to "+path);
                        player.getActionSender().sendChatMessage("Use /newgame more/"+mapName+" to switch to it");
                    } catch(Exception ex) {
                        player.getActionSender().sendChatMessage("Error downloading map. Blame Jacob_ or Jack");
                    }
                }                
            }).start();
        } else {
            player.getActionSender().sendChatMessage("You need to be op to do that!");
        }
    }
}
