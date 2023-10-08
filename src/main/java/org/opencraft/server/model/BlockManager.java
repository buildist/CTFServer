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
import org.opencraft.server.io.PersistenceManager;

import java.util.LinkedList;
import java.util.List;

import static org.opencraft.server.model.CustomBlockDefinition.CUSTOM_BLOCKS;

/**
 * A class which manages <code>BlockDefinition</code>s and <code>BlockBehaviour</code>s.
 *
 * @author Graham Edgecombe
 * @author Brett Russell
 */
public final class BlockManager {

  /** The packet manager instance. */
  private static final BlockManager INSTANCE =
      (BlockManager)
          PersistenceManager.getPersistenceManager().load(Constants.ROOT_PATH + "/blocks.xml");

  /** A list of the blocks. */
  private List<BlockDefinition> blockList = new LinkedList<BlockDefinition>();
  /** The block array (faster access by opcode than list iteration). */
  private transient BlockDefinition[] blocksArray;

  /**
   * Gets the packet manager instance.
   *
   * @return The packet manager instance.
   */
  public static BlockManager getBlockManager() {
    return INSTANCE;
  }

  /**
   * Resolves the block manager after deserialization.
   *
   * @return The resolved object.
   */
  private Object readResolve() {
    blocksArray = new BlockDefinition[1024];
    for (BlockDefinition def : blockList) {
      blocksArray[def.getId()] = def;
    }
    for (CustomBlockDefinition block : CUSTOM_BLOCKS) {
      addCustomBlock(block);
    }
    return this;
  }

  /**
   * Gets an incoming block definition.
   *
   * @param id The id.
   * @return The block definition.
   */
  public BlockDefinition getBlock(int id) {
    return blocksArray[id];
  }

  public boolean isCustom(int id) {
    return blocksArray[id].isCustom();
  }

  public void addCustomBlock(CustomBlockDefinition customBlockDefinition) {
    try {
      // Check if the custom block has any special properties
      boolean isHalfBlock = false;
      boolean doesThink = false;
      boolean isPlant = false;
      long thinkTimer = 0;
      int fullCounterpart = 0;
      String behaviourName = "";

      if (getBlock(customBlockDefinition.id) != null) {
        isHalfBlock = getBlock(customBlockDefinition.id).isHalfBlock();
        doesThink = getBlock(customBlockDefinition.id).doesThink();
        isPlant = getBlock(customBlockDefinition.id).isPlant();
        thinkTimer = getBlock(customBlockDefinition.id).getTimer();
        fullCounterpart = getBlock(customBlockDefinition.id).getFullCounterpart();

        // Check if the custom block overrides a default block
        // If so, use the original behaviour
        if (customBlockDefinition.id < 66) {
          behaviourName = getBlock(customBlockDefinition.id).getBehaviourName().trim();
        }
      }

      blocksArray[customBlockDefinition.id] =
          new BlockDefinition(
              customBlockDefinition.name,
              customBlockDefinition.id,
              customBlockDefinition.solid == Constants.BLOCK_SOLIDITY_SOLID,
              customBlockDefinition.solid == Constants.BLOCK_SOLIDITY_SWIM_THROUGH,
              customBlockDefinition.solid == Constants.BLOCK_SOLIDITY_SOLID,
              isHalfBlock,
              doesThink,
              isPlant,
              thinkTimer,
              true,
              fullCounterpart,
              behaviourName);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
