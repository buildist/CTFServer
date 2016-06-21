package org.opencraft.server.task.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import org.opencraft.server.Server;
import org.opencraft.server.task.ScheduledTask;

public class AutoRestartTask extends ScheduledTask {

    private static final long DELAY = 1 * 1000;

    private final OperatingSystemMXBean osBean;

    public AutoRestartTask() {
      super(0);
      osBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    public void execute() {
      if (this.getDelay() == 0) {
              this.setDelay(DELAY);
      }
      double load = osBean.getSystemLoadAverage();
      
      if (load > 1.5) {
        Server.restartServer("Automatic restart");
        stop();
      }
      Server.d("CPU load: " + osBean.getSystemLoadAverage());
    }

}
