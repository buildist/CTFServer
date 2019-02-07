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

/**
 * Represents an individual block type.
 *
 * @author Brett Russell
 */
public class BlockDefinition {

  /** The block name. */
  private String name;
  /** The block ID. */
  private int bid;
  /** The block's solidity. */
  private boolean solid;
  /** The block's fluidity. */
  private boolean liquid;
  /** The block's transparency. */
  private boolean blocksLight;
  /** The block's size. */
  private boolean halfBlock;
  /** The block's "full" version if it is a halfblock. */
  private int fullCounterpart;
  /** The block's behaviour, as a string. */
  private String behaviourName;
  /** The block's behaviour. */
  private transient BlockBehaviour behaviour;
  /** The block's periodic physics check state. */
  private boolean doesThink;
  /** Whether this block is a plant (whether it cares about the blocks above it) */
  private boolean isPlant;
  /** The timer, in milliseconds, on which this block thinks. */
  private long thinkTimer;
  private boolean isCustom;

  /** Constructor. */
  public BlockDefinition() {}

  public BlockDefinition(
      String name,
      int bid,
      boolean solid,
      boolean liquid,
      boolean blocksLight,
      boolean halfBlock,
      boolean doesThink,
      boolean isPlant,
      long thinkTimer,
      boolean isCustom,
      int fullCounterpart,
      String behaviourName)
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    this.name = name;
    this.bid = bid;
    this.solid = solid;
    this.liquid = liquid;
    this.blocksLight = blocksLight;
    this.halfBlock = halfBlock;
    this.doesThink = doesThink;
    this.isPlant = isPlant;
    this.thinkTimer = thinkTimer;
    this.isCustom = isCustom;
    this.fullCounterpart = fullCounterpart;
    this.behaviourName = behaviourName.trim();
    if (behaviourName.length() > 0) {
      this.behaviour = (BlockBehaviour) Class.forName(this.behaviourName).newInstance();
    }
  }

  /**
   * A method that exists for legacy reasons.
   *
   * @param id The block id.
   * @return The block definition.
   */
  public static BlockDefinition forId(int id) {
    return BlockManager.getBlockManager().getBlock(id);
  }

  /**
   * Resolves this object.
   *
   * @return A resolved object.
   */
  private Object readResolve()
      throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    return new BlockDefinition(
        name,
        bid,
        solid,
        liquid,
        blocksLight,
        halfBlock,
        doesThink,
        isPlant,
        thinkTimer,
        false,
        fullCounterpart,
        behaviourName);
  }

  /**
   * Gets the name.
   *
   * @return The name.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the ID.
   *
   * @return The ID.
   */
  public int getId() {
    return bid;
  }

  /**
   * Gets the solidity.
   *
   * @return The solidity.
   */
  public boolean isSolid() {
    return solid;
  }

  /**
   * Gets the fluidity.
   *
   * @return The fluidity.
   */
  public boolean isLiquid() {
    return liquid;
  }

  /**
   * Gets the transparency.
   *
   * @return The transparency.
   */
  public boolean doesBlockLight() {
    return blocksLight;
  }

  /**
   * Gets the size.
   *
   * @return The size.
   */
  public boolean isHalfBlock() {
    return halfBlock;
  }

  /**
   * Gets the periodic physics check state.
   *
   * @return The... yeah.
   */
  public boolean doesThink() {
    return doesThink;
  }

  /**
   * Apply passive physics.
   *
   * @param level The level.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   */
  public void behavePassive(Level level, int x, int y, int z) {
    if (behaviour == null) {
      return;
    }
    this.behaviour.handlePassive(level, x, y, z, this.bid);
  }

  /**
   * Apply physics on block destruction.
   *
   * @param level The level.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   */
  public void behaveDestruct(Level level, int x, int y, int z) {
    if (behaviour == null) {
      return;
    }
    this.behaviour.handleDestroy(level, x, y, z, this.bid);
  }

  /**
   * Apply periodic physics.
   *
   * @param level The level.
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param z The z coordinate.
   */
  public void behaveSchedule(Level level, int x, int y, int z) {
    if (behaviour == null) {
      return;
    }
    this.behaviour.handleScheduledBehaviour(level, x, y, z, this.bid);
  }

  /**
   * Gets the speed at which this block "thinks."
   *
   * @return The think speed.
   */
  public long getTimer() {
    return thinkTimer;
  }

  public boolean isCustom() {
    return isCustom;
  }

  /**
   * Gets this blocks' "plant" state.
   *
   * @return Whether or not this block is a plant.
   */
  public boolean isPlant() {
    return isPlant;
  }

  /**
   * Gets the fullsize counterpart for this block.
   *
   * @return The fullsize counterpart ID.
   */
  public int getFullCounterpart() {
    if (halfBlock) {
      return fullCounterpart;
    } else {
      return 0;
    }
  }
}
