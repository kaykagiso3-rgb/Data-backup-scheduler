
package group.pkg22_project;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupJob implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String jobId;
    private String name;
    private String sourcePath;
    private String destPath;
    private int intervalMinutes;
    private int keepVersions;
    private boolean enabled;
    private LocalDateTime lastRun;

    public BackupJob(String jobId, String name, String sourcePath,
                     String destPath, int intervalMinutes, int keepVersions) {
        this.jobId = jobId;
        this.name = name;
        this.sourcePath = sourcePath;
        this.destPath = destPath;
        this.intervalMinutes = intervalMinutes;
        this.keepVersions = keepVersions;
        this.enabled = true;
        this.lastRun = null;
    }

    public boolean isDue(LocalDateTime now) {
        if (!enabled) {
            return false;
        }
        if (lastRun == null) {
            return true;
        }
        return !now.isBefore(lastRun.plusMinutes(intervalMinutes));
    }

    public void markRan(LocalDateTime time) {
        this.lastRun = time;
    }

    public String getJobId() { return jobId; }
    public String getName() { return name; }
    public String getSourcePath() { return sourcePath; }
    public String getDestPath() { return destPath; }
    public int getIntervalMinutes() { return intervalMinutes; }
    public int getKeepVersions() { return keepVersions; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getLastRun() { return lastRun; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public void setKeepVersions(int keepVersions) {
        this.keepVersions = keepVersions;
    }

    public String getStatusText() {
        String status = enabled ? "ENABLED" : "DISABLED";
        String last = (lastRun == null) ? "never" : lastRun.format(FMT);
        return status + " | last=" + last + " | every " + intervalMinutes + " min";
    }

    @Override
    public String toString() {
        return jobId + " | " + name
                + " | " + sourcePath + " -> " + destPath
                + " | " + getStatusText();
    }
}
