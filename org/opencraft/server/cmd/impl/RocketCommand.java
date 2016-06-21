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
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;
import org.opencraft.server.model.World;

public class RocketCommand implements Command {

  private static final RocketCommand INSTANCE = new RocketCommand();
  private static final int TIMEOUT = 10;
  Thread rocketThread;

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static RocketCommand getCommand() {
    return INSTANCE;
  }

  void stop() {
    rocketThread.stop();
  }

  public void execute(final Player player, CommandParameters params) {
    rocketThread = new Thread(new Runnable() {
      public void run() {
        long dt = (System.currentTimeMillis() - player.rocketTime);
        if (dt < TIMEOUT * 1000) {
          player.getActionSender().sendChatMessage("- &ePlease wait " + (TIMEOUT - dt / 1000) + "" +
              " seconds");
          if (!GameSettings.getBoolean("Chaos")) {
            player.addStorePoints(50);
          }
          return;
        }
        player.rocketTime = System.currentTimeMillis();
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
          if (block != 0 && block != 49) {
            ((CTFGameMode) World.getWorld().getGameMode()).explodeTNT(player, World.getWorld()
                .getLevel(), bx, by, bz, 2, true, false, false, "rocket");
            break;
          } else {
            World.getWorld().getLevel().setBlock((int) Math.round(lastX), (int) Math.round(lastY)
                , (int) Math.round(lastZ), 0);
            World.getWorld().getLevel().setBlock(bx, by, bz, 49);
          }
          lastX = x;
          lastY = y;
          lastZ = z;
          i++;
          try {
            Thread.sleep(25);
          } catch (InterruptedException ex) {
          }
        }
        return;
                /*Vector<Position> fires = new Vector<Position>(7 * 7 * 7);
                for(int x2 = (int) lastX - 3; x2 < (int) lastX + 3; x2++)
                {
                    for(int y2 = (int) lastY - 3; y2 < (int) lastY + 3; y2++)
                    {
                        for(int z2 = (int) lastZ - 3; z2 < (int) lastZ + 3; z2++)
                        {
                            if(Math.random() < 0.20)
                            {
                                if(World.getWorld().getLevel().getBlock(x2, y2, z2) == 0)
                                    fires.add(new Position(x2, y2, z2));
                            }
                        }
                    }
                }
                while(fires.size() > 0)
                {
                    Enumeration<Position> en = fires.elements();
                    while(en.hasMoreElements())
                    {
                        Position p = en.nextElement();
                        int posX = p.getX();
                        int posY = p.getY();
                        int posZ = p.getZ();
                        if(World.getWorld().getLevel().getBlock(posX, posY, posZ) != 7)
                            World.getWorld().getLevel().setBlock(posX, posY, posZ, 0);
                        posZ--;
                        if(World.getWorld().getLevel().getBlock(posX, posY, posZ) != 0 && World
                        .getWorld().getLevel().getBlock(posX, posY, posZ) != 11 || posZ < 0)
                        {
                            fires.remove(p);
                        }
                        else
                        {
                            World.getWorld().getLevel().setBlock(posX, posY, posZ, 11);
                            fires.remove(p);
                            fires.add(new Position(posX, posY, posZ));
                        }
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                    }
                }*/
      }
    });
    rocketThread.start();
  }

}
