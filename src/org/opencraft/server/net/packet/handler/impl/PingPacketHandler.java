package org.opencraft.server.net.packet.handler.impl;

import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

public class PingPacketHandler implements PacketHandler<MinecraftSession> {

  @Override
  public void handlePacket(MinecraftSession session, Packet packet) {
    boolean serverToClient = packet.getNumericField("server_to_client").byteValue() == (byte) 1;
    byte b = packet.getNumericField("server_to_client").byteValue();
    int data = packet.getNumericField("data").intValue();
    if (serverToClient) {
      session.getPlayer().pingList.update(data);
    } else {
      session.getActionSender().sendPing(false, data);
    }
  }
}
