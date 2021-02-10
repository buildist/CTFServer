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

import org.opencraft.server.cmd.impl.ActivateItemCommand;
import org.opencraft.server.cmd.impl.CreeperCommand;
import org.opencraft.server.cmd.impl.GrenadeCommand;
import org.opencraft.server.cmd.impl.LineCommand;
import org.opencraft.server.cmd.impl.RocketCommand;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.impl.BigTNTItem;
import org.opencraft.server.model.impl.SimpleItem;

import java.util.HashMap;

public class Store {
  private HashMap<String, StoreItem> items = new HashMap<String, StoreItem>(16);

  public Store() {
    addItem("BigTNT", new BigTNTItem("BigTNT", 70), "bigtnt");
    addItem(
        "Rocket",
        new SimpleItem("Rocket", 60, "Shoots a rocket from your face", RocketCommand.getCommand()),
        "r");
    addItem(
        "Grenade",
        new SimpleItem("Grenade", 20, "Throwable TNT", GrenadeCommand.getCommand()),
        "gr");
    addItem(
        "Line", new SimpleItem("Line", 25, "Builds a bridge", LineCommand.getCommand()), "line");
    addItem(
        "Creeper",
        new SimpleItem("Creeper", 40, "Makes you explode", CreeperCommand.getCommand()),
        "cr");
  }

  public boolean buy(Player p, String itemname) {
    StoreItem item = null;
    item = items.get(itemname);
    if (item == null) {
      p.getActionSender().sendChatMessage("- &eStore item does not exist.");
      return false;
    }
    if (!GameSettings.getBoolean("Chaos") && item.price > p.getPoints()) {
      p.getActionSender().sendChatMessage("- &eYou don't have enough points!");
      return false;
    }
    if (!GameSettings.getBoolean("Chaos")) {
      p.subtractPoints(item.price);
      p.getActionSender().sendChatMessage("- &eYou have " + p.getPoints() + " points left");
    }
    return true;
  }

  public void addItem(String name, StoreItem item, String command) {
    items.put(name, item);
    item.command = command;
    World.getWorld().getGameMode().registerCommand(command, new ActivateItemCommand(item));
  }

  public Object[] getItems() {
    return items.values().toArray();
  }
}
