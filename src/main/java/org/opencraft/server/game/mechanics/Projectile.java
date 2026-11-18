package org.opencraft.server.game.mechanics;

import org.opencraft.server.game.impl.CTFGameMode;
import org.opencraft.server.game.impl.GameSettings;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.Entity;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.SmokeZone;
import org.opencraft.server.model.World;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Projectile extends Entity {

  private final Player owner;
  private double velocityX;
  private double velocityY;
  private double velocityZ;
  private String type = null;

  private final double gravity;
  private final double friction;

  private long lastUpdate;

  private boolean finished;

  private static final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(2);

  private ScheduledFuture<?> task;

  public Projectile(Player player, Position position,
      double velocityX,
      double velocityY,
      double velocityZ,
      double gravity,
      double friction,
      String type) {
    owner = player;
    setPosition(position);

    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.velocityZ = velocityZ;

    this.gravity = gravity;
    this.friction = friction;
    this.type = type;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public String getColoredName() {
    return "";
  }

  public void launch() {
    lastUpdate = System.currentTimeMillis();
    finished = false;

    World.getWorld().getEntityList().add(this);
    spawn();

    task = scheduler.scheduleAtFixedRate(
        this::update,
        0,
        25,
        TimeUnit.MILLISECONDS
    );
  }

  private void spawn() {
    Position position = getPosition();

    for (Player player : World.getWorld().getPlayerList().getPlayers(true)) {
      player.getActionSender().sendExtSpawn(
          (byte) getId(),
          "",
          "",
          position.getX(),
          position.getY(),
          position.getZ(),
          (byte) 0,
          (byte) 0
      );

      String model = "607";
      if (owner.team == 1) model = "611";
      if (this.type.equalsIgnoreCase("rocket")) model = "49";
      if (this.type.equalsIgnoreCase("smokegrenade")) model = "34";

      player.getActionSender().sendChangeModel((byte) getId(), model);
    }
  }

  public void update() {
    if (finished) {
      return;
    }

    long now = System.currentTimeMillis();

    double delta = (now - lastUpdate) / 1000.0;
    lastUpdate = now;

    delta = Math.min(delta, 0.1);

    Position position = getPosition();
    Position blockPosition = position.toBlockPos();

    int block = World.getWorld().getLevel().getBlock(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    if (block != BlockConstants.AIR) {
      if (type.equalsIgnoreCase("smokegrenade")) {
        int zones = World.getWorld().getNumberOfSmokeZones();
        int id = zones + 1;
        int radius = GameSettings.getInt("SmokeGrenadeRadius");
        int delay = GameSettings.getInt("SmokeGrenadeDelay");
        final SmokeZone zone = new SmokeZone(blockPosition.getX() - radius, blockPosition.getY() - radius,
            blockPosition.getZ(), blockPosition.getX() + radius, blockPosition.getY() + radius,
            blockPosition.getZ() + radius, id);

        try {
          World.getWorld().addSmokeZone(zone);
          zone.updateDensity(255);
          Thread.sleep(delay);
          zone.updateDensity(191);
          Thread.sleep(delay);
          zone.updateDensity(127);
          Thread.sleep(delay);
          zone.updateDensity(64);
          Thread.sleep(delay);
          World.getWorld().removeSmokeZone(zone);
        } catch (InterruptedException ex) {
          World.getWorld().removeSmokeZone(zone);
        }
      }

      else {
        ((CTFGameMode) World.getWorld()
            .getGameMode())
            .explodeTNT(
                owner,
                World.getWorld().getLevel(),
                blockPosition.getX(),
                blockPosition.getY(),
                blockPosition.getZ(),
                2,
                true,
                false,
                false,
                false,
                type);
      }
      finish();
      return;
    }

    velocityZ -= gravity * delta;
    velocityX *= Math.pow(friction, delta);
    velocityY *= Math.pow(friction, delta);

    double x = position.getX() + velocityX * delta;
    double y = position.getY() + velocityY * delta;
    double z = position.getZ() + velocityZ * delta;

    setPosition(new Position(
        (int) Math.round(x),
        (int) Math.round(y),
        (int) Math.round(z)
    ));

    for (Player player : World.getWorld().getPlayerList().getPlayers(true)) {
      player.getActionSender().sendUpdateEntity(this);
    }

    // Fell out of map
    int width = World.getWorld().getLevel().getWidth();
    int length = World.getWorld().getLevel().getHeight();
    int height = World.getWorld().getLevel().getDepth();
    if ((x/32) < 0 || (x/32) >= width || (z/32) < 0 || (z/32) >= height || (y/32) < 0 || (y/32) >= length) {
      finish();
    }
  }

  private void finish() {
    if (finished) {
      return;
    }

    finished = true;

    if (task != null) {
      task.cancel(false);
    }

    for (Player player : World.getWorld().getPlayerList().getPlayers(true)) {
      player.getActionSender().sendRemoveEntity(getId());
    }

    World.getWorld().getEntityList().remove(this);
  }
}