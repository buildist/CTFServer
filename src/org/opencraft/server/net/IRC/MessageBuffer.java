package org.opencraft.server.net.IRC;

public class MessageBuffer {
    String buffer;

    public MessageBuffer() {
        buffer = "";
    }

    public void append(byte[] bytes) {
        buffer += new String(bytes);
    }

    public boolean hasCompleteMessage() {
        return buffer.contains("\r\n");
    }

    public String getNextMessage() {
        int index = buffer.indexOf("\r\n");
        String message = "";

        if (index > -1) {
            message = buffer.substring(0, index);
            buffer = buffer.substring(index + 2);
        }

        return message;
    }

    public static void main(String[] args) {
        MessageBuffer buf = new MessageBuffer();
        buf.append("blah\r\nblah blah\r\nblah blah oh uh".getBytes());

        while (buf.hasCompleteMessage()) {
            System.out.println("\"" + buf.getNextMessage() + "\"");
        }
        buf.append(" blah\r\n".getBytes());
        while (buf.hasCompleteMessage()) {
            System.out.println("\"" + buf.getNextMessage() + "\"");
        }

    }
}
