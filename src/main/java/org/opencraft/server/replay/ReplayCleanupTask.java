package org.opencraft.server.replay;

import java.io.File;
import java.util.concurrent.TimeUnit;
import org.opencraft.server.Server;

public class ReplayCleanupTask implements Runnable {
    public static final long TIMEOUT = TimeUnit.DAYS.toMillis(15L);

    private static final ReplayCleanupTask INSTANCE = new ReplayCleanupTask();

    private ReplayCleanupTask() {
    }

    public static ReplayCleanupTask getInstance() {
        return INSTANCE;
    }

    private void tryToDelete(File file) {
        if (!file.delete()) {
            Server.log("Failed to delete " + file.getName() + " replay file");
        }
    }

    @Override
    public void run() {
        File[] replayFiles = new File(ReplayFile.REPLAY_DIRECTORY).listFiles();
        if (replayFiles == null) {
            Server.log("replayFiles == null (?)");

            return;
        }
        for (File file : replayFiles) {
            String name;
            if (!(name = file.getName()).endsWith(".ltr")) {
                Server.log("Non-LTR file in \"replays\" directory: " + name);
                tryToDelete(file);

                continue;
            }
            if (name.contains("important")) continue;

            if (System.currentTimeMillis() - file.lastModified() >= TIMEOUT) tryToDelete(file);
        }
    }
}
