package group.pkg22_project;

import java.io.Serializable;
import java.time.LocalDateTime;



public class BackupVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String versionId;
    private LocalDateTime createdAt;
    private int fileCount;
    private long bytesCopied;
    private int filesSkipped;
    private String status;
    private String message;
    
    

    public BackupVersion(String versionId, LocalDateTime createdAt) {
        this.versionId = versionId;
        this.createdAt = createdAt;
        this.status = "RUNNING";
        this.message = "";
    }

    public String getDetails() {
        return versionId + "  " + status
                + "  copied=" + fileCount
                + "  skipped=" + filesSkipped
                + "  bytes=" + bytesCopied
                + "  " + message;
    }
    public String getVersionId() { return versionId; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }

    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public void setFilesSkipped(int filesSkipped) { this.filesSkipped = filesSkipped; }
    public void setBytesCopied(long bytesCopied) { this.bytesCopied = bytesCopied; }
    public void setStatus(String status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
}
