package group.pkg22_project;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JobStore {
    private List<BackupJob> jobs = new ArrayList<>();
    private List<HashRecord> hashRecords = new ArrayList<>();
    private final Path storeFile = Path.of("data", "jobs.dat");

    public void add(BackupJob job) {
        jobs.add(job);
    }

    public boolean remove(String jobId) {
        boolean removed = jobs.removeIf(j -> j.getJobId().equals(jobId));
        if (removed) {
            hashRecords.removeIf(r -> r.getJobId().equals(jobId));
        }
        return removed;
    }

    public BackupJob find(String jobId) {
        for (BackupJob job : jobs) {
            if (job.getJobId().equals(jobId)) {
                return job;
            }
        }
        return null;
    }

    public List<BackupJob> getAll() {
        return new ArrayList<>(jobs);
    }

    public void putHash(String jobId, String relPath, String hash, long fileSize) {
        hashRecords.removeIf(r ->
                r.getJobId().equals(jobId) && r.getRelativePath().equals(relPath));
        hashRecords.add(new HashRecord(jobId, relPath, hash, fileSize));
    }

    public String getHash(String jobId, String relPath) {
        return hashRecords.stream()
                .filter(r -> r.getJobId().equals(jobId) && r.getRelativePath().equals(relPath))
                .map(HashRecord::getHash)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (!Files.exists(storeFile)) {
            jobs = new ArrayList<>();
            hashRecords = new ArrayList<>();
            return;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(storeFile.toFile()))) {
            jobs = (List<BackupJob>) in.readObject();
            hashRecords = (List<HashRecord>) in.readObject();
        } catch (Exception e) {
            jobs = new ArrayList<>();
            hashRecords = new ArrayList<>();
            System.err.println("Could not load jobs: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(storeFile.getParent());
            try (ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(storeFile.toFile()))) {
                out.writeObject(jobs);
                out.writeObject(hashRecords);
            }
        } catch (IOException e) {
            System.err.println("Could not save jobs: " + e.getMessage());
        }
    }
}