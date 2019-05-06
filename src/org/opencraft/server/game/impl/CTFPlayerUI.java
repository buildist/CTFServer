package org.opencraft.server.game.impl;

import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.PlayerUI;
import org.opencraft.server.model.World;

public class CTFPlayerUI extends PlayerUI {
  private final CTFGameMode ctf;

  public CTFPlayerUI(CTFGameMode ctf, Player player) {
    super(player);
    this.ctf = ctf;
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
    String timerSetting = null;
    String timerMessage = null;
    if (ctf.getMode() == Level.TDM) {
      timerSetting = "TDMTimeLimit";
      timerMessage = "Team Deathmatch";
    } else if (GameSettings.getBoolean("Tournament")) {
      timerSetting = "TournamentTimeLimit";
      timerMessage = "Team Tournament";
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

  @Override
  protected String getStatus2() {
    return null;
  }
}
