package org.opencraft.server.net.IRC;

public class IRCBot extends IRCMessageLoop {
    IRCBot(String server, int port) {
        super(server, port);
    }

    void raw(Message msg) {
        // Work in progress command to show online players
        if (msg.content.equals(".who") || msg.content.equals("/who")) {
            if (msg.target.startsWith("#")) {
                privmsg("Coming soon!");
            }
        }
    }

    public static void SendMessage(String msg) {
        privmsg(msg);
    }

    public static void Execute() {
        IRCBot client = new IRCBot("irc.rizon.net", 6667);

        client.nick("CTF");
        client.user("CTF", "null", "null", "real name");
        client.join("#CTF");
        client.run();
    }
}