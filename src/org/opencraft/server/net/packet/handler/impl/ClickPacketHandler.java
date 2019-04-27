package org.opencraft.server.net.packet.handler.impl;

import org.opencraft.server.Server;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.World;
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
    if (session.getPlayer().team == -1
        || session.getPlayer().getAmmo() == 0
        || session.getPlayer().getHealth() == 0) {
      return;
    }
    session.getPlayer().setAmmo(session.getPlayer().getAmmo() - 1);
    int yawInt = (short) packet.getNumericField("yaw");
    int pitchInt = (short) packet.getNumericField("pitch");
    double yaw =
        Math.toRadians(
            (int) (Server.getUnsignedShort(yawInt) * ((float) 360 / 65536)) - 90);
    double pitch =
        Math.toRadians(
            (int) (360 - Server.getUnsignedShort(pitchInt) * ((float) 360 / 65536)));

    Position pos = session.getPlayer().getPosition();
    double startX = pos.getX() / 32.0;
    double startY = pos.getY() / 32.0;
    double startZ = pos.getZ() / 32.0;
    double x = startX;
    double y = startY;
    double z = startZ;
    double vx = Math.cos(yaw) * Math.cos(pitch) * 0.125;
    double vy = Math.sin(yaw) * Math.cos(pitch) * 0.125;
    double vz = Math.sin(pitch) * 0.125;
    for (int i = 0; i < 2048; i++) {
      x += vx;
      y += vy;
      z += vz;
      int bx = (int) Math.floor(x);
      int by = (int) Math.floor(y);
      int bz = (int) Math.floor(z);
      int block = World.getWorld().getLevel().getBlock(bx, by, bz);
      if (block != 0 && block != 20) {
        break;
      }
      for (Player p : World.getWorld().getPlayerList().getPlayers()) {
        Position playerPos = p.getPosition();
        double px = playerPos.getX() / 32.0;
        double py = playerPos.getY() / 32.0;
        double pz = playerPos.getZ() / 32.0 - 0.5;
        if (session.getPlayer() != p
            && x > px - 0.5 && x < px + 0.5
            && y > py - 0.5 && y < py + 0.5
            && z > pz - 1 && z < pz + 1) {
          World.getWorld().getGameMode().onHit(session.getPlayer(), p, x, y, z + 1.25);
          i = 2048;
          break;
        }
      }
    }
    World.getWorld().getGameMode().addLaser(
        session.getPlayer(), startX, startY, startZ + 1, x, y, z + 1);
  }
}
