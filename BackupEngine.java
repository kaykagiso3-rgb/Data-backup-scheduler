
package group.pkg22_project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class BackupEngine {
    private final JobStore store;
    private final LogService logger;
    private final FileWalker walker = new FileWalker();
    private static final DateTimeFormatter VERSION_FMT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public BackupEngine(JobStore store, LogService logger) {
        this.store = store;
        this.logger = logger;
    }

    public BackupVersion run(BackupJob job) {
        LocalDateTime now = LocalDateTime.now();
        String versionId = now.format(VERSION_FMT);
        BackupVersion version = new BackupVersion(versionId, now);

        Path source = Path.of(job.getSourcePath());
        Path destRoot = Path.of(job.getDestPath());

        // Auto-create source if missing
        try {
            if (!Files.exists(source)) {
                Files.createDirectories(source);
                logger.info("created source " + source);
            }
            if (!Files.isDirectory(source)) {
                version.setStatus("FAILED");
                version.setMessage("SOURCE_NOT_DIRECTORY");
                logger.error("job = " + job.getName() + " SOURCE_NOT_DIRECTORY");
                return version;
            }
        } catch (IOException e) {
            version.setStatus("FAILED");
            version.setMessage("CANNOT_CREATE_SOURCE");
            logger.error("cannot create source " + source);
            return version;
        }

        // Auto-create destination if missing
        try {
            if (!Files.exists(destRoot)) {
                Files.createDirectories(destRoot);
                logger.info("created destination " + destRoot);
            }
            if (!Files.isDirectory(destRoot) || !Files.isWritable(destRoot)) {
                version.setStatus("FAILED");
                version.setMessage("DRIVE_OFFLINE");
                logger.error("job = " + job.getName() + " DRIVE_OFFLINE");
                return version;
            }
        } catch (IOException e) {
            version.setStatus("FAILED");
            version.setMessage("CANNOT_CREATE_DEST");
            logger.error("cannot create destination " + destRoot);
            return version;
        }

        Path versionDir = destRoot.resolve(safeName(job.getName())).resolve(versionId);
        try {
            Files.createDirectories(versionDir);
        } catch (IOException e) {
            version.setStatus("FAILED");
            version.setMessage("CANNOT_CREATE_VERSION");
            logger.error("cannot create " + versionDir);
            return version;
        }

        Path prevVersionDir = null;
        List<String> existing = listVersionIds(job);
        if (!existing.isEmpty()) {
            prevVersionDir = destRoot.resolve(safeName(job.getName())).resolve(existing.get(0));
        }

        int copied = 0;
        int linked = 0;
        long bytes = 0;
        boolean partial = false;

        List<Path> files = walker.walk(source);
        for (Path file : files) {
            try {
                Path rel = source.relativize(file);
                String relKey = rel.toString().replace('\\', '/');
                String hash = HashUtil.sha256(file);
                String old = store.getHash(job.getJobId(), relKey);

                Path target = versionDir.resolve(rel);
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }

                boolean didLink = false;
                if (hash.equals(old) && prevVersionDir != null) {
                    Path prevFile = prevVersionDir.resolve(rel);
                    if (Files.exists(prevFile)) {
                        try {
                            Files.createLink(target, prevFile);
                            didLink = true;
                            linked++;
                        } catch (Exception ignored) {
                            // fall back to copy
                        }
                    }
                }

                if (!didLink) {
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                    bytes += Files.size(file);
                }

                long size = Files.size(file);
                store.putHash(job.getJobId(), relKey, hash, size);
            } catch (Exception e) {
                partial = true;
                logger.error("file = " + file.getFileName() + " reason = " + e.getMessage());
            }
        }

        version.setFileCount(copied + linked);
        version.setFilesSkipped(0);
        version.setBytesCopied(bytes);
        version.setStatus(partial ? "PARTIAL" : "SUCCESS");
        version.setMessage(partial ? "some files failed" : "ok (linked=" + linked + ")");

        prune(job);
        job.markRan(now);
        store.save();
        logger.info("job = " + job.getName() + " copied = " + copied
                + " linked = " + linked + " bytes = " + bytes
                + " status = " + version.getStatus());
        return version;
    }

    public List<String> listVersionIds(BackupJob job) {
        Path jobDir = Path.of(job.getDestPath()).resolve(safeName(job.getName()));
        List<String> ids = new ArrayList<>();
        if (!Files.isDirectory(jobDir)) {
            return ids;
        }
        try (Stream<Path> stream = Files.list(jobDir)) {
            stream.filter(Files::isDirectory)
                    .map(p -> p.getFileName().toString())
                    .sorted(Comparator.reverseOrder())
                    .forEach(ids::add);
        } catch (IOException e) {
            logger.error("list versions failed: " + e.getMessage());
        }
        return ids;
    }

    public void prune(BackupJob job) {
        List<String> ids = listVersionIds(job);
        if (ids.size() <= job.getKeepVersions()) {
            return;
        }
        Path jobDir = Path.of(job.getDestPath()).resolve(safeName(job.getName()));
        for (int i = job.getKeepVersions(); i < ids.size(); i++) {
            deleteRecursive(jobDir.resolve(ids.get(i)));
            logger.info("pruned version " + ids.get(i));
        }
    }

    private void deleteRecursive(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) { }
            });
        } catch (IOException ignored) { }
    }

    private String safeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}