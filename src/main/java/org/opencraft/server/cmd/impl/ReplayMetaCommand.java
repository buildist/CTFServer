package org.opencraft.server.cmd.impl;

public class ReplayMetaCommand extends ReplayCommand {

  private static final ReplayMetaCommand INSTANCE = new ReplayMetaCommand();

  public ReplayMetaCommand() {
    super(ReplayCommand.MODE_ONLY_VIEW_METADATA, false);
  }

  public static ReplayMetaCommand getCommand() {
    return INSTANCE;
  }
}
