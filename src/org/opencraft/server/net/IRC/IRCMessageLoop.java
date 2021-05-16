package org.opencraft.server.net.IRC;

import org.opencraft.server.Server;
import org.opencraft.server.model.World;

import java.io.*;
import java.net.*;
import java.util.*;

public abstract class IRCMessageLoop implements Runnable {
    Socket server;
    public static OutputStream out;
    List<String> channelList;
    boolean initial_setup_status;

    IRCMessageLoop(String serverName, int port) {
        channelList = new ArrayList<>();
        try {
            server = new Socket(serverName, port);
            out = server.getOutputStream();
        }
        catch (IOException ex) {
            Server.log(ex);
        }
    }

    public static void send(String text) {
        byte[] bytes = (text + "\r\n").getBytes();

        try {
            out.write(bytes);
        }
        catch (IOException ex) {
            Server.log(ex);
        }
    }

    void nick(String nickname) {
        String msg = "NICK " + nickname;
        send(msg);
    }

    void user(String username, String hostname, String servername, String realname) {
        String msg = "USER " + username + " " + hostname + " " + servername +  " :" + realname;
        send(msg);
    }

    void join(String channel) {
        if (!initial_setup_status) {
            channelList.add(channel);
            return;
        }
        String msg = "JOIN " + channel;
        send(msg);
    }

    public static void privmsg(String text) {
        String msg = "PRIVMSG #CTF :" + text;
        send(msg);
    }

    void pong(String server) {
        String msg = "PONG " + server;
        send(msg);
    }

    void quit(String reason) {
        String msg = "QUIT :Quit: " + reason;
        send(msg);
    }

    abstract void raw(Message msg);

    void initial_setup() {
        initial_setup_status = true;

        for (String channel: channelList) {
            join(channel);
        }
    }

    void processMessage(String ircMessage) {
        Message msg = MessageParser.message(ircMessage);

        switch (msg.command) {
            case "privmsg":
                raw(msg);
                System.out.println("(Discord) " + msg.content.substring(4).replace("\u000F>", ":"));
                World.getWorld().broadcast("&5(Discord) " + msg.content.substring(4).replace("\u000F>", "&f:"));
                break;
            case "001":
                initial_setup();
                return;
            case "ping":
                pong(msg.content);
                break;
        }
    }

    public void run() {
        InputStream stream;

        try
        {
            stream = server.getInputStream();
            MessageBuffer messageBuffer = new MessageBuffer();
            byte[] buffer = new byte[512];
            int count;

            while (true) {
                count = stream.read(buffer);
                if (count == -1)
                    break;
                messageBuffer.append(Arrays.copyOfRange(buffer, 0, count));
                while (messageBuffer.hasCompleteMessage()) {
                    String ircMessage = messageBuffer.getNextMessage();

                    //System.out.println("\"" + ircMessage + "\"");
                    processMessage(ircMessage);
                }
            }
        }
        catch (IOException info)
        {
            quit("error in messageLoop");
            info.printStackTrace();
        }
    }
}