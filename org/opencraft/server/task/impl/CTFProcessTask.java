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
package org.opencraft.server.task.impl;

import org.opencraft.server.Configuration;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

public class CTFProcessTask extends ScheduledTask {

  private static final long DELAY = 100;
  private static CTFGameMode ctf = (CTFGameMode) World.getWorld().getGameMode();
  private static World world = World.getWorld();
  private static int ticks = 0;

  public CTFProcessTask() {
    super(DELAY);
  }

  public void execute() {
    for (Player player : world.getPlayerList().getPlayers()) {
      if (World.getWorld().getPlayerList().size() >= Configuration.getConfiguration()
          .getMaximumPlayers() && System.currentTimeMillis() - player.moveTime > 5 * 60 * 1000 &&
          player.moveTime != 0) {
        World.getWorld().broadcast("- " + player.parseName() + " was kicked for being AFK");
        player.getActionSender().sendLoginFailure("You were kicked for being AFK");
        player.getSession().close();
        player.moveTime = System.currentTimeMillis();
      }
      if (!player.respawning)
        ctf.processPlayerMove(player);
      if (player.flamethrowerEnabled) {
        player.flamethrowerUnits--;
        if (player.flamethrowerUnits == 0) {
          player.getActionSender().sendChatMessage("- &eFuel: &c" + player.flamethrowerUnits);
          player.flamethrowerEnabled = false;
          World.getWorld().getLevel().clearFire(player.linePosition, player.lineRotation);
          player.flamethrowerTime = System.currentTimeMillis();
          return;
        } else if (player.flamethrowerUnits % 25 == 0) {
          player.getActionSender().sendChatMessage("- &eFuel: &c" + player.flamethrowerUnits);
        }
        if (!player.getPosition().equals(player.linePosition) || !player.getRotation().equals
            (player.lineRotation)) {
          if (player.linePosition != null)
            World.getWorld().getLevel().clearFire(player.linePosition, player.lineRotation);
          World.getWorld().getLevel().drawFire(player.getPosition(), player.getRotation());
          player.linePosition = player.getPosition();
          player.lineRotation = player.getRotation();
        }
        ((CTFGameMode) World.getWorld().getGameMode()).processFlamethrower(player, player
            .linePosition, player.lineRotation);
      }
            /* if(player.hasFlag) {
                player.headBlockType = player.team == 0 ? 28 : 21;
            }
            else */
      if (player.duelPlayer != null) {
        player.headBlockType = 41;
      } else {
        player.headBlockType = 0;
      }
      if (player.headBlockType != 0) {
        Position blockPos = player.getPosition().toBlockPos();
        Position newPosition = new Position(blockPos.getX(), blockPos.getY(), blockPos.getZ() + 3);
        if (!newPosition.equals(player.headBlockPosition)) {
          if (player.headBlockPosition != null) {
            world.getLevel().setBlock(player.headBlockPosition, 0);
          }
          if (world.getLevel().getBlock(newPosition) == 0) {
            player.headBlockPosition = newPosition;
            world.getLevel().setBlock(player.headBlockPosition, player.headBlockType);
          } else {
            player.headBlockPosition = null;
          }
        }
      } else if (player.headBlockPosition != null) {
        world.getLevel().setBlock(player.headBlockPosition, 0);
      }
    }
    if (((CTFGameMode) World.getWorld().getGameMode()).getMode() == Level.TDM) {
      long elapsedTime = System.currentTimeMillis() - ((CTFGameMode) World.getWorld().getGameMode
          ()).gameStartTime;
      if (elapsedTime > GameSettings.getInt("TDMTimeLimit") * 60 * 1000) {
        ((CTFGameMode) World.getWorld().getGameMode()).gameStartTime = System.currentTimeMillis();
        ((CTFGameMode) World.getWorld().getGameMode()).endGame();
      }
      long remaining = Math.max((GameSettings.getInt("TDMTimeLimit") * 60 * 1000 - elapsedTime) /
          1000, 0);
      if (((CTFGameMode) World.getWorld().getGameMode()).voting)
        remaining = 0;
      if (ticks == 0) {
        for (Player player : world.getPlayerList().getPlayers()) {
          if (player.getSession().isExtensionSupported("MessageTypes")) {
            ((CTFGameMode) World.getWorld().getGameMode()).sendDefaultMessage(player);
            player.getActionSender().sendChatMessage("Team Deathmatch | " + prettyTime((int)
                remaining), false, 1);
          }
        }
      }
    }
    ticks++;
    if (ticks == 10) {
      ticks = 0;
    }
  }

  public String prettyTime(int seconds) {
    int day = (int) Math.floor(seconds / (24 * 3600));
    int hs = (int) Math.floor(seconds / 3600 % 24);
    int ms = (int) Math.floor(seconds / 60 % 60);
    int sr = (int) Math.floor(seconds / 1 % 60);
    String hh, mm, ss;
    if (hs < 10) {
      hh = "0" + hs;
    } else {
      hh = hs + "";
    }
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
    // if (day != 0) { time += day + ":"; }
    // if (hs  != 0) { time += hh + ":";  } else { time += "00:"; }
    if (ms != 0) {
      time += mm + ":";
    } else {
      time += "00:";
    }
    time += ss;
    return time;
  }
}
