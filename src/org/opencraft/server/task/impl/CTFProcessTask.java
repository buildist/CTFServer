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
import org.opencraft.server.Constants;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

public class CTFProcessTask extends ScheduledTask {

  private static final long DELAY = 100;
  private static CTFGameMode ctf = World.getWorld().getGameMode();
  private static World world = World.getWorld();
  private static int ticks = 0;
  private static int payloadStep = 0;
  private static String payloadStatus = "";

  public CTFProcessTask() {
    super(DELAY);
  }

  public void execute() {
    Position payloadPosition = null;
    int payloadAttackers = 0;
    int payloadDefenders = 0;
    if (ctf.getMode() == Level.PAYLOAD && ctf.payloadPosition != -1) {
      payloadPosition = world.getLevel().getPayloadPath().get(ctf.payloadPosition);
    }

    for (Player player : world.getPlayerList().getPlayers()) {
      if (World.getWorld().getPlayerList().size()
              >= Configuration.getConfiguration().getMaximumPlayers()
          && System.currentTimeMillis() - player.moveTime > 5 * 60 * 1000
          && player.moveTime != 0) {
        World.getWorld().broadcast("- " + player.parseName() + " was kicked for being AFK");
        player.getActionSender().sendLoginFailure("You were kicked for being AFK");
        player.getSession().close();
        player.moveTime = System.currentTimeMillis();
      }
      ctf.processPlayerMove(player);
      if (player.isFlamethrowerEnabled()) {
        int duration = GameSettings.getInt("FlameThrowerDuration");
        // ticks a second
        float rate = (float) Constants.FLAME_THROWER_FUEL / duration;
        long time = System.currentTimeMillis();
        long dt = time - player.flamethrowerTime;
        // Rate in seconds, dt in milliseconds
        player.flamethrowerFuel -= rate * dt / 1000;
        player.flamethrowerTime = time;
        if (player.flamethrowerFuel <= 0) { // Out of fuel
          player.disableFlameThrower();
          player.flamethrowerFuel = 0;
        }
        // Was flame thrower disabled because they ran out of fuel?
        if (player.isFlamethrowerEnabled()) {
          if (!player.getPosition().equals(player.linePosition)
              || !player.getRotation().equals(player.lineRotation)) {
            if (player.linePosition != null)
              World.getWorld().getLevel().clearFire(player.linePosition, player.lineRotation);
            World.getWorld().getLevel().drawFire(player.getPosition(), player.getRotation());
            player.linePosition = player.getPosition();
            player.lineRotation = player.getRotation();
          }
          World.getWorld()
              .getGameMode()
              .processFlamethrower(player, player.linePosition, player.lineRotation);
        }
        player.sendFlamethrowerFuel();
      } else {
        if (player.flamethrowerFuel != (float) Constants.FLAME_THROWER_FUEL) {
          int chargeTime = GameSettings.getInt("FlameThrowerRechargeTime");
          float rechargeRate = (float) Constants.FLAME_THROWER_FUEL / chargeTime;
          long time = System.currentTimeMillis();
          long dt = time - player.flamethrowerTime;
          // Recharge rate in seconds, dt in milliseconds
          player.flamethrowerFuel += rechargeRate * dt / 1000;
          player.flamethrowerTime = time;
          if (player.flamethrowerFuel >= Constants.FLAME_THROWER_FUEL) {
            player.flamethrowerFuel = Constants.FLAME_THROWER_FUEL;
            player.getActionSender().sendChatMessage("- &eFlame thrower charged.");
          }
          player.sendFlamethrowerFuel();
        }
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

      if (player.setPayloadPath) {
        Position currentPosition = player.getPosition().toBlockPos();
        if (player.payloadPathPositions.isEmpty()
            || !currentPosition.equals(
                player.payloadPathPositions.get(player.payloadPathPositions.size() - 1))) {
          player.payloadPathPositions.add(currentPosition);
        }
      }

      if (payloadPosition != null) {
        float px = payloadPosition.getX() + 0.5f,
            py = payloadPosition.getY() + 0.5f,
            pz = payloadPosition.getZ() + 0.5f;
        float pr = Constants.PAYLOAD_RADIUS + 0.5f;
        float tx = (player.getPosition().getX()) / 32f;
        float ty = (player.getPosition().getY()) / 32f;
        float tz = (player.getPosition().getZ()) / 32f;

        if (Math.abs(px - tx) < pr && Math.abs(py - ty) < pr && Math.abs(pz - tz) < pr) {
          if (player.team == 1) {
            payloadAttackers++;
          } else if (player.team == 0) {
            payloadDefenders++;
          }
        }
      }
    }
    if (ctf.getMode() == Level.PAYLOAD) {
      if (payloadDefenders == 0) {
        payloadStep += payloadAttackers;
        if (payloadStep == 50) {
          payloadStep = 0;
          ctf.updatePayload(ctf.payloadPosition + 1);
        }
      }
      showTimer(
          "PayloadTimeLimit",
          true /* shouldEndGame */,
          "Payload | " + World.getWorld().getLevel().id);

      double fraction = (double) ctf.payloadPosition / world.getLevel().getPayloadPath().size();
      int lineWidth = 20;
      int blueWidth = (int) Math.round(fraction * lineWidth);
      String message = "&9";
      for (int i = 0; i < lineWidth; i++) {
        if (i == blueWidth) {
          if (payloadDefenders > 0) {
            message += "&cX&7";
          } else if (payloadAttackers > 0) {
            message += ">&7";
          } else {
            message += "o&7";
          }
        } else {
          message += "-";
        }
      }
      if (!message.equals(payloadStatus)) {
        payloadStatus = message;
        ctf.setStatusMessage(payloadStatus);
      }
    } else if (ctf.getMode() == Level.TDM) {
      showTimer("TDMTimeLimit", true /* shouldEndGame */, "Team Deathmatch");
    } else if (GameSettings.getBoolean("Tournament")) {
      showTimer("TournamentTimeLimit", false /* shouldEndGame */, "Tournament");
    }
    ticks++;
    if (ticks == 10) {
      ticks = 0;
    }
  }

  private void showTimer(String settingName, boolean shouldEndGame, String message) {
    long elapsedTime = System.currentTimeMillis() - World.getWorld().getGameMode().gameStartTime;
    if (shouldEndGame && elapsedTime > GameSettings.getInt(settingName) * 60 * 1000) {
      World.getWorld().getGameMode().gameStartTime = System.currentTimeMillis();
      World.getWorld().getGameMode().endGame();
    }
    long remaining =
        Math.max((GameSettings.getInt(settingName) * 60 * 1000 - elapsedTime) / 1000, 0);
    if (World.getWorld().getGameMode().voting) {
      remaining = 0;
    } else if (!World.getWorld().getGameMode().tournamentGameStarted) {
      remaining = GameSettings.getInt(settingName) * 60;
    }
    if (ticks == 0) {
      for (Player player : world.getPlayerList().getPlayers()) {
        if (player.getSession().isExtensionSupported("MessageTypes")) {
          World.getWorld().getGameMode().sendStatusMessage(player);
          player
              .getActionSender()
              .sendChatMessage(message + " | " + prettyTime((int) remaining), 3);
        }
      }
    }
  }

  private static String prettyTime(int seconds) {
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
