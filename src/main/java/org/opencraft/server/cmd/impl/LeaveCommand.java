package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;

public class LeaveCommand implements Command {
  private static final LeaveCommand INSTANCE = new LeaveCommand();

  public static LeaveCommand getCommand() {
    return INSTANCE;
  }

  @Override
  @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
  public void execute(Player player, CommandParameters params) {
    synchronized (player) {
      if (!player.watchingReplay) {
        player.sendMessage("- &eYou are not currently watching any replay");

        return;
      }
      player.requestedToLeaveReplay = true;

      player.notifyAll();
    }
  }
}
