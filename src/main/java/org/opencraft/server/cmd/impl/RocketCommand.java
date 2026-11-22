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
package org.opencraft.server.cmd.impl;

import org.opencraft.server.Server;
import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.mechanics.Projectile;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;

public class RocketCommand implements Command {

  private static final RocketCommand INSTANCE = new RocketCommand();

  /**
   * Gets the singleton instance of this command.
   *
   * @return The singleton instance of this command.
   */
  public static RocketCommand getCommand() {
    return INSTANCE;
  }

  public void execute(final Player player, CommandParameters params) {
    player.rocketsShot++;
    player.rocketTime = System.currentTimeMillis();
    player.rocketStartPosition = player.getPosition().toBlockPos();

    Position pos = player.getPosition();
    Position start = new Position(pos.getX(), pos.getY(), pos.getZ() + 24);

    double heading = Math.toRadians(
        (int) (Server.getUnsigned(player.getRotation().getRotation())
            * ((float) 360 / 256) - 90)
    );

    double pitch = Math.toRadians(
        (int) (360 - Server.getUnsigned(player.getRotation().getLook())
            * ((float) 360 / 256))
    );

    double speed = 1000.0;

    double velocityX = Math.cos(pitch) * Math.cos(heading) * speed;
    double velocityY = Math.cos(pitch) * Math.sin(heading) * speed;
    double velocityZ = Math.sin(pitch) * speed;

    Projectile rocket = new Projectile(
        player,
        start,
        velocityX,
        velocityY,
        velocityZ,
        0,
        1.0,
        "rocket"
    );

    rocket.setId(calculateEntityId());
    rocket.launch();
  }

  public int calculateEntityId() {
    int players = World.getWorld().getPlayerList().size();
    int entities = World.getWorld().getEntityList().size();
    int newId = (254 - players - entities);
    if (newId <= players) return -2; // -1 = self, so we use -2 for invalid
    return newId;
  }
}
