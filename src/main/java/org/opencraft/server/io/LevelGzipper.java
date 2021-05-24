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
package org.opencraft.server.io;

import org.opencraft.server.Constants;
import org.opencraft.server.model.CustomBlockDefinition;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.net.ActionSender;
import org.opencraft.server.net.MinecraftSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class for gzipping levels.
 *
 * @author Graham Edgecombe
 */
public final class LevelGzipper {

  private static final LevelGzipper INSTANCE = new LevelGzipper();
  private ExecutorService service = Executors.newCachedThreadPool();

  private static final int[] DEFAULT_RESTRICTED_BLOCKS = new int[]{
      7, 8, 10, Constants.HIT_RED, Constants.HIT_BLUE, Constants.LASER_RED, Constants.LASER_BLUE, 60};

  public static LevelGzipper getLevelGzipper() {
    return INSTANCE;
  }

  public void gzipLevel(final MinecraftSession session) {
    final Level level = World.getWorld().getLevel();
    if (session.levelSent) {
      session
          .getActionSender()
          .sendLoginResponse(
              Constants.PROTOCOL_VERSION,
              "Next map: " + level.id,
              "&0-hax" + level.getMotd(),
              session.getPlayer().isOp());
      session.getActionSender().sendHackControl(true);
    }
    final int width = level.getWidth();
    final int height = level.getHeight();
    final int depth = level.getDepth();
    int length = width * height * depth;
    session.getActionSender().sendLevelInit(length);

    for (CustomBlockDefinition blockDef : level.customBlockDefinitions) {
      session.getActionSender().sendDefineBlockExt(blockDef);
    }
    for (CustomBlockDefinition blockDef : CustomBlockDefinition.CUSTOM_BLOCKS) {
      session.getActionSender().sendDefineBlockExt(blockDef);
      session.getActionSender().sendInventoryOrder(blockDef.id, blockDef.inventoryOrder);
    }
    service.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              sendBlocks(level.getCompressedBlocks0(), session.getActionSender(), false);
              sendBlocks(level.getCompressedBlocks1(), session.getActionSender(), true);

              if (session.isExtensionSupported("EnvMapAspect", 1))
                session.getActionSender().sendMapAspect();
              if (session.isExtensionSupported("EnvColors"))
                session.getActionSender().sendMapColors();
              session.getActionSender().sendLevelFinish();

              for (int id : level.usedSolidTypes) {
                session.getActionSender().sendBlockPermissions(id, false, false);
              }

              for (int id : level.usedBreakableTypes) {
                session.getActionSender().sendBlockPermissions(id, true, true);
              }

              for (int type : DEFAULT_RESTRICTED_BLOCKS) {
                session.getActionSender().sendBlockPermissions(type, false, false);
              }
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_RED_FLAG, false, true);
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_BLUE_FLAG, false, true);
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_MINE_RED, false, true);
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_MINE_BLUE, false, true);

              session.getActionSender().sendDefineEffect(
                  Constants.EFFECT_TNT,
                  0,
                  8,
                  0,
                  8,
                  255,
                  255,
                  255,
                  4,
                  100,
                  4,
                  1,
                  24,
                  10000,
                  3 * 10000,
                  1 * 10000,
                  2000,
                  0b00000000,
                  1
              );
              session.getActionSender().sendDefineEffect(
                  Constants.EFFECT_TNT_2,
                  0,
                  56,
                  8,
                  64,
                  255,
                  255,
                  255,
                  3,
                  5,
                  32,
                  0,
                  32,
                  200,
                  5 * 100,
                  1 * 5000,
                  10000,
                  0b00000000,
                  1
              );

              session.getPlayer().getLocalEntities().clear();
            } catch (IOException ex) {
              session.getActionSender().sendLoginFailure("Failed to gzip level. Please try again.");
            }
          }
        });
  }

  private static void sendBlocks(byte[] bytes, ActionSender sender, boolean isHighBytes) {
    int i = 0;
    while (i < bytes.length) {
      int len = Math.min(1024, bytes.length - 1);
      byte[] chunk = Arrays.copyOfRange(bytes, i, i + len);
      sender.sendLevelBlock(len, chunk, isHighBytes ? 1 : 0);
      i += len;
    }
  }
}
