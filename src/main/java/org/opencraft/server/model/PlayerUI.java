package org.opencraft.server.model;

import java.util.HashMap;

public abstract class PlayerUI {
  public static final String PROGRESS_LEFT = "Ç";
  public static final String PROGRESS_RIGHT = "ü";
  public static final String PROGRESS_LEFT_INACTIVE = "Æ";
  public static final String PROGRESS_RIGHT_INACTIVE = "ô";
  public static final String PROGRESS_RELOAD_1 = "ò";
  public static final String PROGRESS_RELOAD_2 = "û";
  public static final String PROGRESS_RESPAWN_1 = "ÿ";
  public static final String PROGRESS_RESPAWN_2 = "Ö";
  public static final String PROGRESS_RESPAWN_3 = "Ü";
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
  public static final String PROGRESS_B_8 = "ó";
  public static final String PROGRESS_B_7 = "ú";
  public static final String PROGRESS_B_6 = "ñ";
  public static final String PROGRESS_B_5 = "Ñ";
  public static final String PROGRESS_B_4 = "ª";
  public static final String PROGRESS_B_3 = "º";
  public static final String PROGRESS_B_2 = "¿";
  public static final String PROGRESS_B_1 = "⌐";
  public static final String PROGRESS_F_8 = "▓";
  public static final String PROGRESS_F_7 = "│";
  public static final String PROGRESS_F_6 = "┤";
  public static final String PROGRESS_F_5 = "╡";
  public static final String PROGRESS_F_4 = "╢";
  public static final String PROGRESS_F_3 = "╖";
  public static final String PROGRESS_F_2 = "╕";
  public static final String PROGRESS_F_1 = "╣";
  public static final String HIT_ICON = "É";
  public static final String KILL_ICON = "ù";
  private static final String HEALTH = "á";
  private static final String AMMO = "í";
  protected static final String FIRE = "░";
  private static final int PROGRESS_LENGTH = 64;
  private static final int PROGRESS_LENGTH_CHARACTERS = PROGRESS_LENGTH / 8;
  private final HashMap<Player, String> playerListName = new HashMap<>();

  protected final Player player;

  protected ProgressBar flamethrowerBar = new ProgressBar();

  private String status0 = "";
  private String status1 = "";
  private String status2 = "";

  public PlayerUI(Player player) {
    this.player = player;
    for (Player p : World.getWorld().getPlayerList().getPlayers()) {
      playerListName.put(p, p.getListName());
    }
  }

  public void setFlamethrower(int value) {
    flamethrowerBar.set(value);
  }

  protected String getProgressBar(float currentValue, int maxValue, ProgressBarType style) {
    int length = Math.round(currentValue / maxValue * PROGRESS_LENGTH);
    StringBuilder builder = new StringBuilder();
    if (currentValue == 0) {
      builder.append(PROGRESS_LEFT_INACTIVE);
      switch (style) {
        case AMMO:
          builder.append(PROGRESS_RELOAD_1);
          builder.append(PROGRESS_RELOAD_2);
          for (int i = 2; i < PROGRESS_LENGTH_CHARACTERS; i++) {
            builder.append(PROGRESS_0_INACTIVE);
          }
          break;
        case HEALTH:
          builder.append(PROGRESS_RESPAWN_1);
          builder.append(PROGRESS_RESPAWN_2);
          builder.append(PROGRESS_RESPAWN_3);
          for (int i = 3; i < PROGRESS_LENGTH_CHARACTERS; i++) {
            builder.append(PROGRESS_0_INACTIVE);
          }
          break;
        case FIRE:
          builder.append(PROGRESS_RELOAD_1);
          builder.append(PROGRESS_RELOAD_2);
          for (int i = 2; i < PROGRESS_LENGTH_CHARACTERS; i++) {
            builder.append(PROGRESS_0_INACTIVE);
          }
          break;
      }
      builder.append(PROGRESS_RIGHT_INACTIVE);
          return builder.toString();
    }

    String p8, p7, p6, p5, p4, p3, p2, p1;
    switch (style) {
      case AMMO:
        p8 = PROGRESS_8;
        p7 = PROGRESS_7;
        p6 = PROGRESS_6;
        p5 = PROGRESS_5;
        p4 = PROGRESS_4;
        p3 = PROGRESS_3;
        p2 = PROGRESS_2;
        p1 = PROGRESS_1;
        break;
      case HEALTH:
        p8 = PROGRESS_B_8;
        p7 = PROGRESS_B_7;
        p6 = PROGRESS_B_6;
        p5 = PROGRESS_B_5;
        p4 = PROGRESS_B_4;
        p3 = PROGRESS_B_3;
        p2 = PROGRESS_B_2;
        p1 = PROGRESS_B_1;
        break;
      case FIRE:
        p8 = PROGRESS_F_8;
        p7 = PROGRESS_F_7;
        p6 = PROGRESS_F_6;
        p5 = PROGRESS_F_5;
        p4 = PROGRESS_F_4;
        p3 = PROGRESS_F_3;
        p2 = PROGRESS_F_2;
        p1 = PROGRESS_F_1;
        break;
        default:
          throw new RuntimeException();
    }

    builder.append(PROGRESS_LEFT);
    for (int i = 0; i < length / PROGRESS_LENGTH_CHARACTERS; i++) {
      builder.append(p8);
    }
    switch (length % PROGRESS_LENGTH_CHARACTERS) {
      case 7:
        builder.append(p7);
        break;
      case 6:
        builder.append(p6);
        break;
      case 5:
        builder.append(p5);
        break;
      case 4:
        builder.append(p4);
        break;
      case 3:
        builder.append(p3);
        break;
      case 2:
        builder.append(p2);
        break;
      case 1:
        builder.append(p1);
        break;
      case 0:
        if (length < PROGRESS_LENGTH) builder.append(PROGRESS_0);
        break;
    }
    for (int i = length / PROGRESS_LENGTH_CHARACTERS + 1; i < PROGRESS_LENGTH_CHARACTERS; i++) {
      builder.append(PROGRESS_0);
    }
    builder.append(PROGRESS_RIGHT);
    return builder.toString();
  }

  protected static String prettyTime(int seconds) {
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

  public void step(int ticks) {
    update();
    String newStatus0 = getStatus0();
    if (newStatus0 != null && !status0.equals(newStatus0)) {
      status0 = newStatus0;
      player.getActionSender().sendChatMessage(status0, 1);
    }
    String newStatus1 = getStatus1();
    if (newStatus1 != null && !status1.equals(newStatus1)) {
      status1 = newStatus1;
      player.getActionSender().sendChatMessage(status1, 2);
    }
    String newStatus2 = getStatus2();
    if (newStatus2 != null && !status2.equals(newStatus2)) {
      status2 = newStatus2;
      player.getActionSender().sendChatMessage(status2, 3);
    }

    if (ticks % 5 == 0) {
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        String listName = p.getListName();
        if (!listName.equals(playerListName.get(p))){
          playerListName.put(p, listName);
          player.getActionSender().sendAddPlayerName(
              p.nameId, p.getName(), p.getListName(), p.getTeamName(), (byte) 1);
        }
      }
    }
  }

  protected void update() {}
  protected abstract String getStatus0();
  protected abstract String getStatus1();
  protected abstract String getStatus2();
}