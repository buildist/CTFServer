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
import org.opencraft.server.cmd.impl.FuelCommand;
import org.opencraft.server.cmd.impl.GrenadeCommand;
import org.opencraft.server.cmd.impl.LineCommand;
import org.opencraft.server.cmd.impl.RocketCommand;
import org.opencraft.server.cmd.impl.SmokeGrenadeCommand;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.impl.BigTNTItem;
import org.opencraft.server.model.impl.SimpleItem;

import java.util.HashMap;

public class Store {
  private HashMap<String, StoreItem> items = new HashMap<String, StoreItem>(16);

  public static int bigTNTPrice = GameSettings.getInt("BigTNTPrice");
  public static int rocketPrice = GameSettings.getInt("RocketPrice");
  public static int grenadePrice = GameSettings.getInt("GrenadePrice");
  public static int linePrice = GameSettings.getInt("LinePrice");
  public static int creeperPrice = GameSettings.getInt("CreeperPrice");
  public static int smokeGrenadePrice = GameSettings.getInt("SmokeGrenadePrice");
  public static int flamethrowerFuelPrice = GameSettings.getInt("FlameThrowerFuelPrice");

  private static final int creeperRecharge = 7;
  private static final int grenadeRecharge = 7;
  private static final int lineRecharge = 7;
  private static final int rocketRecharge = 10;
  private static final int smokeGrenadeRecharge = 10;

  public Store() {
    addItem("BigTNT", new BigTNTItem("BigTNT", bigTNTPrice), "bigtnt");
    addItem(
        "Rocket",
        new SimpleItem("Rocket", rocketPrice, "Shoots a rocket from your face", RocketCommand.getCommand()),
        "r");
    addItem(
        "Grenade",
        new SimpleItem("Grenade", grenadePrice, "Throwable TNT", GrenadeCommand.getCommand()),
        "gr");
    addItem(
        "Line", new SimpleItem("Line", linePrice, "Builds a bridge", LineCommand.getCommand()), "line");
    addItem(
        "Creeper",
        new SimpleItem("Creeper", creeperPrice, "Makes you explode", CreeperCommand.getCommand()),
        "cr");
    addItem(
        "SmokeGrenade",
        new SimpleItem("SmokeGrenade", smokeGrenadePrice, "Fogs an area temporarily", SmokeGrenadeCommand.getCommand()),
        "sg");
    addItem(
        "FlameThrowerFuel",
        new SimpleItem("FlameThrowerFuel", flamethrowerFuelPrice, "Buys fuel to power the flamethrower", FuelCommand.getCommand()),
        "ff");
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

    if (itemname == "Creeper") {
      long creeperCooldown = (System.currentTimeMillis() - p.creeperTime);
      if (creeperCooldown < creeperRecharge * 1000) {
        p.getActionSender().sendChatMessage("- &ePlease wait " + (creeperRecharge - creeperCooldown / 1000) + "" + " seconds");
        return false;
      }
      if (p.isCreepering) {
        p.getActionSender().sendChatMessage("- &eYou need to explode before using this again!");
        return false;
      }
    }

    if (itemname == "Grenade") {
      long grenadeCooldown = (System.currentTimeMillis() - p.grenadeTime);
      if (grenadeCooldown < grenadeRecharge * 1000) {
        p.getActionSender().sendChatMessage("- &ePlease wait " + (grenadeRecharge - grenadeCooldown / 1000) + "" + " seconds");
        return false;
      }
    }

    if (itemname == "Line") {
      long lineCooldown = (System.currentTimeMillis() - p.lineTime);
      if (lineCooldown < lineRecharge * 1000) {
        p.getActionSender().sendChatMessage("- &ePlease wait " + (lineRecharge - lineCooldown / 1000) + "" + " seconds");
        return false;
      }
    }

    if (itemname == "Rocket") {
      long rocketCooldown = (System.currentTimeMillis() - p.rocketTime);
      if (rocketCooldown < rocketRecharge * 1000) {
        p.getActionSender().sendChatMessage("- &ePlease wait " + (rocketRecharge - rocketCooldown / 1000) + "" + " seconds");
        return false;
      }
    }

    if (itemname == "FlameThrowerFuel") {
      long smokeGrenadeCooldown = (System.currentTimeMillis() - p.smokeGrenadeTime);
      if (smokeGrenadeCooldown < smokeGrenadeRecharge * 1000) {
        p.getActionSender().sendChatMessage("- &ePlease wait " + (smokeGrenadeRecharge - smokeGrenadeCooldown / 1000) + "" + " seconds");
        return false;
      }
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

  public void updateItem(String name, int price) {
    StoreItem item = null;
    String command = null;

    if (name == "BigTNT") {
      item = new BigTNTItem("BigTNT", price);
      command = "bigtnt";
    } else if (name == "Rocket") {
      item = new SimpleItem("Rocket", price, "Shoots a rocket from your face", RocketCommand.getCommand());
      command = "r";
    } else if (name == "Grenade") {
      item = new SimpleItem("Grenade", price, "Throwable TNT", GrenadeCommand.getCommand());
      command = "gr";
    } else if (name == "Line") {
      item = new SimpleItem("Line", price, "Builds a bridge", LineCommand.getCommand());
      command = "line";
    } else if (name == "Creeper") {
      item = new SimpleItem("Creeper", price, "Makes you explode", CreeperCommand.getCommand());
      command = "cr";
    } else if (name == "SmokeGrenade") {
      item = new SimpleItem("SmokeGrenade", price, "Fogs an area temporarily", SmokeGrenadeCommand.getCommand());
      command = "sg";
    } else if (name == "FlameThrowerFuel") {
      item = new SimpleItem("FlameThrowerFuel", price, "Buys fuel to power the flamethrower", FuelCommand.getCommand());
      command = "ff";
    }

    items.put(name, item);
    item.command = command;
  }

  public Object[] getItems() {
    return items.values().toArray();
  }
}
