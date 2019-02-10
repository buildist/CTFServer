package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;

public class RCommand implements Command{
  private static final RCommand INSTANCE = new RCommand();

  public static RCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if (player.isReloading )return;
    player.reloadStep = 0;
    player.isReloading = true;
  }
}
