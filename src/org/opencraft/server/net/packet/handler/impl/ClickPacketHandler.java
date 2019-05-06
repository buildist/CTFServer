package org.opencraft.server.net.packet.handler.impl;

import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

public class ClickPacketHandler implements PacketHandler<MinecraftSession> {
  @Override
  public void handlePacket(MinecraftSession session, Packet packet) {
    int button = (byte) packet.getNumericField("button");
    int action = (byte) packet.getNumericField("action");
    if (action > 0 || button > 0) {
      return;
    }
  }
}
