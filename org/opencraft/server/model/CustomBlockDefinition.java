package org.opencraft.server.model;

import org.opencraft.server.Constants;

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
  public final int inventoryOrder;

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
      int fogB,
      int inventoryOrder) {
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
    this.inventoryOrder = inventoryOrder;
  }

  public CustomBlockDefinition(int id, String name, int texture, int walkSound, int inventoryOrder) {
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
        0,
        inventoryOrder
    );
  }

  public CustomBlockDefinition(
      int id,
      String name,
      int topTexture,
      int sideTexture,
      int bottomTexture,
      int walkSound,
      int inventoryOrder) {

    this(
        id,
        name,
        Constants.BLOCK_SOLIDITY_SOLID,
        128,
        topTexture,
        sideTexture,
        sideTexture,
        sideTexture,
        sideTexture,
        bottomTexture,
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
        0,
        inventoryOrder
    );
  }

  public CustomBlockDefinition(
      int id,
      String name,
      int texture,
      int minX,
      int minY,
      int minZ,
      int maxX,
      int maxY,
      int maxZ,
      int walkSound,
      int inventoryOrder) {

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
        minX,
        minY,
        minZ,
        maxX,
        maxY,
        maxZ,
        Constants.BLOCK_DRAW_OPAQUE,
        0,
        0,
        0,
        0,
        inventoryOrder
    );
  }

  public static final CustomBlockDefinition PURPLE = new CustomBlockDefinition(
      Constants.BLOCK_DETONATOR,
      "Detonator",
      245,
      Constants.BLOCK_WALK_SOUND_METAL,
      1);
  public static final CustomBlockDefinition MINE = new CustomBlockDefinition(
      Constants.BLOCK_MINE,
      "Mine",
      242,
      Constants.BLOCK_WALK_SOUND_METAL,
      2);
  public static final CustomBlockDefinition MINE_RED = new CustomBlockDefinition(
      Constants.BLOCK_MINE_RED,
      "Mine",
      240,
      Constants.BLOCK_WALK_SOUND_METAL,
      -1);
  public static final CustomBlockDefinition MINE_BLUE = new CustomBlockDefinition(
      Constants.BLOCK_MINE_BLUE,
      "Mine",
      241,
      Constants.BLOCK_WALK_SOUND_METAL,
      -1);
  public static final CustomBlockDefinition FLAG_RED = new CustomBlockDefinition(
      Constants.BLOCK_RED_FLAG,
      "Red Flag",
      243,
      2,
      2,
      2,
      14,
      14,
      14,
      Constants.BLOCK_WALK_SOUND_WOOL,
      -1);
  public static final CustomBlockDefinition FLAG_BLUE = new CustomBlockDefinition(
      Constants.BLOCK_BLUE_FLAG,
      "Blue Flag",
      244,
      2,
      2,
      2,
      14,
      14,
      14,
      Constants.BLOCK_WALK_SOUND_WOOL,
      -1);

  public static final CustomBlockDefinition[] CUSTOM_BLOCKS = new CustomBlockDefinition[]{
      PURPLE,
      MINE,
      MINE_RED,
      MINE_BLUE,
      FLAG_RED,
      FLAG_BLUE
  };
}
