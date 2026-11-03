
package group.pkg22_project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;


public class RestoreService {
    private final LogService logger;
    private final BackupEngine engine;

    public RestoreService(LogService logger, BackupEngine engine) {
        this.logger = logger;
        this.engine = engine;
    }

    public List<String> listVersionIds(BackupJob job) {
        return engine.listVersionIds(job);
    }

    public void restore(BackupJob job, String versionId, Path targetDir) {
        String safe = job.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        Path versionDir = Path.of(job.getDestPath()).resolve(safe).resolve(versionId);

        if (!Files.isDirectory(versionDir)) {
            logger.error("restore failed: version not found " + versionId);
            return;
        }
        try {
            Files.createDirectories(targetDir);
            try (Stream<Path> walk = Files.walk(versionDir)) {
                walk.filter(Files::isRegularFile).forEach(src -> {
                    try {
                        Path dest = targetDir.resolve(versionDir.relativize(src));
                        if (dest.getParent() != null) {
                            Files.createDirectories(dest.getParent());
                        }
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("restore file failed " + src.getFileName());
                    }
                });
            }
            logger.info("restored job=" + job.getName()
                    + " version=" + versionId + " -> " + targetDir);
        } catch (IOException e) {
            logger.error("restore failed: " + e.getMessage());
        }
    }
}