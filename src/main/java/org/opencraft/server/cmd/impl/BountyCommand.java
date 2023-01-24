package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;

public class BountyCommand implements Command {
  private static final BountyCommand INSTANCE = new BountyCommand();

  public static BountyCommand getCommand() {
    return INSTANCE;
  }

  public void execute(Player player, CommandParameters params) {
    if (params.getArgumentCount() == 2) {
      Player hunted = player.getPlayer(params.getStringArgument(0), player.getActionSender());
      if (hunted.bountyAmount == 0) {
        player.bountyActive = false;
        player.bountySet = 0;
      }
      if (hunted.team != -1) {
        if (params.getIntegerArgument(1) == 0) {
          if (player.bountySet != 0) {
            player.addPoints(player.bountySet);
            hunted.bountiedBy = null;
            hunted.bountyAmount = hunted.bountyAmount - player.bountySet;
            if (hunted.bountyAmount == 0) {
              hunted.bountyMode = false;
            }
            player.bountyActive = false;
            World.getWorld()
                .broadcast(
                    "- "
                        + player.getColoredName()
                        + " &ehas removed their "
                        + "bounty on "
                        + hunted.getColoredName());
          } else {
            player
                .getActionSender()
                .sendChatMessage("You do not have an active bounty on this " + "player!");
          }
        } else {
          if (hunted != null && params.getIntegerArgument(1) >= 50) {
            if (params.getIntegerArgument(1) <= player.getPoints()) {
              if (player.bountyActive) {
                player
                    .getActionSender()
                    .sendChatMessage(
                        "You currently have an active bounty on " + "" + hunted.getColoredName());
                player
                    .getActionSender()
                    .sendChatMessage(
                        "Use \"/bounty " + hunted.getName() + " " + "0\" to remove it");
              } else {
                if (hunted.bountyMode) {
                  player.bountySet = params.getIntegerArgument(1);
                  hunted.bountied = hunted;
                  hunted.bountiedBy = null;
                  hunted.bountyAmount += player.bountySet;
                  hunted.bountyMode = true;
                  player.bountyActive = true;
                  player.addPoints(-params.getIntegerArgument(1));
                  World.getWorld()
                      .broadcast(
                          "- "
                              + player.getColoredName()
                              + " &ehas added "
                              + params.getIntegerArgument(1)
                              + " on "
                              + hunted.getColoredName()
                              + "&e's "
                              + "bounty! The bounty is now "
                              + hunted.bountyAmount);
                } else {
                  World.getWorld()
                      .broadcast(
                          "- "
                              + player.getColoredName()
                              + " &ehas set a "
                              + "bounty of "
                              + params.getIntegerArgument(1)
                              + " on "
                              + hunted.getColoredName()
                              + "! &4KILL &ethem five times to collect the bounty.");
                  player.bountySet = params.getIntegerArgument(1);
                  hunted.bountied = hunted;
                  hunted.bountiedBy = player;
                  hunted.bountyAmount += player.bountySet;
                  hunted.bountyMode = true;
                  player.bountyActive = true;
                  player.addPoints(-params.getIntegerArgument(1));
                }
              }
            } else {
              player.getActionSender().sendChatMessage("You do not have enough points!");
            }
          } else {
            player.getActionSender().sendChatMessage("You cannot set a bounty lower than 50!");
          }
        }

      } else {
        player.getActionSender().sendChatMessage("You cannot bounty someone who is not playing!");
      }
    } else {
      player.getActionSender().sendChatMessage("Wrong number of arguments");
      player.getActionSender().sendChatMessage("/bounty <player> <amount>");
      player.getActionSender().sendChatMessage("Use 0 for the amount to remove your bounty");
    }
  }
}
