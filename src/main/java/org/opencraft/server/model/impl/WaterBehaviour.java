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
package org.opencraft.server.model.impl;

import org.opencraft.server.Configuration;
import org.opencraft.server.model.BlockBehaviour;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.model.Level;

/**
 * A block behaviour that handles water. Takes into account water's preference for downward flow.
 *
 * @author Brett Russell
 */
public class WaterBehaviour implements BlockBehaviour {

  @Override
  public void handlePassive(Level level, int x, int y, int z, int type) {
    level.queueActiveBlockUpdate(x, y, z);
  }

  @Override
  public void handleDestroy(Level level, int x, int y, int z, int type) {}

  @Override
  public void handleScheduledBehaviour(Level level, int x, int y, int z, int type) {

    // represents different directions in the Cartesian plane, z axis is
    // ignored and handled specially
    int[][] spreadRules = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}};

    int spongeRadius = Configuration.getConfiguration().getSpongeRadius();

    // preference: spread downward
    OUTERMOST_DOWNWARD:
    for (int offsetZ = z - 1; offsetZ >= 0; offsetZ--) {
      for (int spongeX = (-1 * spongeRadius); spongeX <= spongeRadius; spongeX++) {
        for (int spongeY = (-1 * spongeRadius); spongeY <= spongeRadius; spongeY++) {
          for (int spongeZ = (-1 * spongeRadius); spongeZ <= spongeRadius; spongeZ++) {
            if (level.getBlock(x + spongeX, y + spongeY, offsetZ + spongeZ)
                == BlockConstants.SPONGE) break OUTERMOST_DOWNWARD;
          }
        }
      }

      int thisBlock = level.getBlock(x, y, offsetZ);

      // check for lava
      if (thisBlock == BlockConstants.LAVA || thisBlock == BlockConstants.STILL_LAVA) {
        level.setBlock(x, y, offsetZ, BlockConstants.STONE);
      } else if (!BlockManager.getBlockManager().getBlock(thisBlock).isSolid()
          && !BlockManager.getBlockManager().getBlock(thisBlock).isLiquid()) {
        level.setBlock(x, y, offsetZ, BlockConstants.WATER);
      } else break;
    }

    // then, spread outward
    OUTERMOST_OUTWARD:
    for (int i = 0; i <= spreadRules.length - 1; i++) {
      int thisOutwardBlock =
          level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2]);

      for (int spongeX = (-1 * spongeRadius); spongeX <= spongeRadius; spongeX++) {
        for (int spongeY = (-1 * spongeRadius); spongeY <= spongeRadius; spongeY++) {
          for (int spongeZ = (-1 * spongeRadius); spongeZ <= spongeRadius; spongeZ++) {
            if (level.getBlock(
                    x + spreadRules[i][0] + spongeX,
                    y + spreadRules[i][1] + spongeY,
                    z + spreadRules[i][2] + spongeZ)
                == BlockConstants.SPONGE) break OUTERMOST_OUTWARD;
          }
        }
      }

      // check for lava
      if (thisOutwardBlock == BlockConstants.LAVA
          || thisOutwardBlock == BlockConstants.STILL_LAVA) {
        level.setBlock(
            x + spreadRules[i][0],
            y + spreadRules[i][1],
            z + spreadRules[i][2],
            BlockConstants.STONE);
      } else if (!BlockManager.getBlockManager().getBlock(thisOutwardBlock).isSolid()
          && !BlockManager.getBlockManager().getBlock(thisOutwardBlock).isLiquid()) {
        level.setBlock(
            x + spreadRules[i][0],
            y + spreadRules[i][1],
            z + spreadRules[i][2],
            BlockConstants.WATER);
      }
    }
  }
}
