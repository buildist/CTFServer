package org.opencraft.server.model;

import org.opencraft.server.Constants;

public class CustomBlockDefinition {
  public final int id;
  public final String name;
  public final int solid;
  public final int movementSpeed;
  public final int textureTop;
  public final int textureSide;
  public final int textureBottom;
  public final boolean emitsLight;
  public final int walkSound;
  public final boolean fullBright;
  public final int shape;
  public final int blockDraw;

  public CustomBlockDefinition(
      int id,
       String name,
       int solid,
       int movementSpeed,
       int textureTop,
       int textureSide,
       int textureBottom,
       boolean emitsLight,
       int walkSound,
       boolean fullBright,
       int shape,
       int blockDraw) {
    this.id = id;
    this.name = name;
    this.solid = solid;
    this.movementSpeed = movementSpeed;
    this.textureTop = textureTop;
    this.textureSide = textureSide;
    this.textureBottom = textureBottom;
    this.emitsLight = emitsLight;
    this.walkSound = walkSound;
    this.fullBright = fullBright;
    this.shape = shape;
    this.blockDraw = blockDraw;
  }

  public static final CustomBlockDefinition MINE = new CustomBlockDefinition(
      254,
      "Mine",
      Constants.BLOCK_SOLIDITY_SOLID,
      128,
      0,
      0,
      0,
      false,
      Constants.BLOCK_WALK_SOUND_METAL,
      false,
      16,
      Constants.BLOCK_DRAW_OPAQUE
  );
}
