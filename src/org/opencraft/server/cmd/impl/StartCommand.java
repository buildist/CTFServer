package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class StartCommand implements Command {

  private static final StartCommand INSTANCE = new StartCommand();

  public static StartCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if ((player.isOp()) || player.isVIP()) {
      World.getWorld().broadcast("- &aGame will start in 10 seconds!");
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    Thread.sleep(10 * 1000);
                  } catch (InterruptedException ex) {
                  }
                  World.getWorld().getGameMode().tournamentGameStarted = true;
                  World.getWorld().getGameMode().gameStartTime = System.currentTimeMillis();
                  for (Player other : World.getWorld().getPlayerList().getPlayers()) {
                    if (other.team != -1) {
                      other.sendToTeamSpawn();
                    }
                  }
                  World.getWorld().broadcast("- &aThe game has started!");
                }
              })
          .start();
    } else player.getActionSender().sendChatMessage("You must be OP to do that");
  }
}
