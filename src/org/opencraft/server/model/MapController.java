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

import org.opencraft.server.Constants;
import org.opencraft.server.Server;
import org.opencraft.server.game.impl.GameSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class MapController {
  public static ArrayList<String> levelNames = new ArrayList<String>(16);
  private static ArrayList<String> voteList;
  private static HashMap<String, Integer> levelVotes = new HashMap<String, Integer>(16);
  private static int nLevels = 0;

  public static void create() {
    levelNames.clear();
    levelVotes.clear();
    File dir = new File(Constants.ROOT_PATH + "/maps");
    String[] maps = dir.list();
    for (String mapName : maps) {
      String[] parts = mapName.split("\\.");
      if (parts.length == 2 && parts[1].equals("cw")) {
        levelNames.add(parts[0]);
        levelVotes.put(parts[0], 0);
      }
    }
    Collections.sort(levelNames);
    nLevels = levelNames.size();
    MapRatings.load();
  }

  public static Level randomLevel() {
    int r = Server.random.nextInt(nLevels);
    String name = levelNames.get(r);
    return getLevel(name);
  }

  public static Level getLevel(String id) {
    try {
      String path = "maps/" + id + ".cw";
      File file = new File(path);
      if (file.exists()) {
        return new Level().load(path, id);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public static boolean addVote(String id) {
    if (voteList.contains(id)) {
      levelVotes.put(id, levelVotes.get(id) + 1);
      return true;
    } else return false;
  }

  public static void resetVotes() {
    for (int i = 0; i < nLevels; i++) {
      levelVotes.put(levelNames.get(i), 0);
    }
  }

  public static ArrayList<String> getRandomMapNames(int n, String[] ignore) {
    if (levelNames.size() == 1) return levelNames;
    ArrayList<String> names = new ArrayList<String>();
    names.addAll(levelNames);
    for (String name : ignore) {
      if (name != null && names.contains(name)) names.remove(name);
    }
    if (GameSettings.getBoolean("OnlyTDM")) {
      Iterator<String> itr = names.iterator();
      while (itr.hasNext()) {
        if (!itr.next().startsWith("tdm_")) itr.remove();
      }
    }
    ArrayList<String> r = new ArrayList<String>(n);
    n = Math.min(names.size(), n);
    for (int i = 0; i < n; i++) {
      int idx = new Random().nextInt(names.size());
      r.add(names.remove(idx));
    }
    voteList = r;
    return r;
  }

  public static Level getMostVotedForMap() {
    int highestVotes = 0;
    String highest = null;
    for (String l : levelVotes.keySet()) {
      int votes = levelVotes.get(l);
      if (votes > highestVotes) {
        highest = l;
        highestVotes = votes;
      }
    }
    if (highest == null) return randomLevel();
    else return getLevel(highest);
  }
}
