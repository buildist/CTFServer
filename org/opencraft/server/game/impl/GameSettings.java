package org.opencraft.server.game.impl;

import java.util.HashMap;

public class GameSettings {
  public static final int TYPE_INT = 0;
  public static final int TYPE_BOOLEAN = 1;
  private static final HashMap<String, GameSetting> settings = new HashMap<String, GameSetting>();

  static {
    add("MaxCaptures", TYPE_INT, 5);
    add("Chaos", TYPE_BOOLEAN, false);
    add("MaxMines", TYPE_INT, 2);
    add("TDMTimeLimit", TYPE_INT, 10);
    add("TournamentTimeLimit", TYPE_INT, 40);
    add("EnableStore", TYPE_BOOLEAN, true);
    add("OnlyTDM", TYPE_BOOLEAN, false);
    add("Debug", TYPE_BOOLEAN, false);
    add("Tournament", TYPE_BOOLEAN, false);
  }

  public static Object get(String k) {
    synchronized (settings) {
      return settings.get(k).value;
    }
  }

  public static int getInt(String k) {
    return (Integer) get(k);
  }

  public static boolean getBoolean(String k) {
    return (Boolean) get(k);
  }

  public static HashMap<String, GameSetting> getSettings() {
    return settings;
  }

  public static boolean set(String k, String value) {
    synchronized (settings) {
      GameSetting s = settings.get(k);
      try {
        switch (s.type) {
          case TYPE_INT:
            s.value = Integer.parseInt(value);
            break;
          case TYPE_BOOLEAN:
            s.value = Boolean.parseBoolean(value);
            break;
        }
      } catch (Exception ex) {
        return false;
      }
      return true;
    }
  }

  private static void add(String name, int type, Object value) {
    settings.put(name, new GameSetting(name, type, value));
  }

  public static void reset() {
    synchronized (settings) {
      for (GameSetting s : settings.values()) {
        s.value = s.defaultValue;
      }
    }
  }

  public static class GameSetting {
    public String name;
    public Object value;
    public Object defaultValue;
    public int type;

    public GameSetting(String n, int t, Object d) {
      name = n;
      value = defaultValue = d;
      defaultValue = d;
      type = t;
    }
  }
}