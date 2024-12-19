package org.opencraft.server.model;

public class BlockChange {
  public final int x;
  public final int y;
  public final int z;

  public final int type;

  public BlockChange(int x, int y, int z, int type) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.type = type;
  }
}
