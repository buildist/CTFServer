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
package org.opencraft.server.game;


import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.impl.DeOperatorCommand;
import org.opencraft.server.cmd.impl.KickCommand;
import org.opencraft.server.cmd.impl.OperatorCommand;
import org.opencraft.server.cmd.impl.SayCommand;
import org.opencraft.server.cmd.impl.SetspawnCommand;
import org.opencraft.server.cmd.impl.TeleportCommand;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a game mode that does the majority of the work for the game mode developer.
 *
 * @author Graham Edgecombe
 */
public abstract class GameModeAdapter<P extends Player> implements GameMode<P> {

  /**
   * The command map.
   */
  private final Map<String, Command> commands = new HashMap<String, Command>();

  /**
   * Creates the game mode adapter with default settings.
   */
  public GameModeAdapter() {
    // these commands are standard to every game mode
    registerCommand("op", OperatorCommand.getCommand());
    registerCommand("deop", DeOperatorCommand.getCommand());
    registerCommand("say", SayCommand.getCommand());
    registerCommand("kick", KickCommand.getCommand());
    registerCommand("tp", TeleportCommand.getCommand());
    registerCommand("setspawn", SetspawnCommand.getCommand());
  }

  /**
   * Adds a command
   *
   * @param name    The command name.
   * @param command The command.
   */
  public void registerCommand(String name, Command command) {
    commands.put(name, command);
  }

  @Override
  public Map<String, Command> getCommands() {
    return commands;
  }

  // Default implementation
  public void tick() {

  }

  // Default implementation
  public void playerConnected(Player player) {
    World.getWorld().broadcast("Welcome " + player.getName());
  }

  // Default implementation
  public void setBlock(Player player, Level level, int x, int y, int z, int mode, int type) {
    level.setBlock(x, y, z, (mode == 1 ? type : 0));
  }

  // Default implementation
  public void playerDisconnected(Player player) {
    World.getWorld().broadcast(player.getName() + " disconnected.");
  }

  // Default implementation
  public void broadcastChatMessage(Player player, String message) {
    World.getWorld().broadcast(player, player.getName() + ": " + message);
  }

}
