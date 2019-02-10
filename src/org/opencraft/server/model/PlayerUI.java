package org.opencraft.server.model;

import org.opencraft.server.game.impl.GameSettings;

public class PlayerUI {
  public static final String PROGRESS_LEFT = "Ç";
  public static final String PROGRESS_RIGHT = "ü";
  public static final String PROGRESS_LEFT_INACTIVE = "Æ";
  public static final String PROGRESS_RIGHT_INACTIVE = "ô";
  public static final String PROGRESS_RELOAD_1 = "ò";
  public static final String PROGRESS_RELOAD_2 = "û";
  public static final String PROGRESS_0_INACTIVE = "ö";
  public static final String PROGRESS_8 = "é";
  public static final String PROGRESS_7 = "â";
  public static final String PROGRESS_6 = "ä";
  public static final String PROGRESS_5 = "à";
  public static final String PROGRESS_4 = "å";
  public static final String PROGRESS_3 = "ç";
  public static final String PROGRESS_2 = "ê";
  public static final String PROGRESS_1 = "ë";
  public static final String PROGRESS_0 = "æ";
  private static final int PROGRESS_LENGTH = 64;
  private static final int PROGRESS_LENGTH_CHARACTERS = PROGRESS_LENGTH / 8;

  private final Player player;

  private String status0 = "";
  private String status1 = "";
  private String status2 = "";

  public PlayerUI(Player player) {
    this.player = player;
  }

  public String getProgressBar(int currentValue, int maxValue) {
    int length = (int) Math.round(((double) currentValue / maxValue) * PROGRESS_LENGTH);
    StringBuilder builder = new StringBuilder();
    if (currentValue == 0) {
      builder.append(PROGRESS_LEFT_INACTIVE);
      builder.append(PROGRESS_RELOAD_1);
      builder.append(PROGRESS_RELOAD_2);
      for (int i = 2; i < PROGRESS_LENGTH_CHARACTERS; i++) {
        builder.append(PROGRESS_0_INACTIVE);
      }
      builder.append(PROGRESS_RIGHT_INACTIVE);
          return builder.toString();
    }
    builder.append(PROGRESS_LEFT);
    for (int i = 0; i < length / PROGRESS_LENGTH_CHARACTERS; i++) {
      builder.append(PROGRESS_8);
    }
    switch (length % PROGRESS_LENGTH_CHARACTERS) {
      case 7:
        builder.append(PROGRESS_7);
        break;
      case 6:
        builder.append(PROGRESS_6);
        break;
      case 5:
        builder.append(PROGRESS_5);
        break;
      case 4:
        builder.append(PROGRESS_4);
        break;
      case 3:
        builder.append(PROGRESS_3);
        break;
      case 2:
        builder.append(PROGRESS_2);
        break;
      case 1:
        builder.append(PROGRESS_1);
        break;
      case 0:
        if (currentValue < maxValue) builder.append(PROGRESS_0);
        break;
    }
    for (int i = length / PROGRESS_LENGTH_CHARACTERS + 1; i < PROGRESS_LENGTH_CHARACTERS; i++) {
      builder.append(PROGRESS_0);
    }
    builder.append(PROGRESS_RIGHT);
    return builder.toString();
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
  }

  public void step() {
    String time = getElapsedTime();
    if (!status0.equals(time)) {
      status0 = time;
      player.getActionSender().sendChatMessage(status0, 1);
    }

    String ammoBar = getProgressBar(player.ammo, GameSettings.getInt("Ammo"));
    if (!status1.equals(ammoBar)) {
      status1 = ammoBar;
      player.getActionSender().sendChatMessage(status1, 2);
    }
  }
}
