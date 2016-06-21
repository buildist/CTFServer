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
import org.opencraft.server.Server;
import org.opencraft.server.cmd.impl.PlayerCommand;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.heartbeat.HeartbeatManager;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

import java.util.HashMap;
import java.util.Map;

/**
 * A task which sends a heartbeat periodically to the master server.
 *
 * @author Graham Edgecombe
 */
public class HeartbeatTask extends ScheduledTask {

  /**
   * The delay.
   */
  private static final long DELAY = 30000;

  /**
   * Creates the heartbeat task with a 45s delay.
   */
  public HeartbeatTask() {
    super(0);
  }

  @Override
  public void execute() {
    if (getDelay() == 0) {
      setDelay(DELAY);
    }
    final int players;
    if (PlayerCommand.playerCount == 0 || PlayerCommand.playerCount < World.getWorld()
        .getPlayerList().size())
      players = World.getWorld().getPlayerList().size();
    else
      players = PlayerCommand.playerCount;
    final Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("users", String.valueOf(players));
    parameters.put("max", String.valueOf(Configuration.getConfiguration().getMaximumPlayers()));
    parameters.put("public", String.valueOf(Configuration.getConfiguration().isPublicServer()));
    parameters.put("port", String.valueOf(Constants.PORT));
    parameters.put("salt", String.valueOf(HeartbeatManager.getHeartbeatManager().getSalt()));
    parameters.put("version", String.valueOf(Constants.PROTOCOL_VERSION));
    parameters.put("software", Constants.VERSION);
    new Thread(new Runnable() {
      public void run() {
        String name = Configuration.getConfiguration().getLongName().replace("_m", World.getWorld
            ().getLevel().id);
        HeartbeatManager.getHeartbeatManager().sendHeartbeat("http://www.classicube" +
            ".net/server/heartbeat", parameters, name);
      }
    }).start();
        /*new Thread(new Runnable()
        {
            public void run()
            {
                Date d = new Date();
                String name = Configuration.getConfiguration().getName().replace("_d", "(Online
                "+(d.getMonth()+1)+"/"+d.getDate()+"/"+(d.getYear()-100)+")");
                HeartbeatManager.getHeartbeatManager().sendHeartbeat("https://minecraft
                .net/heartbeat.jsp", parameters, name);
            }
        }).start();*/
    new Thread(new Runnable() {
      public void run() {
        if (!GameSettings.getBoolean("Tournament"))
          Server.httpGet(Constants.URL_SERVER_STATUS + "?status[players]=" + World.getWorld()
              .getPlayerList().size() + "&status[map]=" + World.getWorld().getLevel().id);
      }
    }).start();
  }
}
