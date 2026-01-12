package org.opencraft.server.cmd.impl;

public class MarkReplayCommand extends ReplayCommand {

  private static final MarkReplayCommand INSTANCE = new MarkReplayCommand();

  public MarkReplayCommand() {
    super(ReplayCommand.MODE_MARK_IMPORTANT);
  }

  public static MarkReplayCommand getCommand() {
    return INSTANCE;
  }
}
