package org.opencraft.server.model;

import org.opencraft.server.game.impl.GameSettings;

public class PlayerUI {
  private final Player player;

  public PlayerUI(Player player) {
    this.player = player;
  }

  private String getElapsedTime() {
    int totalTime;
    if (GameSettings.getBoolean("Tournament")) {
      totalTime = GameSettings.getInt("TournamentTimeLimit");
    } else {
      totalTime = GameSettings.getInt("TimeLimit");
    }
    long elapsedTime = System.currentTimeMillis() - World.getWorld().getGameMode().gameStartTime;
    long remaining =
        Math.max((totalTime * 60 * 1000 - elapsedTime) / 1000, 0);
    if (World.getWorld().getGameMode().voting) {
      remaining = 0;
    } else if (!World.getWorld().getGameMode().tournamentGameStarted) {
      remaining = totalTime * 60;
    }
    return prettyTime((int) remaining);
  }

  private static String prettyTime(int seconds) {
    int ms = (int) Math.floor(seconds / 60 % 60);
    int sr = (int) Math.floor(seconds / 1 % 60);
    String mm, ss;
    if (ms < 10) {
      mm = "0" + ms;
    } else {
      mm = ms + "";
    }
    if (sr < 10) {
      ss = "0" + sr;
    } else {
      ss = sr + "";
    }
    String time = "";
    if (ms != 0) {
      time += mm + ":";
    } else {
      time += "00:";
    }
    time += ss;
    return time;
  }}
