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


import org.opencraft.server.Server;
import org.opencraft.server.model.Entity;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

import java.util.List;
import java.util.Set;

/**
 * Updates the players and game world.
 *
 * @author Graham Edgecombe
 */
public class UpdateTask extends ScheduledTask {

  /**
   * The delay.
   */
  private static final long DELAY = 150;

  /**
   * Creates the update task with a delay of 100ms.
   */
  public UpdateTask() {
    super(DELAY);
  }

  @Override
  public void execute() {
    final World world = World.getWorld();
    world.getGameMode().tick();
    List<Player> players = world.getPlayerList().getPlayers();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      Set le = player.getLocalEntities();
      Object[] localEntities = le.toArray();
      for (Object object : localEntities) {
        Entity localEntity = (Entity) object;
        if (localEntity.getId() == -1) {
          le.remove(localEntity);
          if (localEntity instanceof Player)
            player.getSession().getActionSender().sendRemovePlayer((Player) localEntity);
          else
            player.getSession().getActionSender().sendRemoveEntity(localEntity);
          Server.d("Removing " + localEntity.getName() + " from " + player.getName());
        } else {
          player.getSession().getActionSender().sendUpdateEntity(localEntity);
        }
      }
      for (int j = 0; j < players.size(); j++) {
        Player otherEntity = players.get(j);
        if (!le.contains(otherEntity) && otherEntity != player && otherEntity.isVisible) {
          le.add(otherEntity);
          player.getSession().getActionSender().sendAddPlayer(otherEntity, false);
          Server.d("Adding " + otherEntity.getName() + " to " + player.getName());
        }
      }
      if (player.lastPacketTime != 0 && System.currentTimeMillis() - player.lastPacketTime >
          30000) {
        player.getActionSender().sendLoginFailure("Too much lag`");
        player.getSession().close();
      }
    }
    for (Player player : world.getPlayerList().getPlayers()) {
      player.resetOldPositionAndRotation();
    }
    world.getLevel().applyBlockBehaviour();
  }

}
