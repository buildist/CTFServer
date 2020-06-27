package org.opencraft.server.game.impl;

import org.opencraft.server.Constants;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUI;
import org.opencraft.server.model.ProgressBar;
import org.opencraft.server.model.ProgressBarType;
import org.opencraft.server.model.World;

public class CTFPlayerUI extends PlayerUI {
  private final CTFGameMode ctf;

  private final ProgressBar flamethrower = new ProgressBar();

  public CTFPlayerUI(CTFGameMode ctf, Player player) {
    super(player);
    this.ctf = ctf;
  }

  protected void update() {
    flamethrowerBar.update();
  }

  @Override
  protected String getStatus0() {
    String redFlag = ctf.redFlagTaken ? " &6[!]" : "";
    String blueFlag = ctf.blueFlagTaken ? " &6[!]" : "";
    return "Map: "
        + ctf.map.id
        + " | &cRed: "
        + ctf.redCaptures
        + redFlag
        + " &f| &9Blue: "
        + ctf.blueCaptures
        + blueFlag;
  }

  @Override
  protected String getStatus1() {
    if (hasTimer()) {
      return getTimerMessage();
    } else {
      return getFlamethrowerMessage();
    }
  }

  @Override
  protected String getStatus2() {
    if(hasTimer()) {
      return getFlamethrowerMessage();
    } else {
      return null;
    }
  }

  private boolean hasTimer() {
    return ctf.getMode() == Level.TDM || GameSettings.getInt("TimeLimit") > 0;
  }

  private String getTimerMessage() {
    String timerSetting = null;
    String timerMessage = null;
    if (ctf.getMode() == Level.TDM) {
      timerSetting = "TDMTimeLimit";
      timerMessage = "Team Deathmatch";
    } else {
      timerSetting = "TimeLimit";
      timerMessage = "Capture the Flag";
    }
    if (timerSetting == null) return null;

    long elapsedTime = System.currentTimeMillis() - World.getWorld().getGameMode().gameStartTime;
    long remaining =
        Math.max((GameSettings.getInt(timerSetting) * 60 * 1000 - elapsedTime) / 1000, 0);
    if (World.getWorld().getGameMode().voting) {
      remaining = 0;
    } else if (!World.getWorld().getGameMode().tournamentGameStarted) {
      remaining = GameSettings.getInt(timerSetting) * 60;
    }
    return timerMessage + " | " + prettyTime((int) remaining);
  }

  private String getFlamethrowerMessage() {
    return FIRE + " "
        + getProgressBar(flamethrowerBar.get(), Constants.FLAME_THROWER_FUEL, ProgressBarType.FIRE);
  }
}
  