package org.opencraft.server.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.opencraft.server.Server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TexturePackHandler {

  public static final int CTF_BLOCK_SIZE_PX = 16;
  public static final int TEXTURE_WIDTH_BLOCKS = 16;
  public static final int TEXTURE_HEIGHT_BLOCKS = 32;

  public static boolean hasCustomTexturePack(String map) {
    File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
    return texturePackFile.exists();
  }

  protected static BufferedImage mergeTerrain(Image ctfTerrain, Image source) {
    /*
     * So...
     * first find size of texture file. Standard seems to be 16 tiles across.
     *    pixels per block = width in pixels / 16
     * Calculate ctf blocks height. We'll just say the default ctf texture has to be
     * 16 blocks by default. so the ctf blocks height is going to be
     *    number of rows = height in pixels / 16
     * Scale and place in bottom left.
     *    scale factor = pixels per block / 16
     * Bottom left is going to be 512 - 16 * number of rows
     */
    int pxPerBlock = source.getWidth(null) / TEXTURE_WIDTH_BLOCKS;

    // Get number of rows we are going to take up.
    int ctfRows = ctfTerrain.getHeight(null) / CTF_BLOCK_SIZE_PX;
    // Scale CTF image if needed.
    int scaleFactor = pxPerBlock / CTF_BLOCK_SIZE_PX;
    ctfTerrain = ctfTerrain.getScaledInstance(ctfTerrain.getWidth(null) * scaleFactor,
        ctfTerrain.getHeight(null) * scaleFactor, Image.SCALE_DEFAULT);

    // Make it a 16x32 texture by default I guess?
    BufferedImage target = new BufferedImage(pxPerBlock * TEXTURE_WIDTH_BLOCKS,
        pxPerBlock * TEXTURE_HEIGHT_BLOCKS, BufferedImage.TYPE_INT_ARGB);

    Graphics2D graphics = target.createGraphics();
    // Draw original image on first.
    graphics.drawImage(source, 0, 0, null);

    graphics.setComposite(AlphaComposite.Src);

    // Draw the scaled CTF Texture onto it.
    graphics.drawImage(ctfTerrain, 0, (TEXTURE_HEIGHT_BLOCKS - ctfRows) * pxPerBlock, null);

    return target;
  }

  public static void createPatchedTexturePack(String map) {
    try {
      File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
      File outputFile = new File("texturepacks_cache/terrain_" + map + ".zip");
      if (outputFile.exists()) {
        return;
      }

      File fontFile = new File("texturepack_patch/default.png");
      File particlesFile = new File("texturepack_patch/particles.png");
      Image ctfTerrain = ImageIO.read(new File("texturepack_patch/ctf_terrain.png"));

      ZipFile in = new ZipFile(texturePackFile);
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));
      Enumeration<? extends ZipEntry> entries = in.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        out.putNextEntry(new ZipEntry(entry.getName()));
        switch (entry.getName()) {
          case "default.png":
            Files.copy(fontFile.toPath(), out);
            break;
          case "particles.png":
            Files.copy(particlesFile.toPath(), out);
            break;
          case "terrain.png":
            DataInputStream terrainData = new DataInputStream(in.getInputStream(entry));
            Image source = ImageIO.read(terrainData);
            BufferedImage imageData = mergeTerrain(ctfTerrain, source);
            ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
            ImageIO.write(imageData, "png", imgOutput);
            out.write(imgOutput.toByteArray());
            terrainData.close();
            break;
          default:
            DataInputStream dataIn = new DataInputStream(in.getInputStream(entry));
            byte[] bytes = new byte[(int) entry.getSize()];
            dataIn.readFully(bytes);
            out.write(bytes);
            dataIn.close();
            break;
        }
      }
      in.close();
      out.close();
    } catch (IOException ex) {
      Server.log(ex);
    }
  }
}
