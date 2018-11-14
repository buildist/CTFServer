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
package org.opencraft.server.visualizer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import org.opencraft.server.Configuration;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.MapController;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class MapRenderer {
  static final int scale = 4;
  static final int margin = 100;
  static int imgWidth;
  static int imgHeight;
  static Color[] blockColors = {Color.white, new Color(109, 111, 112), new Color(111, 174, 69),
      new Color(113, 84, 62), new Color(85, 85, 85), new Color(170, 138, 85), new Color(112, 117,
      33), Color.darkGray, new Color(80, 99, 169), new Color(86, 105, 174), new Color(252, 70,
      14), new Color(251, 69, 13), new Color(217, 208, 158), new Color(136, 126, 125), new Color
      (179, 172, 109), new Color(209, 184, 167), new Color(95, 95, 95), new Color(86, 68, 41),
      new Color(45, 145, 32), new Color(165, 164, 56), new Color(212, 255, 255), new Color(226,
      52, 52), new Color(219, 134, 50), new Color(227, 227, 52), new Color(135, 222, 51), new
      Color(49, 221, 49), new Color(50, 222, 135), new Color(51, 221, 221), new Color(104, 162,
      221), new Color(121, 121, 224), new Color(135, 50, 222), new Color(173, 73, 221), new Color
      (224, 51, 224), new Color(216, 48, 132), new Color(116, 116, 116), new Color(160, 160, 160)
      , new Color(217, 217, 217), new Color(169, 188, 0), new Color(162, 31, 12), new Color(167,
      126, 98), new Color(138, 31, 34), new Color(223, 164, 43), new Color(194, 194, 194), new
      Color(169, 169, 169), new Color(165, 165, 165), new Color(205, 120, 95), new Color(219, 68,
      26), new Color(66, 62, 37), new Color(117, 133, 117), new Color(15, 15, 24), new Color(175,
      159, 96), new Color(255, 183, 18), new Color(31, 85, 255), new Color(248, 66, 10), new
      Color(151, 107, 34), new Color(180, 180, 180), new Color(135, 163, 167), new Color(31, 168,
      154), new Color(107, 88, 59), new Color(92, 192, 5), new Color(75, 41, 14)};
  static Color lineColor = new Color(128, 0, 128);
  static BufferedImage red;
  static BufferedImage blue;
  static Composite composite;
  static HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
  static HashMap<String, Graphics2D> graphics = new HashMap<String, Graphics2D>();
  static HashMap<String, MapData> meta = new HashMap<String, MapData>();

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
    else
      return Integer.parseInt(level.props.getProperty("redFlagY")) + 1;
  }

  public static void main(String[] args) {
    try {
      Configuration.readConfiguration();
      red = ImageIO.read(new File("red.png"));
      blue = ImageIO.read(new File("blue.png"));
    } catch (FileNotFoundException ex) {
    } catch (IOException ex) {
    }
    MapController.create();
    for (String mapName : MapController.levelNames) {
      Level level = MapController.getLevel(mapName);
      imgWidth = level.getWidth() * scale + margin * 2;
      imgHeight = level.getHeight() * scale + margin * 2;
      BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
      images.put(mapName, img);
      meta.put(mapName, new MapData());
      Graphics2D g = img.createGraphics();
      graphics.put(mapName, g);
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
    }
    try {
      composite = new AdditiveComposite();
      BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream("kills" +
          ".txt")));
      String l;
      while ((l = r.readLine()) != null) {
        String[] parts = l.split(" ");
        parts[0] = parts[0].replace("more/", "");
        if (parts[0].equals("spire"))
          parts[0] = "ultiduo";
        try {
          BufferedImage bg = images.get(parts[0]);
          Graphics2D g = graphics.get(parts[0]);
          MapData metadata = meta.get(parts[0]);
          int team = Integer.parseInt(parts[1]);
          float x = (Integer.parseInt(parts[2]) - 16) / 32f;
          float y = (Integer.parseInt(parts[3]) - 16) / 32f;
          float z = (Integer.parseInt(parts[4]) - 16) / 32f;
          boolean hasFlag = Boolean.parseBoolean(parts[5]);
          Color c;
          if (team == 0) {
            c = new Color(255, 0, 0, 64);
            metadata.redDeaths++;
          } else {
            c = new Color(0, 0, 255, 64);
            metadata.blueDeaths++;
          }
          Color c2;
          if (team == 0)
            c2 = new Color(255, 0, 0, 128);
          else
            c2 = new Color(0, 0, 255, 128);
          if (g == null)
            System.err.println(parts[0]);
          g.setColor(c);
          //g.setComposite(composite);
          int ix = imgX(x);
          int iy = imgY(y);
          final int radius = 2;
          final int radius2 = 4;
          g.fillOval(ix - radius, iy - radius, radius * 2, radius * 2);
          if (hasFlag) {
            g.setColor(c2);
            g.setComposite(AlphaComposite.SrcOver);
            g.drawOval(ix - radius2, iy - radius2, radius2 * 2, radius2 * 2);
            metadata.flagDeaths++;
          }
        } catch (Exception ex) {
          System.out.println(parts[0]);
        }
      }
      for (String mapName : images.keySet()) {
        Graphics2D g = graphics.get(mapName);
        MapData metadata = meta.get(mapName);
        g.setColor(Color.white);
        g.drawString(mapName, 100, 40);
        int x = images.get(mapName).getWidth() - 200;
        int totalDeaths = metadata.redDeaths + metadata.blueDeaths;
        int redP = (int) Math.round(((double) metadata.redDeaths / totalDeaths) * 100);
        int blueP = (int) Math.round(((double) metadata.blueDeaths / totalDeaths) * 100);
        int flagP = (int) Math.round(((double) metadata.flagDeaths / totalDeaths) * 100);
        g.drawString("Red Deaths: " + metadata.redDeaths + "(" + redP + "%)", x, 40);
        g.drawString("Blue Deaths: " + metadata.blueDeaths + "(" + blueP + "%)", x, 56);
        g.drawString("Flag Deaths: " + flagP + "%", x, 72);
        ImageIO.write(images.get(mapName), "png", new FileOutputStream(mapName + ".png"));
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  static class MapData {
    public int redDeaths;
    public int blueDeaths;
    public int flagDeaths;
  }
}
    