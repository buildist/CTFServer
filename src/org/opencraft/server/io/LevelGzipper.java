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

import org.apache.mina.core.buffer.IoBuffer;
import org.opencraft.server.Configuration;
import org.opencraft.server.Constants;
import org.opencraft.server.model.CustomBlockDefinition;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

/**
 * A utility class for gzipping levels.
 *
 * @author Graham Edgecombe
 */
public final class LevelGzipper {

  private static final LevelGzipper INSTANCE = new LevelGzipper();
  private ExecutorService service = Executors.newCachedThreadPool();

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
              "&0-hax",
              session.getPlayer().isOp());
      session.getActionSender().sendHackControl(true);
    }
    session.levelSent = true;
    final int width = level.getWidth();
    final int height = level.getHeight();
    final int depth = level.getDepth();
    session.getActionSender().sendLevelInit();

    for (CustomBlockDefinition blockDef : level.customBlockDefinitions) {
      session.getActionSender().sendDefineBlockExt(blockDef);
    }
    for (CustomBlockDefinition blockDef : CustomBlockDefinition.CUSTOM_BLOCKS) {
      session.getActionSender().sendDefineBlockExt(blockDef);
    }
    service.submit(
        new Runnable() {
          @Override
          public void run() {
            try {
              ByteArrayOutputStream out = new ByteArrayOutputStream();
              int size = width * height * depth;
              DataOutputStream os = new DataOutputStream(new GZIPOutputStream(out));
              os.writeInt(size);
              os.write(level.getBlocks1D());
              os.close();
              byte[] data = out.toByteArray();
              IoBuffer buf = IoBuffer.allocate(data.length);
              buf.put(data);
              buf.flip();
              while (buf.hasRemaining()) {
                int len = buf.remaining();
                if (len > 1024) {
                  len = 1024;
                }
                byte[] chunk = new byte[len];
                buf.get(chunk);
                int percent = (int) ((double) buf.position() / (double) buf.limit() * 255D);
                session.getActionSender().sendLevelBlock(len, chunk, percent);
              }
              String texturePack =
                  (level.textureUrl != null && !level.textureUrl.isEmpty())
                      ? level.textureUrl
                      : Configuration.getConfiguration().getEnvTexturePack();
              if (session.isExtensionSupported("EnvMapAppearance", 2))
                session.getActionSender().sendMapAppearanceV2(texturePack);
              else if (session.isExtensionSupported("EnvMapAppearance", 1))
                session.getActionSender().sendMapAppearanceV1();
              if (session.isExtensionSupported("EnvColors"))
                session.getActionSender().sendMapColors();
              session.getActionSender().sendLevelFinish();

              session.getActionSender().sendBlockPermissions(0, true, true);
              session.getActionSender().sendBlockPermissions(7, false, false);
              session.getActionSender().sendBlockPermissions(8, false, false);
              session.getActionSender().sendBlockPermissions(10, false, false);
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_MINE, true, false);
              session
                  .getActionSender()
                  .sendBlockPermissions(Constants.BLOCK_MINE_RED, false, false);
              session
                  .getActionSender()
                  .sendBlockPermissions(Constants.BLOCK_MINE_BLUE, false, false);
              session.getActionSender().sendBlockPermissions(Constants.BLOCK_RED_FLAG, false, true);
              session
                  .getActionSender()
                  .sendBlockPermissions(Constants.BLOCK_BLUE_FLAG, false, true);

              session.getPlayer().getLocalEntities().clear();
            } catch (IOException ex) {
              session.getActionSender().sendLoginFailure("Failed to gzip level. Please try again.");
            }
          }
        });
    // if(session.isExtensionSupported("HackControl"))
    //      session.getActionSender().sendHackControl(session.getPlayer().isOp());
  }
}
