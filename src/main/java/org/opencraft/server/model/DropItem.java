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
package org.opencraft.server.model;

import org.opencraft.server.Constants;

public class DropItem implements Runnable {
  public int points;
  public int posX;
  public int posY;
  public int posZ;

  public DropItem(int p) {
    points = p;
    new Thread(this).start();
    World.getWorld().broadcast("- &bA crate of points has been dropped somewhere on the map!");
  }

  public void pickUp(Player p) {
    if (p.team == -1) {
      p.getActionSender().sendBlock(posX, posY, posZ, (short) Constants.BLOCK_CRATE);
    } else {
      World.getWorld().broadcast("- " + p.parseName() + " has found " + points + " points!");
      p.addPoints(points);
      World.getWorld().getLevel().setBlock(posX, posY, posZ, 0);
      World.getWorld().getGameMode().removeDropItem(this);
    }
  }

  public void run() {
    posX = (int) (4 + Math.random() * (World.getWorld().getLevel().getWidth() - 8));
    posY = (int) (4 + Math.random() * (World.getWorld().getLevel().getHeight() - 8));
    posZ = World.getWorld().getLevel().ceiling - 8;
    World.getWorld().getGameMode().addDropItem(this);
    boolean done = false;
    for (int i = 1; i < 1000 && !done; i++) {
      if (World.getWorld().getLevel().getBlock(posX, posY, posZ) != 7)
        World.getWorld().getLevel().setBlock(posX, posY, posZ, 0);
      posZ--;
      if (World.getWorld().getLevel().getBlock(posX, posY, posZ) != 0
              && World.getWorld().getLevel().getBlock(posX, posY, posZ) != 11
          || posZ < 0) {
        done = true;
        posZ++;
        World.getWorld().getLevel().setBlock(posX, posY, posZ, Constants.BLOCK_CRATE);
      } else {
        World.getWorld().getLevel().setBlock(posX, posY, posZ, Constants.BLOCK_CRATE);
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException ex) {
      }
    }
  }
}
