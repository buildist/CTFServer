package org.opencraft.server.model;

import org.opencraft.server.Constants;

import jdk.nashorn.internal.ir.Block;

public class CustomBlockDefinition {
  public final int id;
  public final String name;
  public final int solid;
  public final int movementSpeed;
  public final int textureTop;
  public final int textureLeft;
  public final int textureRight;
  public final int textureFront;
  public final int textureBack;
  public final int textureBottom;
  public final boolean emitsLight;
  public final int walkSound;
  public final boolean fullBright;
  public final int minX;
  public final int minY;
  public final int minZ;
  public final int maxX;
  public final int maxY;
  public final int maxZ;
  public final int blockDraw;
  public final int fogDensity;
  public final int fogR;
  public final int fogG;
  public final int fogB;

  public CustomBlockDefinition(
      int id,
      String name,
      int solid,
      int movementSpeed,
      int textureTop,
      int textureLeft,
      int textureRight,
      int textureFront,
      int textureBack,
      int textureBottom,
      boolean emitsLight,
      int walkSound,
      boolean fullBright,
      int minX,
      int minY,
      int minZ,
      int maxX,
      int maxY,
      int maxZ,
      int blockDraw,
      int fogDensity,
      int fogR,
      int fogG,
      int fogB) {
    this.id = id;
    this.name = name;
    this.solid = solid;
    this.movementSpeed = movementSpeed;
    this.textureTop = textureTop;
    this.textureLeft = textureLeft;
    this.textureRight = textureRight;
    this.textureFront = textureFront;
    this.textureBack = textureBack;
    this.textureBottom = textureBottom;
    this.emitsLight = emitsLight;
    this.walkSound = walkSound;
    this.fullBright = fullBright;
    this.minX = minX;
    this.minY = minY;
    this.minZ = minZ;
    this.maxX = maxX;
    this.maxY = maxY;
    this.maxZ = maxZ;
    this.blockDraw = blockDraw;
    this.fogDensity = fogDensity;
    this.fogR = fogR;
    this.fogG = fogG;
    this.fogB = fogB;
  }

  public CustomBlockDefinition(int id, String name, int texture, int walkSound) {
    this(
        id,
        name,
        Constants.BLOCK_SOLIDITY_SOLID,
        128,
        texture,
        texture,
        texture,
        texture,
        texture,
        texture,
        false,
        walkSound,
        false,
        0,
        0,
        0,
        16,
        16,
        16,
        Constants.BLOCK_DRAW_OPAQUE,
        0,
        0,
        0,
        0
    );
  }

  public static final CustomBlockDefinition MINE = new CustomBlockDefinition(
      Constants.BLOCK_MINE,
      "Mine",
      89,
      Constants.BLOCK_WALK_SOUND_METAL);
  public static final CustomBlockDefinition MINE_RED = new CustomBlockDefinition(
      Constants.BLOCK_MINE_RED,
      "Mine",
      87,
      Constants.BLOCK_WALK_SOUND_METAL);
  public static final CustomBlockDefinition MINE_BLUE = new CustomBlockDefinition(
      Constants.BLOCK_MINE_BLUE,
      "Mine",
      88,
      Constants.BLOCK_WALK_SOUND_METAL);

  public static final CustomBlockDefinition[] CUSTOM_BLOCKS = new CustomBlockDefinition[]{
      MINE,
      MINE_RED,
      MINE_BLUE
  };

  static {
    for (CustomBlockDefinition block : CUSTOM_BLOCKS) {
      BlockManager.getBlockManager().addCustomBlock(block);
    }
  }
}
