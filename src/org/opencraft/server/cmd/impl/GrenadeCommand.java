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
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.World;

public class GrenadeCommand implements Command {
  private static final GrenadeCommand INSTANCE = new GrenadeCommand();
  Thread rocketThread;

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static GrenadeCommand getCommand() {
    return INSTANCE;
  }

  void stop() {
    rocketThread.stop();
  }

  public void execute(final Player player, CommandParameters params) {
    rocketThread = new Thread(new Runnable() {
      public void run() {
        Position pos = player.getPosition().toBlockPos();
        Rotation r = player.getRotation();

        double heading = Math.toRadians((int) (Server.getUnsigned(r.getRotation()) * ((float) 360 / 256) - 90));
        double pitch = Math.toRadians((int) (360 - Server.getUnsigned(r.getLook()) * ((float) 360 / 256)));

        double px = pos.getX();
        double py = pos.getY();
        double pz = pos.getZ();

        double vx = Math.cos(heading)*Math.cos(pitch);
        double vy = Math.sin(heading)*Math.cos(pitch);
        double vz = Math.sin(pitch);
        double length = Math.sqrt(vx * vx + vy * vy + vz * vz) / 1.25;
        vx /= length;
        vz /= length;
        vy /= length;
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
          if (block != 0 && block != 46) {
            World.getWorld().getLevel().setBlock((int) Math.round(lastX), (int) Math.round(lastY)
                , (int) Math.round(lastZ), 0);
            World.getWorld().getGameMode().explodeTNT(player, World.getWorld()
                .getLevel(), bx, by, bz, 2, true, false, false, "Samsung Galaxy Note 7");
            break;
          } else {
            World.getWorld().getLevel().setBlock((int) Math.round(lastX), (int) Math.round(lastY)
                , (int) Math.round(lastZ), 0);
            if (block == 0)
              World.getWorld().getLevel().setBlock(bx, by, bz, Constants.BLOCK_TNT);
          }
          lastX = x;
          lastY = y;
          lastZ = z;
          i++;
          if (vz > (double) -2)
            vz -= 0.15;
          vx *= 0.95;
          vy *= 0.95;
          try {
            Thread.sleep(100);
          } catch (InterruptedException ex) {
          }
        }
      }
    });
    rocketThread.start();
  }
}
