package org.opencraft.server.task.impl;

import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.server.task.ScheduledTask;

public class PingTask extends ScheduledTask {

  private static final long DELAY = 3000;

  public PingTask() {
    super(DELAY);
  }

  @Override
  public void execute() {
    for (Player player : World.getWorld().getPlayerList().getPlayers()) {
      if (player.getSession().isExtensionSupported("TwoWayPing")) {
        player.getSession().getActionSender().sendPing(true, player.pingList.nextData());
      }
    }
  }
}
