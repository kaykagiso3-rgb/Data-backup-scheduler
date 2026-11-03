
package group.pkg22_project;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {
    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor();
    private final JobStore store;
    private final BackupEngine engine;
    private final LogService logger;
    private final int checkSeconds = 30;

    public SchedulerService(JobStore store, BackupEngine engine, LogService logger) {
        this.store = store;
        this.engine = engine;
        this.logger = logger;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 5, checkSeconds, TimeUnit.SECONDS);
        logger.info("Scheduler started (checks every " + checkSeconds + "s)");
    }

    public void stop() {
        executor.shutdownNow();
        logger.info("Scheduler stopped");
    }

    private void tick() {
        LocalDateTime now = LocalDateTime.now();
        for (BackupJob job : store.getAll()) {
            if (job.isDue(now)) {
                logger.info("Scheduler running due job " + job.getName());
                engine.run(job);
            }
        }
    }
}
