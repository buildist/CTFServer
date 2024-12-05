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

import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

/* Does the delayed explosion for creeper item. */
public class CreeperTask extends ScheduledTask {
  private static final boolean LETHAL = true;
  private static final boolean TEAMKILL = true;
  private static final String NAME = "Creeper";

  private CTFGameMode ctf;
  private Player invoker;
  private Level level;

  public CreeperTask(Player invoker, Level level) {
    super(0);

    this.ctf = (CTFGameMode)World.getWorld().getGameMode();
    this.invoker = invoker;
    this.level = level;

    float creeperTime = GameSettings.getFloat("CreeperTime");
    if (creeperTime > 0.0f) {
      this.setDelay((long)(1000 * creeperTime));
    }
  }

  public void execute() {
    /* Only explode if:
     * - the player hasn't died
     * - the player hasn't changed teams
     * - the player hasn't rejoined his own team
     * - the game hasn't ended
     * - a new game hasn't started
     * - the player's session hasn't been unregistered */
    if (
      invoker.isCreepering
        && invoker.getSession().getPlayer() != null
    ) {
      doCreeper();
    }

    this.stop();
  }

  private void doCreeper() {
    int radius = GameSettings.getInt("CreeperRadius");
    if (radius < 0) {
      radius = 0;
    }

    Position pos = this.invoker.getPosition().toBlockPos();

    ctf.explodeTNT(
      this.invoker, this.level, pos.getX(), pos.getY(), pos.getZ(),
      radius, LETHAL, TEAMKILL, false, NAME
    );

    invoker.isCreepering = false;
  }
}
