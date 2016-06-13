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


import org.opencraft.server.model.BlockBehaviour;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.model.Level;

/**
 * Handles the spreading of grass blocks to adjacent, sun-exposed dirt blocks.
 * @author Brett Russell
 */
public class GrassBehaviour implements BlockBehaviour {
	
	@Override
	public void handleDestroy(Level level, int x, int y, int z, int type) {
		
	}
	
	@Override
	public void handlePassive(Level level, int x, int y, int z, int type) {
		level.queueActiveBlockUpdate(x, y, z);
	}
	
	@Override
	public void handleScheduledBehaviour(Level level, int x, int y, int z, int type) {
		// do we need to die?
		if (level.getLightDepth(x, y) > z) {
			level.setBlock(x, y, z, BlockConstants.DIRT);
			return;
		}
		
		// represents the different directions grass can spread
		// x, y, z
		int[][] spreadRules = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
		
		// spread
		for (int i = 0; i <= spreadRules.length - 1; i++) {
			boolean found = false;
			if (level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2]) == BlockConstants.DIRT) {
				if (z + spreadRules[i][2] < level.getLightDepth(x + spreadRules[i][0], y + spreadRules[i][1])) {
					found = true;
				}
				if (BlockManager.getBlockManager().getBlock(level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + 1)).isLiquid()) {
					found = true;
				}
				
				if (!found) {
					level.setBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2], BlockConstants.GRASS);
				}
			}
		}
	}
}
