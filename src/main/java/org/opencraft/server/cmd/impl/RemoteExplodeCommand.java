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
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.World;

public class RemoteExplodeCommand implements Command {

  private static final RemoteExplodeCommand INSTANCE = new RemoteExplodeCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static RemoteExplodeCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    Position pos = player.getPosition();
    Rotation r = player.getRotation();

    int heading = (int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256)) - 90;
    int pitch = 360 - (int) (Server.getUnsigned(r.getLook()) * ((float) 360 / 256));

    double px = (pos.getX() - 16) / 32;
    double py = (pos.getY() - 16) / 32;
    double pz = ((pos.getZ() - 16) / 32);

    double vx = Math.cos(Math.toRadians(heading));
    double vz = Math.tan(Math.toRadians(pitch));
    double vy = Math.sin(Math.toRadians(heading));
    double x = px;
    double y = py;
    double z = pz;
    double lastX = px;
    double lastY = py;
    double lastZ = pz;
    for (int i = 0; i < 256; i++) {
      x += vx;
      y += vy;
      z += vz;
      int bx = (int) Math.round(x);
      int by = (int) Math.round(y);
      int bz = (int) Math.round(z);
      int block = World.getWorld().getLevel().getBlock(bx, by, bz);
      if (block != 0) {
        ((CTFGameMode)World.getWorld()
            .getGameMode())
            .explodeTNT(
                player, World.getWorld().getLevel(), bx, by, bz, 3, true, false, false, null);
        return;
      }
      lastX = x;
      lastY = y;
      lastZ = z;
      i++;
    }
  }
}
