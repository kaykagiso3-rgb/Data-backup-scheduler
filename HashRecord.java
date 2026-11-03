package group.pkg22_project;


import java.io.Serializable;
import java.time.LocalDateTime;

public class HashRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String jobId;
    private final String relativePath;
    private final String hash;
    private final long fileSize;
    private final LocalDateTime recordedAt;

    public HashRecord(String jobId, String relativePath, String hash, long fileSize) {
        this.jobId = jobId;
        this.relativePath = relativePath;
        this.hash = hash;
        this.fileSize = fileSize;
        this.recordedAt = LocalDateTime.now();
    }

    public String getJobId() { return jobId; }
    public String getRelativePath() { return relativePath; }
    public String getHash() { return hash; }
    public long getFileSize() { return fileSize; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}