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

  public CustomBlockDefinition(
      int id, String name, int texture, int walkSound, int inventoryOrder) {
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
        inventoryOrder);
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
        inventoryOrder);
  }

  public static final CustomBlockDefinition TNT =
      new CustomBlockDefinition(
          Constants.BLOCK_TNT,
          "TNT",
          Constants.BLOCK_SOLIDITY_SOLID,
          128,
          503,
          502,
          502,
          502,
          502,
          504,
          false,
          Constants.BLOCK_WALK_SOUND_METAL,
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
          1);
  public static final CustomBlockDefinition PURPLE =
      new CustomBlockDefinition(
          Constants.BLOCK_DETONATOR, "Detonator", 501, Constants.BLOCK_WALK_SOUND_METAL, 2);
  public static final CustomBlockDefinition MINE =
      new CustomBlockDefinition(
          Constants.BLOCK_MINE, "Mine", 498, Constants.BLOCK_WALK_SOUND_METAL, 3);
  public static final CustomBlockDefinition MINE_RED =
      new CustomBlockDefinition(
          Constants.BLOCK_MINE_RED, "Mine (Red)", 496, Constants.BLOCK_WALK_SOUND_METAL, 0);
  public static final CustomBlockDefinition MINE_BLUE =
      new CustomBlockDefinition(
          Constants.BLOCK_MINE_BLUE, "Mine (Blue)", 497, Constants.BLOCK_WALK_SOUND_METAL, 0);
  public static final CustomBlockDefinition FLAG_RED =
      new CustomBlockDefinition(
          Constants.BLOCK_RED_FLAG,
          "Red Flag",
          499,
          2,
          2,
          2,
          14,
          14,
          14,
          Constants.BLOCK_WALK_SOUND_WOOL,
          0);
  public static final CustomBlockDefinition FLAG_BLUE =
      new CustomBlockDefinition(
          Constants.BLOCK_BLUE_FLAG,
          "Blue Flag",
          500,
          2,
          2,
          2,
          14,
          14,
          14,
          Constants.BLOCK_WALK_SOUND_WOOL,
          0);
  public static final CustomBlockDefinition PAYLOAD =
      new CustomBlockDefinition(
          Constants.BLOCK_PAYLOAD, "Payload", 249, Constants.BLOCK_WALK_SOUND_METAL, 0);
  public static final CustomBlockDefinition CRATE =
      new CustomBlockDefinition(
          Constants.BLOCK_CRATE, "Crate", 505, Constants.BLOCK_WALK_SOUND_WOOD, 0);
  public static final CustomBlockDefinition INVISIBLE =
      new CustomBlockDefinition(
          255, "Invisible", 255, Constants.BLOCK_WALK_SOUND_METAL, 0);
  public static final CustomBlockDefinition BLOCK_VINE = new CustomBlockDefinition(
      Constants.BLOCK_VINE,
      "Vine",
      Constants.BLOCK_SOLIDITY_SWIM_THROUGH,
      128,
      506,
      506,
      506,
      506,
      506,
      506,
      false,
      Constants.BLOCK_WALK_SOUND_METAL,
      false,
      0,
      0,
      0,
      0,
      0,
      0,
      Constants.BLOCK_DRAW_SPRITE,
      0,
      0,
      0,
      0,
      1);
  public static final CustomBlockDefinition HIT_RED = new CustomBlockDefinition(
      Constants.HIT_RED,
      "Hit Red",
      Constants.BLOCK_SOLIDITY_WALK_THROUGH,
      128,
      496,
      496,
      496,
      496,
      496,
      496,
      true,
      Constants.BLOCK_WALK_SOUND_METAL,
      true,
      0,
      0,
      0,
      0,
      0,
      0,
      Constants.BLOCK_DRAW_SPRITE,
      0,
      0,
      0,
      0,
      1);
  public static final CustomBlockDefinition HIT_BLUE = new CustomBlockDefinition(
      Constants.HIT_BLUE,
      "Hit Blue",
      Constants.BLOCK_SOLIDITY_WALK_THROUGH,
      128,
      497,
      497,
      497,
      497,
      497,
      497,
      true,
      Constants.BLOCK_WALK_SOUND_METAL,
      true,
      0,
      0,
      0,
      0,
      0,
      0,
      Constants.BLOCK_DRAW_SPRITE,
      0,
      0,
      0,
      0,
      1);
  public static final CustomBlockDefinition LASER_RED = new CustomBlockDefinition(
      Constants.LASER_RED,
      "Laser Red",
      Constants.BLOCK_SOLIDITY_WALK_THROUGH,
      128,
      498,
      498,
      498,
      498,
      498,
      498,
      true,
      Constants.BLOCK_WALK_SOUND_METAL,
      true,
      7,
      7,
      7,
      10,
      10,
      10,
      Constants.BLOCK_DRAW_OPAQUE,
      0,
      0,
      0,
      0,
      1);
  public static final CustomBlockDefinition LASER_BLUE = new CustomBlockDefinition(
      Constants.LASER_BLUE,
      "Laser Blue",
      Constants.BLOCK_SOLIDITY_WALK_THROUGH,
      128,
      499,
      499,
      499,
      499,
      499,
      499,
      true,
      Constants.BLOCK_WALK_SOUND_METAL,
      true,
      7,
      7,
      7,
      10,
      10,
      10,
      Constants.BLOCK_DRAW_OPAQUE,
      0,
      0,
      0,
      0,
      1);
  public static final CustomBlockDefinition RESUPPLY =
      new CustomBlockDefinition(
          Constants.BLOCK_RESUPPLY, "Resupply", 500, Constants.BLOCK_WALK_SOUND_METAL, 0);


  public static final CustomBlockDefinition[] CUSTOM_BLOCKS =
      new CustomBlockDefinition[]{TNT, PURPLE, MINE, MINE_RED, MINE_BLUE, FLAG_RED, FLAG_BLUE,
          CRATE, BLOCK_VINE, INVISIBLE};
}