package org.opencraft.server.game.impl;

import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUI;
import org.opencraft.server.model.ProgressBar;

public class LTPlayerUI extends PlayerUI {
  private ProgressBar healthBar = new ProgressBar();
  private ProgressBar ammoBar = new ProgressBar();

  public LTPlayerUI(Player player) {
    super(player);
  }

  public void setHealth(int value) {
    healthBar.set(value);
  }

  public void setAmmo(int value) {
    ammoBar.set(value);
  }

  @Override
  protected void update() {
    healthBar.update();
    ammoBar.update();
  }

  @Override
  protected String getStatus0() {
    return null;
  }

  @Override
  protected String getStatus1() {
    return null;
  }

  @Override
  protected String getStatus2() {
    return null;
  }
}
