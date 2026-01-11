package org.opencraft.server.net;

import org.opencraft.server.model.Player;

public class FakePlayerBase extends Player {
  public static class FakeMinecraftSession extends MinecraftSession {
    public FakeMinecraftSession() {
      super(null);

      ccUser = true;
    }

    @Override
    public boolean isExtensionSupported(String name, int version) {
      return true;
    }
  }

  public static final FakePlayerBase CAMERA_MAN = new FakePlayerBase("#CameraMan");

  public FakePlayerBase(String name) {
    super(new FakeMinecraftSession(), name);

    getSession().setPlayer(this);
  }
}
