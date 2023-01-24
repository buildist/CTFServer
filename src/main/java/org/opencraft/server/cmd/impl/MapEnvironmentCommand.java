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

import java.util.HashMap;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class MapEnvironmentCommand implements Command {

  private static final MapEnvironmentCommand INSTANCE = new MapEnvironmentCommand();

  private static final HashMap<String, String[]> presets = new HashMap<String, String[]>();

  public static MapEnvironmentCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if (player.isOp()) {
      if (params.getArgumentCount() == 0) {
        sendList(player);
        return;
      }
      String name = params.getStringArgument(0);
      if (!presets.containsKey(name)) {
        sendList(player);
        return;
      }

      String[] colors = presets.get(name);
      setColorProperty("fogColor", colors[0]);
      setColorProperty("skyColor", colors[1]);
      setColorProperty("cloudColor", colors[2]);
      setColorProperty("diffuseColor", colors[3]);
      setColorProperty("ambientColor", colors[4]);
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        p.getActionSender().sendMapColors();
      }
    } else {
      player.getActionSender().sendChatMessage("You must be OP to do that!");
    }
  }

  private void sendList(Player player) {
    StringBuilder list = new StringBuilder();
    for (String preset : presets.keySet()) {
      list.append(preset).append(", ");
    }
    player.getActionSender().sendChatMessage("- &eAvailable environments: " + list);
  }

  private void setColorProperty(String name, String colorString) {
    MapSetCommand.doPropertyChange(name, "#" + colorString);
  }

  static {
    // fog, sky, clouds, diffuse/sun, ambient/shadow
    presets.put("midnight", new String[] {"8b8989", "191970", "000080", "0000cd", "918A3B"});
    presets.put("cartoon", new String[] {"00ffff", "1e90ff", "00bfff", "f5deb3", "f4a460"});
    presets.put("noir", new String[] {"000000", "1f1f1f", "000000", "696969", "1f1f1f"});
    presets.put("trippy", new String[] {"4B0082", "FFD700", "006400", "7CFC00", "B22222"});
    presets.put("watery", new String[] {"5f9ea0", "008080", "008B8B", "E0FFFF", "008B8B"});
    presets.put("normal", new String[] {"default", "default", "default", "default", "default"});
    presets.put("gloomy", new String[] {"6A80A5", "405875", "405875", "444466", "3B3B59"});
    presets.put("cloudy", new String[] {"AFAFAF", "8E8E8E", "8E8E8E", "9b9b9b", "8C8C8C"});
    presets.put("sunset", new String[] {"FFA322", "836668", "9A6551", "7F6C60", "46444C"});
    presets.put("midnight2", new String[] {"131947", "070A23", "1E223A", "181828", "0F0F19"});
  }
}
