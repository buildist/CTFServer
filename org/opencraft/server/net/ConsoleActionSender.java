package org.opencraft.server.net;

import org.opencraft.server.Server;

public class ConsoleActionSender extends ActionSender{

    public ConsoleActionSender() {
        super(null);
    }

    public void sendChatMessage(int id, String message, boolean isWOM, int messageType) {
        if(!isWOM && messageType == 0)
            Server.log(">> "+message);
    }
}
