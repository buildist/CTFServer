package org.opencraft.server.game.impl;

import java.util.HashMap;
import org.opencraft.server.Constants;

public class GameSettings {
  private static final int TYPE_INT = 0;
  private static final int TYPE_BOOLEAN = 1;
  private static final int TYPE_STRING = 2;
  private static final int TYPE_FLOAT = 3;

  private static final HashMap<String, GameSetting> settings = new HashMap<String, GameSetting>();

  static {
    add("MaxCaptures", TYPE_INT, 5);
    add("Chaos", TYPE_BOOLEAN, false);
    add("MaxMines", TYPE_INT, 2);
    add("LTTimeLimit", TYPE_INT, 10);
    add("TDMTimeLimit", TYPE_INT, 10);
    add("TimeLimit", TYPE_INT, 20);
    add("EnableStore", TYPE_BOOLEAN, true);
    add("OnlyTDM", TYPE_BOOLEAN, false);
    add("Debug", TYPE_BOOLEAN, false);
    add("Tournament", TYPE_BOOLEAN, false);
    add("FlameThrowerStartDistanceFromPlayer", TYPE_INT, 3);
    add("FlameThrowerLength", TYPE_INT, 2);
    add("FlameThrowerRechargeTime", TYPE_INT, 30);
    add("FlameThrowerDuration", TYPE_INT, 8);
    add("Ammo", TYPE_INT, 20); // number of shots before reloading
    add("Health", TYPE_INT, 24); // number of hits taken before having to resupply
    add("ReloadStep", TYPE_INT, 3);
    add("AntiStalemate", TYPE_BOOLEAN, false);
    add("InitialPoints", TYPE_INT, Constants.INITIAL_PLAYER_POINTS);
    add("Whitelist", TYPE_BOOLEAN, false);
    add("WhitelistMessage", TYPE_STRING, "There is a tournament going on, try again later!");
    add("FirstBloodPoints", TYPE_INT, 30);
    add("MineRadius", TYPE_FLOAT, 2);
    add("MinePoints", TYPE_INT, 2);
    add("BigTNTPrice", TYPE_INT, 70);
    add("BigTNTAmount", TYPE_INT, 7);
    add("BigTNTRadius", TYPE_INT, 3);
    add("CreeperPrice", TYPE_INT, 40);
    add("GrenadePrice", TYPE_INT, 35);
    add("LinePrice", TYPE_INT, 25);
    add("RocketPrice", TYPE_INT, 60);
    add("RocketSpeed", TYPE_INT, 25);
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

  public static String getString(String k) {
    return (String) get(k);
  }

  public static Float getFloat(String k) { return (Float) get(k); }

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
          case TYPE_STRING:
            s.value = value;
            break;
          case TYPE_FLOAT:
            s.value = Float.parseFloat(value);
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
