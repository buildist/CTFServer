package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;

public class SetPathCommand implements Command {

  private static final SetPathCommand INSTANCE = new SetPathCommand();

  public static SetPathCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    if (player.isOp()) {
      if (player.setPayloadPath) {
        player.setPayloadPath = false;
        String positions = "";
        for (Position position : player.payloadPathPositions) {
          positions += position.getX()+","+position.getY()+","+position.getZ()+" ";
        }
        MapSetCommand.doPropertyChange("payloadPath", positions);
        ((CTFGameMode)World.getWorld().getGameMode()).updatePayload(0);
      } else {
        player.setPayloadPath = true;
        player.payloadPathPositions.clear();
        player.getActionSender().sendChatMessage(
            "- &eWalk from the start point to the end point. Use /setpath again when done.");
      }
    } else
      player.getActionSender().sendChatMessage("You need to be op to do that!");
  }
}
