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


import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

/**
 * Shortcut for creation of Brushes Only constructor and paintBlocks are necessary
 *
 * @author S�ren Enevoldsen
 */
public abstract class BrushAdapter extends Brush {

  protected boolean useForDelete = false;
  protected int minWidth = 1;
  protected int minHeight = 1;
  protected int minLength = 1;

  protected int maxWidth = 7;
  protected int maxHeight = 7;
  protected int maxLength = 7;

  protected int radius = 0;
  protected int width = 0;
  protected int height = 0;
  protected int length = 0;

  /*
   * If even dimensions, prioriterize right, front and up
   */
  protected int offLeft = 0;
  protected int offRight = 0;
  protected int offBack = 0;
  protected int offForward = 0;
  protected int offTop = 0;
  protected int offBot = 0;

  /*
   * Offsets for paintBlock
   */
  protected int xOffStart;
  protected int xOffEnd;
  protected int yOffStart;
  protected int yOffEnd;
  protected int zOffStart;
  protected int zOffEnd;

  /**
   * Creates the brush adapter with the default radius.
   */
  public BrushAdapter() {
    setRadius(1);
  }

  /**
   * Creates the brush adapter with the specified radius.
   *
   * @param radius The radius.
   */
  public BrushAdapter(int radius) {
    setRadius(radius);
  }

  /**
   * Creates the brush adapter with the specified width, length and height.
   *
   * @param width  The width.
   * @param length The length.
   * @param height The height.
   */
  public BrushAdapter(int width, int length, int height) {
    setWidth(width);
    setLength(length);
    setHeight(height);
  }

  /**
   * Bounds a value to min, max or between.
   *
   * @value The value
   * @min Minimum value
   * @max Maximum value
   */
  protected int clamp(int value, int min, int max) {
    if (value > max)
      return max;
    if (value < min)
      return min;
    return value;
  }

  /**
   * Keeps a width between min and max
   *
   * @return The new width
   */
  protected int clampWidth(int width) {
    return clamp(width, minWidth, maxWidth);
  }

  /**
   * Keeps a height between min and max
   *
   * @return The new height
   */
  protected int clampHeight(int height) {
    return clamp(height, minHeight, maxHeight);
  }

  /**
   * Keeps a length between min and max
   *
   * @return The new length
   */
  protected int clampLength(int length) {
    return clamp(length, minLength, maxLength);
  }

  /**
   * Checks to see if a type can be built upon
   *
   * @param type The type questioned
   * @return Returns true if the type is build-able (changeable)
   */
  protected boolean typeIsBuildable(int type) {
    if (type == BlockConstants.AIR || type == BlockConstants.WATER || type == BlockConstants
        .STILL_WATER)
      return true;
    return false;
  }

  /**
   * Checks to see if a block is build-able
   *
   * @param x x position
   * @param y y position
   * @param z z position
   * @return Returns true if block at position is build-able (changeable)
   */
  protected boolean positionIsBuildable(int x, int y, int z) {
    return typeIsBuildable(World.getWorld().getLevel().getBlock(x, y, z));
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public void paint(Player player, Level level, int x, int y, int z, int mode, int type) {
    boolean add = (mode == 1 ? true : false);
    if (add)
      paintBlocks(player, level, x, y, z, add, type);
    else if (useForDelete)
      paintBlocks(player, level, x, y, z, add, BlockConstants.AIR);
    else
      World.getWorld().getLevel().setBlock(x, y, z, BlockConstants.AIR);
  }

  /**
   * Paints the blocks Should be implemented
   */
  protected abstract void paintBlocks(Player player, Level level, int x, int y, int z, boolean
      add, int type);

  @Override
  public int setHeight(int newHeight) {
    height = clampHeight(newHeight);
    offTop = (int) Math.ceil((height - 1) / 2.0);
    offBot = (int) Math.floor((height - 1) / 2.0);
    return height;
  }

  @Override
  public int setLength(int newLength) {
    length = clampLength(newLength);
    offForward = (int) Math.ceil((length - 1) / 2.0);
    offBack = (int) Math.floor((length - 1) / 2.0);
    return length;
  }

  @Override
  public int setWidth(int newWidth) {
    width = clampWidth(newWidth);
    offRight = (int) Math.ceil((width - 1) / 2.0);
    offLeft = (int) Math.floor((width - 1) / 2.0);
    return width;
  }

  @Override
  public boolean setRadius(int newRadius) {
    // Prevent too great radius
    radius = clamp(newRadius, (minWidth - 1) / 2, maxWidth);
    radius = clamp(radius, (minHeight - 1) / 2, maxHeight);
    radius = clamp(radius, (minLength - 1) / 2, maxLength);

    setHeight(newRadius * 2 + 1);
    setLength(newRadius * 2 + 1);
    setWidth(newRadius * 2 + 1);
    return true;
  }

  @Override
  public boolean useForDelete(boolean enable) {
    boolean oldValue = useForDelete;
    useForDelete = enable;
    return oldValue;
  }

  @Override
  public int getRadius() {
    return radius;
  }

  @Override
  public boolean getUseForDelete() {
    return useForDelete;
  }

  /**
   * Sets the start and end offsets using the
   *
   * @param player The player
   */
  protected void setOffsetsFromPerspective(Player player) {
    zOffStart = -offBot;
    zOffEnd = offTop;

    int rotation = player.getRotation().getRotation();

		/*
     * START HAVE TO BE LOWEST
		 */
    // looking x-pos forward y-pos right
    if (rotation >= 32 && rotation < 96) {
      xOffStart = -offBack;
      xOffEnd = offForward;
      yOffStart = -offLeft;
      yOffEnd = offRight;
    }
    // Looking x-pos right and y-pos behind
    else if (rotation >= -32 && rotation < 32) {
      xOffStart = -offLeft;
      xOffEnd = offRight;
      yOffStart = -offForward;
      yOffEnd = offBack;
    }
    // Looking x-pos behind and y-pos left
    else if (rotation >= -96 && rotation < -32) {
      xOffStart = -offForward;
      xOffEnd = offBack;
      yOffStart = -offRight;
      yOffEnd = offLeft;
    }
    // Looking x-pos left and y-pos forward
    else {
      xOffStart = -offLeft;
      xOffEnd = offRight;
      yOffStart = -offBack;
      yOffEnd = offForward;
    }

  }

}
