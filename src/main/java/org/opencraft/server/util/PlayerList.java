/*
 * Jacob_'s Capture the Flag for Minecraft Classic and ClassiCube
 * Copyright (c) 2010-2014 Jacob Morgan
 * Based on OpenCraft v0.2
 *
 * OpenCraft License
 *
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Distribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Distributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Distributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.opencraft.server.util;

import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Player;
import org.opencraft.server.net.FakePlayerBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class which manages the list of connected players.
 *
 * @author Graham Edgecombe
 */
public class PlayerList {
  /** The player array. */
  private final Player[] players = new Player[128];

  /** The size of the player array. */
  private int size = 0;

  /**
   * Gets a list of online players.
   *
   * @return A list of online players.
   */
  public List<Player> getPlayers() {
    return getPlayers(false);
  }

  public synchronized List<Player> getPlayers(boolean includeCameraMan) {
    List<Player> playerList = new ArrayList<>();
    for (Player p : players) {
      if (p != null) {
        playerList.add(p);
      }
    }
    if (includeCameraMan) playerList.add(FakePlayerBase.CAMERA_MAN);

    return Collections.unmodifiableList(playerList);
  }

  /**
   * Adds a player.
   *
   * @param player The new player.
   * @return <code>true</code> if they could be added, <code>false</code> if not.
   */
  public synchronized boolean add(Player player) {
    if (size == GameSettings.getMaxPlayers()) {
      return false;
    }
    for (int i = 0; i < players.length; i++) {
      if (i == players.length - 1) return false;
      if (players[i] == null) {
        players[i] = player;
        player.setId(i);
        size++;
        return true;
      }
    }
    return false;
  }

  /**
   * Removes a player.
   *
   * @param player The player to remove.
   */
  public synchronized void remove(Player player) {
    int id = player.getId();
    if (id != -1 && players[id] == player) {
      players[id] = null;
      size--;
    }
    player.setId(-1);
  }

  public synchronized boolean contains(Player player) {
    for (Player p : players) {
      if (p != null && p == player) return true;
    }

    return false;
  }

  /**
   * Gets the number of online players.
   *
   * @return The player list size.
   */
  public synchronized int size() {
    return size;
  }

  @Override
  public synchronized String toString() {
    String m = "";
    for (Player p : getPlayers()) {
      m += p + ", ";
    }
    return m;
  }
}
