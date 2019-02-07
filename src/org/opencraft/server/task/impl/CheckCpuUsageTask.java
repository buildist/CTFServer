package org.opencraft.server.task.impl;

import org.opencraft.server.Server;
import org.opencraft.server.task.ScheduledTask;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class CheckCpuUsageTask extends ScheduledTask {

  private static final long DELAY = 1 * 30000;

  private final OperatingSystemMXBean osBean;

  public CheckCpuUsageTask() {
    super(0);
    osBean =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  }

  @Override
  public void execute() {
    if (this.getDelay() == 0) {
      this.setDelay(DELAY);
    }
    double load = osBean.getSystemLoadAverage();

    if (load > 1.0) {
      Server.log("[E] CPU load: " + load);
    }
  }
}
