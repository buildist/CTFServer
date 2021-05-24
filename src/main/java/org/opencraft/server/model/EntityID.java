package org.opencraft.server.model;

import java.util.ArrayList;
import java.util.List;

public class EntityID {
  private static final List<Integer> freeIDs = new ArrayList<>(128);

  public static void init(int maxPlayers) {
    for (int i = maxPlayers; i < 127; i++) {
      freeIDs.add(i);
    }
  }

  public static int get() {
    synchronized (freeIDs) {
      if (freeIDs.isEmpty()) return -1;
      return freeIDs.remove(0);
    }
  }

  public static void release(int id) {
    synchronized (freeIDs) {
      freeIDs.add(id);
    }
  }
}
