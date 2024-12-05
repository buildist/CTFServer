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

public class SmokeZone {
  public int minX;
  public int minY;
  public int minZ;
  public int maxX;
  public int maxY;
  public int maxZ;
  public int id;
  public int density;

  public void updateDensity(int density) {
    // Update the packet

    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      if (density == 0) {
        player.getSession().getActionSender().sendRemoveSelectionCuboid(this.id);
        continue;
      }

      this.density = density;

      if (player.team == -1) {
        // Increase visibility for spectators, but also show that there is a smoke zone
        player.getSession().getActionSender().sendSelectionCuboid(
                this.id,
                "SmokeZone" + this.id,
                (short) minX,
                (short) minZ,
                (short) minY,
                (short) maxX,
                (short) maxZ,
                (short) maxY,
                (short) 36,
                (short) 36,
                (short) 36,
                (short) 40
        );
      }

      else {
        player.getSession().getActionSender().sendSelectionCuboid(
                this.id,
                "SmokeZone" + this.id,
                (short) minX,
                (short) minZ,
                (short) minY,
                (short) maxX,
                (short) maxZ,
                (short) maxY,
                (short) 36,
                (short) 36,
                (short) 36,
                (short) density
        );
      }
    }
  }

  public SmokeZone(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int id) {
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
    this.id = id;
    this.density = 255;
  }
}
