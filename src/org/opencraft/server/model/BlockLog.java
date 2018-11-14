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

import java.util.HashMap;

public class BlockLog {
  private static HashMap<IntPosition, BlockInfo> info = new HashMap<IntPosition, BlockInfo>(1024);

  public static BlockInfo getInfo(int x, int y, int z) {
    IntPosition pos = new IntPosition(x, y, z);
    return info.get(pos);
  }

  public static void clear() {
    info.clear();
  }

  public static void logBlockChange(Player p, int x, int y, int z) {
    BlockInfo b = new BlockInfo();
    b.player = p;
    info.put(new IntPosition(x, y, z), b);
  }

  public static class BlockInfo {
    public Player player;
  }

  private static class IntPosition {
    public int x;
    public int y;
    public int z;

    public IntPosition(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public boolean equals(Object obj) {
      if (!(obj instanceof IntPosition))
        return false;
      IntPosition p = (IntPosition) obj;
      return x == p.x && y == p.y && z == p.z;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 29 * hash + this.x;
      hash = 29 * hash + this.y;
      hash = 29 * hash + this.z;
      return hash;
    }
  }
}
