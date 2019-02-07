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
package org.opencraft.server.extensions.brushes;

import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;

/**
 * Represents the brush used to "paint" the level with.
 *
 * @author S�ren Enevoldsen
 */
public abstract class Brush {

  /** Creates the brush with the default radius. */
  public Brush() {}

  /**
   * Creates the brush with the specified radius.
   *
   * @param radius The radius of this brush.
   */
  public Brush(int radius) {}

  /**
   * Handles the "painting". Implementation should be done with paintBlocks
   *
   * @param level The level
   * @param x x position
   * @param y y position
   * @param z z position
   * @param type Type of block
   */
  public abstract void paint(Player player, Level level, int x, int y, int z, int mode, int type);

  /**
   * Set the radius
   *
   * @return Whether the radius was set
   */
  public abstract boolean setRadius(int newRadius);

  /**
   * Sets the width
   *
   * @return Return the width that was set
   */
  public abstract int setWidth(int newWidth);

  /**
   * Sets the height
   *
   * @return Return the height that was set
   */
  public abstract int setHeight(int newHeight);

  /**
   * Sets the length
   *
   * @return Return the length that was set
   */
  public abstract int setLength(int newLength);

  /**
   * Sets whether this brush will delete also
   *
   * @return The old value
   */
  public abstract boolean useForDelete(boolean enable);

  /**
   * Returns whether this brush is used to delete
   *
   * @return If used for delete
   */
  public abstract boolean getUseForDelete();

  /**
   * Gets the width
   *
   * @return The width
   */
  public abstract int getWidth();

  /**
   * Gets the length
   *
   * @return The length
   */
  public abstract int getLength();

  /**
   * Gets the height
   *
   * @return The height
   */
  public abstract int getHeight();

  /**
   * Gets the radius
   *
   * @return The radius
   */
  public abstract int getRadius();
}
