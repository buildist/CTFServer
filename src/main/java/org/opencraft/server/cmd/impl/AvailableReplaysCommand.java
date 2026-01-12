package org.opencraft.server.cmd.impl;

public class AvailableReplaysCommand extends ReplayCommand {

  private static final AvailableReplaysCommand INSTANCE = new AvailableReplaysCommand();

  public AvailableReplaysCommand() {
    super(ReplayCommand.MODE_VIEW_IDS);
  }

  public static AvailableReplaysCommand getCommand() {
    return INSTANCE;
  }
}
