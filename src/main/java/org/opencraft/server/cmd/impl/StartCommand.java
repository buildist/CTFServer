package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import tf.jacobsc.utils.RatingKt;

public class StartCommand implements Command {

  private static final StartCommand INSTANCE = new StartCommand();

  public static StartCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (!player.isOp() && !player.isVIP()) {
      player.getActionSender().sendChatMessage("You must be OP to do that!");
      return;
    }

    int seconds = 10;
    if (params.getArgumentCount() >= 1) {
      try {
        seconds = params.getIntegerArgument(0);
        if (seconds <= 0) {
          player.getActionSender().sendChatMessage("Start seconds cannot be negative.");
          return;
        }
      } catch (NumberFormatException e) {
        player.getActionSender().sendChatMessage("Must be an integer representing number of seconds.");
        return;
      }
    }

    int quality = RatingKt.matchQuality();
    World.getWorld().broadcast("- &aGame will start in " + seconds + " seconds!");
    World.getWorld().broadcast("- &aGame is rated. Game quality is " + quality + "%");

    int finalSeconds = seconds;
    new Thread(
        () -> {
          try {
            Thread.sleep(finalSeconds * 1000L);
          } catch (InterruptedException ex) {
          }

          for (Player other : World.getWorld().getPlayerList().getPlayers()) {
            if (other.team != -1) {
              other.sendToTeamSpawn();
            }
          }

          World.getWorld().getGameMode().tournamentGameStarted = true;
          World.getWorld().getGameMode().gameStartTime = System.currentTimeMillis();
          World.getWorld().broadcast("- &aThe game has started!");

          // Hide other spectators during tournament games for viewability
          for (Player p : World.getWorld().getPlayerList().getPlayers()) {
            if (p.team != -1) {
              continue;
            }

            for (Player other : World.getWorld().getPlayerList().getPlayers()) {
              // Don't hide self or non-spec players
              if (other == p || other.team != -1) {
                continue;
              }

              p.getActionSender().sendRemoveEntity(other); // Hide their player entity
            }
          }
        })
        .start();
  }
}
