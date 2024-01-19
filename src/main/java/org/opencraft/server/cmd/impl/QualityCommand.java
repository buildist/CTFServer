package org.opencraft.server.cmd.impl;

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import tf.jacobsc.utils.RatingKt;

public class QualityCommand implements Command {

  private static final QualityCommand INSTANCE = new QualityCommand();

  public static QualityCommand getCommand() {
    return INSTANCE;
  }

  @Override
  public void execute(Player player, CommandParameters params) {
    int quality = RatingKt.matchQuality();
    player.getActionSender().sendChatMessage("- &bGame quality is " + quality + "%.");
  }
}
