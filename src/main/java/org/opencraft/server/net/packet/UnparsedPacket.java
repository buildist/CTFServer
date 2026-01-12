package org.opencraft.server.net.packet;

public class UnparsedPacket extends Packet {

  private final byte[] rawPacket;

  public UnparsedPacket(byte[] rawPacket) {
    super(null, null);

    this.rawPacket = rawPacket;
  }

  @Override
  public int getLength() {
    return rawPacket.length;
  }

  @Override
  public byte[] toByteArray() {
    return rawPacket;
  }
}
