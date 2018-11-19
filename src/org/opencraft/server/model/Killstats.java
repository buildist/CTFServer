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
package org.opencraft.server.model;

import java.util.ArrayList;
import java.util.Iterator;

public class Killstats {
  public static ArrayList<PlayerKillRecord> killRecords = new ArrayList<PlayerKillRecord>(32);

  public static PlayerKillRecord getKillRecord(Player p1, Player p2) {
    PlayerKillRecord test = new PlayerKillRecord();
    test.p1 = p1;
    test.p2 = p2;
    int idx = killRecords.indexOf(test);
    if (idx == -1) {
      killRecords.add(test);
      return test;
    } else {
      return killRecords.get(idx);
    }
  }

  public static void kill(Player attacker, Player defender) {
    PlayerKillRecord kr = getKillRecord(attacker, defender);
    if (attacker == kr.p1) kr.balance++;
    else kr.balance--;
    kr.balance = Math.min(Math.max(kr.balance, -5), 5);
    kr.checkBalance();
  }

  public static void removePlayer(Player player) {
    Iterator<PlayerKillRecord> itr = killRecords.iterator();
    while (itr.hasNext()) {
      PlayerKillRecord kr = itr.next();
      if (kr.p1 == player || kr.p2 == player) itr.remove();
    }
  }

  static class PlayerKillRecord {
    public Player p1;
    public Player p2;
    public int balance;
    private Player dominator = null;

    public boolean equals(Object o) {
      if (!(o instanceof PlayerKillRecord)) return false;
      PlayerKillRecord other = (PlayerKillRecord) o;
      return (other.p1 == p1 && other.p2 == p2) || (other.p2 == p1 && other.p1 == p2);
    }

    public void checkBalance() {
      if (balance == 5 && dominator == null) {
        dominator = p1;
        World.getWorld()
            .broadcast("- " + p1.getColoredName() + "&b is DOMINATING " + p2.getColoredName());
        dominator.incStat("domination");
      } else if (balance == -5 && dominator == null) {
        dominator = p2;
        World.getWorld()
            .broadcast("- " + p2.getColoredName() + "&b is DOMINATING " + p1.getColoredName());
        dominator.incStat("domination");
      } else if (balance < 5 && dominator == p1) {
        dominator = null;
        World.getWorld()
            .broadcast("- " + p2.getColoredName() + "&b got REVENGE on " + p1.getColoredName());
        p2.incStat("revenge");
        balance = 0;
      } else if (balance > -5 && dominator == p2) {
        dominator = null;
        World.getWorld()
            .broadcast("- " + p1.getColoredName() + "&b got REVENGE on " + p2.getColoredName());
        p1.incStat("revenge");
        balance = 0;
      }
    }
  }
}
