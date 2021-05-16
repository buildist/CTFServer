package org.opencraft.server.net.IRC;

// Class only parses messages it understands. If a message is not understood,
// the origin and command are extracted and parsing halts.

public class MessageParser {
    static Message message(String ircMessage) {
        Message message = new Message();
        int spIndex;

        if (ircMessage.startsWith(":")) {
            spIndex = ircMessage.indexOf(' ');
            if (spIndex > -1) {
                message.origin = ircMessage.substring(1, spIndex);
                ircMessage = ircMessage.substring(spIndex + 1);

                int uIndex = message.origin.indexOf('!');
                if (uIndex > -1) {
                    message.nickname = message.origin.substring(0, uIndex);
                }
            }
        }
        spIndex = ircMessage.indexOf(' ');
        if (spIndex == -1) {
            message.command = "null";
            return message;
        }

        message.command = ircMessage.substring(0, spIndex).toLowerCase();
        ircMessage = ircMessage.substring(spIndex + 1);

        // parse privmsg params
        if (message.command.equals("privmsg")) {
            spIndex = ircMessage.indexOf(' ');
            message.target = ircMessage.substring(0, spIndex);
            ircMessage = ircMessage.substring(spIndex + 1);

            if (ircMessage.startsWith(":")) {
                message.content = ircMessage.substring(1);
            }
            else {
                message.content = ircMessage;
            }
        }

        if (message.command.equals("quit") || message.command.equals("join")) {
            if (ircMessage.startsWith(":")) {
                message.content = ircMessage.substring(1);
            }
            else {
                message.content = ircMessage;
            }
        }

        if (message.command.equals("ping")) {
            spIndex = ircMessage.indexOf(' ');
            if (spIndex > -1) {
                message.content = ircMessage.substring(0, spIndex);
            }
            else {
                message.content = ircMessage + " ggg";
            }
        }
        return message;
    }
}
