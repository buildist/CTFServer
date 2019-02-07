package org.opencraft.server.net;

import org.opencraft.server.Server;

public class ConsoleActionSender extends ActionSender {

  public ConsoleActionSender() {
    super(null);
  }

  @Override
  public void sendChatMessage(String message, int messageType) {
    if (messageType == 0) Server.log(">> " + message);
  }
}
