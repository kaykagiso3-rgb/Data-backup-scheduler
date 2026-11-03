package group.pkg22_project;

import java.nio.file.Path;
import java.util.List;

public class AppFacade {
    private final JobStore store = new JobStore();
    private final LogService logger = new LogService();
    private final BackupEngine engine;
    private final RestoreService restore;
    private final SchedulerService scheduler;

    public AppFacade() {
        store.load();
        engine = new BackupEngine(store, logger);
        restore = new RestoreService(logger, engine);
        scheduler = new SchedulerService(store, engine, logger);
        scheduler.start();
    }

    public void shutdown() {
        scheduler.stop();
        store.save();
    }

    public List<BackupJob> listJobs() {
        return store.getAll();
    }

    public void addJob(BackupJob job) {
        store.add(job);
        store.save();
        logger.info("added job " + job.getJobId());
    }

    public boolean deleteJob(String jobId) {
        boolean removed = store.remove(jobId);
        if (removed) {
            store.save();
            logger.info("deleted job " + jobId);
        }
        return removed;
    }

    public boolean setJobEnabled(String jobId, boolean enabled) {
        BackupJob job = store.find(jobId);
        if (job == null) {
            return false;
        }
        job.setEnabled(enabled);
        store.save();
        logger.info((enabled ? "enabled" : "disabled") + " job " + jobId);
        return true;
    }

    public BackupVersion backupNow(String jobId) {
        BackupJob job = store.find(jobId);
        if (job == null) {
            return null;
        }
        return engine.run(job);
    }

    public List<String> listVersions(String jobId) {
        BackupJob job = store.find(jobId);
        return job == null ? List.of() : engine.listVersionIds(job);
    }

    public List<String> listVersionDetails(String jobId) {
        BackupJob job = store.find(jobId);
        if (job == null) {
            return List.of();
        }
        return engine.listVersionIds(job);
    }

    public void restore(String jobId, String versionId, Path target) {
        BackupJob job = store.find(jobId);
        if (job != null) {
            restore.restore(job, versionId, target);
        }
    }

    public List<String> getRecentLog() {
        return logger.getRecent();
    }
}