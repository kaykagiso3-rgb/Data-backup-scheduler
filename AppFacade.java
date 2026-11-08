package group.pkg22_project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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

        try {
            Path source = Path.of(job.getSourcePath());
            Files.createDirectories(source);

            Path sample = source.resolve("sample_document.txt");
            if (!Files.exists(sample)) {
                String text = "Sample Document\n==============\n"
                        + "This file was created automatically when the job was added.\n"
                        + "Job Name : " + job.getName() + "\n"
                        + "Source   : " + job.getSourcePath() + "\n"
                        + "Created  : " + LocalDateTime.now() + "\n\n";
                Files.writeString(sample, text);
                logger.info("sample document created for job " + job.getJobId());
            }
        } catch (IOException e) {
            logger.error("could not create sample file: " + e.getMessage());
        }

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

    public boolean restore(String jobId, String versionId, Path target) {
        BackupJob job = store.find(jobId);
        if (job == null) {
            logger.error("restore failed: unknown job " + jobId);
            return false;
        }
        restore.restore(job, versionId, target);
        return true;
    }

    public List<String> getRecentLog() {
        return logger.getRecent();
    }
}
