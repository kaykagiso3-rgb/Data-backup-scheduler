
package group.pkg22_project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LogService {
    private final Path logFile = Path.of("data", "backup.log");
    private final LinkedList<String> recent = new LinkedList<>();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogService() {
        try {
            Files.createDirectories(logFile.getParent());
            if (!Files.exists(logFile)) {
                Files.createFile(logFile);
            }
        } catch (IOException e) {
            System.err.println("Could not create log file: " + e.getMessage());
        }
    }

    public void info(String msg) {
        write("INFO", msg);
    }

    public void error(String msg) {
        write("ERROR", msg);
    }

    public List<String> getRecent() {
        return new ArrayList<>(recent);
    }

    private void write(String level, String msg) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] " + level + "  " + msg;
        recent.addLast(line);
        while (recent.size() > 50) {
            recent.removeFirst();
        }
        try {
            Files.writeString(logFile, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(line);
        }
        System.out.println(line);
    }
}