package org.opencraft.server.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.opencraft.server.Server;
import org.opencraft.server.game.impl.GameSettings;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * What this does in "simple" terms:
 *  - Takes the base terrain.png from a texture pack .zip
 *  - Replaces red/blue wool with whatever team1/team2's wool should be
 *  - Draws the CTF blocks over the terrain (vines, crates, purple wool etc.)
 *  - Injects clan-specific textures (mine/flag/TNT etc.) over those CTF blocks
 *  - Repackages everything back into a .zip for the client to use
 *
 * Think of it as: "runtime texture pack patcher", if that makes it any easier to understand.
 */
public class TexturePackHandler {

  public static final int CTF_BLOCK_SIZE_PX = 16;
  public static final int TEXTURE_WIDTH_BLOCKS = 16;
  public static final int TEXTURE_HEIGHT_BLOCKS = 32;

  public static boolean hasCustomTexturePack(String map) {
    File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
    return texturePackFile.exists();
  }

  /**
   * This maps a clan color name -> where that color lives inside
   * clan_blocks.png.
   *
   * Each color is a strip of 5 textures formatted next to each other:
   *   mine, flag, side TNT, top TNT, bottom TNT (in that order)
   *
   * Each tile is 16x16, so we can just increment x by 16 to get the index within that strip.
   */
  private static final Map<String, Point> CLAN_COLOR_POSITIONS = new HashMap<>();

  static {
    CLAN_COLOR_POSITIONS.put("MAROON", new Point(0, 0));
    CLAN_COLOR_POSITIONS.put("RED", new Point(80, 0));
    CLAN_COLOR_POSITIONS.put("ORANGE", new Point(160, 0));
    CLAN_COLOR_POSITIONS.put("GOLD", new Point(240, 0));

    CLAN_COLOR_POSITIONS.put("YELLOW", new Point(0, 16));
    CLAN_COLOR_POSITIONS.put("LIME", new Point(80, 16));
    CLAN_COLOR_POSITIONS.put("GREEN", new Point(160, 16));
    CLAN_COLOR_POSITIONS.put("TURQUOISE", new Point(240, 16));

    CLAN_COLOR_POSITIONS.put("CYAN", new Point(0, 32));
    CLAN_COLOR_POSITIONS.put("BLUE", new Point(80, 32));
    CLAN_COLOR_POSITIONS.put("NAVY", new Point(160, 32));
    CLAN_COLOR_POSITIONS.put("PURPLE", new Point(240, 32));

    CLAN_COLOR_POSITIONS.put("PINK", new Point(0, 48));
    CLAN_COLOR_POSITIONS.put("WHITE", new Point(80, 48));
    CLAN_COLOR_POSITIONS.put("SILVER", new Point(160, 48));
    CLAN_COLOR_POSITIONS.put("GRAY", new Point(240, 48));

    CLAN_COLOR_POSITIONS.put("BLACK", new Point(0, 64));
  }

  private static final Map<String, Rectangle> WOOL_SOURCES = Map.of(
      "RED", new Rectangle(0, 64, 16, 16),
      "ORANGE", new Rectangle(16, 64, 16, 16),
      "YELLOW", new Rectangle(32, 64, 16, 16),
      "LIME", new Rectangle(64, 64, 16, 16),
      "CYAN", new Rectangle(96, 64, 16, 16),
      "BLUE", new Rectangle(112, 64, 16, 16),
      "PURPLE", new Rectangle(160, 64, 16, 16),
      "PINK", new Rectangle(192, 64, 16, 16)
  );

  private static final Rectangle TEAM1_WOOL_DST_BLOCK = new Rectangle(0, 4, 1, 1);
  private static final Rectangle TEAM2_WOOL_DST_BLOCK = new Rectangle(7, 4, 1, 1);
  private static final Rectangle TEAM2_ALT_WOOL_DST_BLOCK = new Rectangle(3, 5, 1, 1);
  /**
   * Grabs a single 16x16 tile out of the clan sprite sheet and draws it
   * onto the final texture pack.
   *
   * Everything is scaled so it /should/ work for 16x / 32x / 64x packs etc.
   */
  private static void drawClanTexture(
      Graphics2D graphics,
      BufferedImage clanBlocks,
      int scaleFactor,
      int sourceX,
      int sourceY,
      int destX,
      int destY
  ) {
    BufferedImage tile = clanBlocks.getSubimage(
        sourceX,
        sourceY,
        16,
        16
    );

    graphics.drawImage(
        tile,
        destX * scaleFactor,
        destY * scaleFactor,
        16 * scaleFactor,
        16 * scaleFactor,
        null
    );
  }

  private static void drawTeamTextures(
      Graphics2D graphics,
      BufferedImage clanBlocks,
      int scaleFactor,
      String color,
      boolean team1
  ) {
    Point colorPos = CLAN_COLOR_POSITIONS.get(color.toUpperCase());

    if (colorPos == null) {
      throw new IllegalArgumentException("Unknown clan color: " + color);
    }

    int sourceX = colorPos.x;
    int sourceY = colorPos.y;

    int mineDestX       = team1 ? 0   : 16;
    int flagDestX       = team1 ? 48  : 64;
    int sideTntDestX    = team1 ? 96  : 176;
    int topTntDestX     = team1 ? 112 : 192;
    int bottomTntDestX  = team1 ? 128 : 208;

    int destY = 496;

    drawClanTexture(graphics, clanBlocks, scaleFactor,
        sourceX + 0 * 16, sourceY,
        mineDestX, destY);

    drawClanTexture(graphics, clanBlocks, scaleFactor,
        sourceX + 1 * 16, sourceY,
        flagDestX, destY);

    drawClanTexture(graphics, clanBlocks, scaleFactor,
        sourceX + 2 * 16, sourceY,
        sideTntDestX, destY);

    drawClanTexture(graphics, clanBlocks, scaleFactor,
        sourceX + 3 * 16, sourceY,
        topTntDestX, destY);

    drawClanTexture(graphics, clanBlocks, scaleFactor,
        sourceX + 4 * 16, sourceY,
        bottomTntDestX, destY);
  }

  private static BufferedImage toBuffered(Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }

    BufferedImage b = new BufferedImage(
        img.getWidth(null),
        img.getHeight(null),
        BufferedImage.TYPE_INT_ARGB
    );

    Graphics2D g = b.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();

    return b;
  }

  private static void copyRegion(
      BufferedImage img,
      Rectangle src,
      Rectangle dst
  ) {
    BufferedImage tile = img.getSubimage(src.x, src.y, src.width, src.height);

    Graphics2D g = img.createGraphics();
    try {
      g.drawImage(tile, dst.x, dst.y, null);
    } finally {
      g.dispose();
    }
  }

  protected static BufferedImage mergeTerrain(
      Image ctfTerrain,
      Image source
  ) throws IOException {

    String team1Color = GameSettings.getString("Team1Color");
    String team2Color = GameSettings.getString("Team2Color");

    int pxPerBlock = source.getWidth(null) / TEXTURE_WIDTH_BLOCKS;
    int ctfRows = ctfTerrain.getHeight(null) / CTF_BLOCK_SIZE_PX;

    // Some texture packs are HD so we need to scale our overlayed blocks and also offset their paste locations
    int scaleFactor = pxPerBlock / CTF_BLOCK_SIZE_PX;

    // Scale CTF terrain to match the texture pack resolution
    ctfTerrain = ctfTerrain.getScaledInstance(
        ctfTerrain.getWidth(null) * scaleFactor,
        ctfTerrain.getHeight(null) * scaleFactor,
        Image.SCALE_DEFAULT
    );

    // This is our beautiful mess of a final image :D
    BufferedImage target = new BufferedImage(
        pxPerBlock * TEXTURE_WIDTH_BLOCKS,
        pxPerBlock * TEXTURE_HEIGHT_BLOCKS,
        BufferedImage.TYPE_INT_ARGB
    );

    Graphics2D graphics = target.createGraphics();

    try {
      BufferedImage terrain = toBuffered(source); // Need to make a buffer so we can edit the terrain.png

      // To minimize confusion, let's replace red/blue wool with team1 and team2's corresponding wool color
      // To do this, we can just copy the texture from terrain.png and overlay it directly above the red/blue wool textures
      Rectangle src1 = WOOL_SOURCES.get(team1Color.toUpperCase());
      Rectangle src2 = WOOL_SOURCES.get(team2Color.toUpperCase());

      if (src1 == null || src2 == null) {
        throw new IllegalArgumentException("Invalid team color");
      }

      // Scale the wool textures if the texture pack is HD
      Rectangle scaledSrc1 = new Rectangle(
          src1.x * scaleFactor,
          src1.y * scaleFactor,
          src1.width * scaleFactor,
          src1.height * scaleFactor
      );

      Rectangle scaledSrc2 = new Rectangle(
          src2.x * scaleFactor,
          src2.y * scaleFactor,
          src2.width * scaleFactor,
          src2.height * scaleFactor
      );

      int blockSize = pxPerBlock;

      Rectangle team1Dst = new Rectangle(
          TEAM1_WOOL_DST_BLOCK.x * blockSize,
          TEAM1_WOOL_DST_BLOCK.y * blockSize,
          TEAM1_WOOL_DST_BLOCK.width * blockSize,
          TEAM1_WOOL_DST_BLOCK.height * blockSize
      );

      Rectangle team2Dst = new Rectangle(
          TEAM2_WOOL_DST_BLOCK.x * blockSize,
          TEAM2_WOOL_DST_BLOCK.y * blockSize,
          TEAM2_WOOL_DST_BLOCK.width * blockSize,
          TEAM2_WOOL_DST_BLOCK.height * blockSize
      );

      // Some newer maps use navy blue wool instead of blue wool, so let's also replace those
      Rectangle team2Dst2 = new Rectangle(
          TEAM2_ALT_WOOL_DST_BLOCK.x * blockSize,
          TEAM2_ALT_WOOL_DST_BLOCK.y * blockSize,
          TEAM2_ALT_WOOL_DST_BLOCK.width * blockSize,
          TEAM2_ALT_WOOL_DST_BLOCK.height * blockSize
      );

      copyRegion(terrain, scaledSrc1, team1Dst);
      copyRegion(terrain, scaledSrc2, team2Dst);
      copyRegion(terrain, scaledSrc2, team2Dst2);

      graphics.drawImage(terrain, 0, 0, null); // Put the modified terrain
      graphics.setComposite(AlphaComposite.Src);

      // Add the CTF blocks on top
      graphics.drawImage(
          ctfTerrain,
          0,
          (TEXTURE_HEIGHT_BLOCKS - ctfRows) * pxPerBlock,
          null
      );

      // Add custom mine/flag/tnt textures on top
      BufferedImage clanBlocks =
          ImageIO.read(new File("texturepack_patch/clan_blocks.png"));

      // Team 1 textures
      drawTeamTextures(
          graphics,
          clanBlocks,
          scaleFactor,
          team1Color,
          true
      );

      // Team 2 textures
      drawTeamTextures(
          graphics,
          clanBlocks,
          scaleFactor,
          team2Color,
          false
      );

    } finally {
      graphics.dispose();
    }

    return target;
  }

  public static void createPatchedTexturePack(String map) {
    try {
      File texturePackFile = new File("texturepacks/terrain_" + map + ".zip");
      File outputFile = new File("texturepacks_cache/terrain_" + map + ".zip");
      if (outputFile.exists()) {
        return;
      }

      System.out.println("Creating patched textures for " + map);

      ZipFile in = new ZipFile(texturePackFile);
      ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFile));

      File fontFile = new File("texturepack_patch/default.png");
      out.putNextEntry(new ZipEntry("default.png"));
      Files.copy(fontFile.toPath(), out);
      File particlesFile = new File("texturepack_patch/particles.png");
      out.putNextEntry(new ZipEntry("particles.png"));
      Files.copy(particlesFile.toPath(), out);
      Image ctfTerrain = ImageIO.read(new File("texturepack_patch/ctf_terrain.png"));

      Enumeration<? extends ZipEntry> entries = in.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        switch (name) {
          case "default.png":
            // use font file above
            break;
          case "particles.png":
            // use particles file above
            break;
          default:
            if (name.equals("terrain.png") || name.endsWith("/terrain.png")) {
              out.putNextEntry(new ZipEntry("terrain.png"));
              DataInputStream terrainData = new DataInputStream(in.getInputStream(entry));

              Image source = ImageIO.read(terrainData);
              BufferedImage imageData = mergeTerrain(ctfTerrain, source);

              ByteArrayOutputStream imgOutput = new ByteArrayOutputStream();
              ImageIO.write(imageData, "png", imgOutput);
              out.write(imgOutput.toByteArray());

              terrainData.close();
              break;
            }

            // Everything else gets copied raw
            out.putNextEntry(new ZipEntry(name));

            DataInputStream dataIn =
                new DataInputStream(in.getInputStream(entry));

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
