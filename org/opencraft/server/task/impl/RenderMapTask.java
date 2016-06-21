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
package org.opencraft.server.task.impl;

import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;

import java.awt.image.BufferedImage;


public class RenderMapTask implements Runnable {
  static final int scale = 4;
  static final int margin = 0;
  static final Color[] blockColors = {Color.white, new Color(109, 111, 112), new Color(111, 174,
      69), new Color(113, 84, 62), new Color(85, 85, 85), new Color(170, 138, 85), new Color(112,
      117, 33), Color.darkGray, new Color(80, 99, 169), new Color(86, 105, 174), new Color(252,
      70, 14), new Color(251, 69, 13), new Color(217, 208, 158), new Color(136, 126, 125), new
      Color(179, 172, 109), new Color(209, 184, 167), new Color(95, 95, 95), new Color(86, 68,
      41), new Color(45, 145, 32), new Color(165, 164, 56), new Color(212, 255, 255), new Color
      (226, 52, 52), new Color(219, 134, 50), new Color(227, 227, 52), new Color(135, 222, 51),
      new Color(49, 221, 49), new Color(50, 222, 135), new Color(51, 221, 221), new Color(104,
      162, 221), new Color(121, 121, 224), new Color(135, 50, 222), new Color(173, 73, 221), new
      Color(224, 51, 224), new Color(216, 48, 132), new Color(116, 116, 116), new Color(160, 160,
      160), new Color(217, 217, 217), new Color(169, 188, 0), new Color(162, 31, 12), new Color
      (167, 126, 98), new Color(138, 31, 34), new Color(223, 164, 43), new Color(194, 194, 194),
      new Color(169, 169, 169), new Color(165, 165, 165), new Color(205, 120, 95), new Color(219,
      68, 26), new Color(66, 62, 37), new Color(117, 133, 117), new Color(15, 15, 24), new Color
      (175, 159, 96), new Color(255, 183, 18), new Color(31, 85, 255), new Color(248, 66, 10),
      new Color(151, 107, 34), new Color(180, 180, 180), new Color(135, 163, 167), new Color(31,
      168, 154), new Color(107, 88, 59), new Color(92, 192, 5), new Color(75, 41, 14)};
  static final Color lineColor = new Color(128, 0, 128);
  static final Color red = new Color(255, 0, 0);
  static final Color blue = new Color(0, 0, 255);
  public static BufferedImage mapImage = null;
  static int imgWidth;
  static int imgHeight;

  private static int imgX(double mapX) {
    return margin + (int) Math.round(mapX * scale);
  }

  private static int imgY(double mapY) {
    return margin + (int) Math.round(mapY * scale);
  }

  private static int getStartZ(Level level) {
    if (level.id.equals("hydro") || level.id.equals("pits") || level.id.equals("underworld") ||
        level.id.equals("207") || level.id.equals("arctic"))
      return level.ceiling;
    else if (level.id.equals("217"))
      return Integer.parseInt(level.props.getProperty("redFlagY")) + 20;
    else if (level.props.getProperty("isTDM") == null)
      return Integer.parseInt(level.props.getProperty("redFlagY")) + 1;
    else
      return Integer.parseInt(level.props.getProperty("buildCeiling"));
  }

  public void run() {
    while (true) {
      Level level = World.getWorld().getLevel();
      imgWidth = level.getWidth() * scale + margin * 2;
      imgHeight = level.getHeight() * scale + margin * 2;
      BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D g = img.createGraphics();
      int startZ = getStartZ(level);
      int z;
      int maxZ = Integer.MIN_VALUE;
      int minZ = Integer.MAX_VALUE;
      for (int x = 0; x < level.width; x++) {
        for (int y = 0; y < level.height; y++) {
          for (z = startZ; z > 0; z--) {
            if (level.getBlock(x, y, z) != 0) {
              if (z > maxZ)
                maxZ = x;
              if (level.getBlock(x, y, z + 1) == 0 && z < minZ)
                minZ = z;
            }
          }
        }
      }
      for (int x = 0; x < level.width; x++) {
        for (int y = 0; y < level.height; y++) {
          int block = 0;
          int overlayBlock = 0;
          int overlayZ = 0;
          for (z = startZ; z > 0; z--) {
            if ((block = level.getBlock(x, y, z)) != 0) {
              if ((block == 8 || block == 9 || block == 20)) {
                overlayBlock = block;
                if (overlayZ == 0)
                  overlayZ = z;
              } else
                break;
            }
          }
          double f = (double) (z - minZ) / (maxZ - minZ);
          f = f * 0.5 + 0.5;
          f = Math.max(Math.min(f, 1), 0);
          Color blockColor = blockColors[block];
          blockColor = new Color((int) (f * blockColor.getRed()), (int) (f * blockColor.getGreen
              ()), (int) (f * blockColor.getBlue()));
          g.setColor(blockColor);
          g.fillRect(imgX(x), imgY(y), scale, scale);
          if (overlayBlock != 0) {
            f = (double) (overlayZ - minZ) / (maxZ - minZ);
            f = f * 0.5 + 0.5;
            f = Math.max(Math.min(f, 1), 0);
            blockColor = blockColors[overlayBlock];
            blockColor = new Color((int) (f * blockColor.getRed()), (int) (f * blockColor
                .getGreen()), (int) (f * blockColor.getBlue()), 128);
            g.setColor(blockColor);
            g.fillRect(imgX(x), imgY(y), scale, scale);
          }
        }
      }
      int x = imgX(Integer.parseInt(level.getProp("divider").toString()));
      g.setColor(lineColor);
      g.drawLine(x, 0, x, img.getHeight());
      mapImage = img;
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        if (p.team != -1) {
          Position pos = p.getPosition().toBlockPos();
          int px = imgX(pos.getX());
          int py = imgY(pos.getY());
          g.setColor(Color.white);
          g.drawLine(px - 8, py, px + 8, py);
          g.drawLine(px, py - 8, px, py + 8);
          g.setColor(Color.black);
          g.drawString(p.getName(), px + 1 + 4, py + 1 - 4);
          g.setColor(p.team == 0 ? red : blue);
          g.drawString(p.getName(), px + 4, py - 4);
        }
      }
      try {
        Thread.sleep(20 * 1000);
      } catch (InterruptedException ex) {
      }
    }
  }
}
