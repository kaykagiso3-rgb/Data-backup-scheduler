
package group.pkg22_project;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class FileWalker {

    public List<Path> walk(Path root) {
        List<Path> out = new ArrayList<>();
        if (root == null || !Files.isDirectory(root)) {
            return out;
        }
        walkRecursive(root, out);
        return out;
    }

    private void walkRecursive(Path dir, List<Path> out) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                if (Files.isDirectory(child)) {
                    walkRecursive(child, out);
                } else if (Files.isRegularFile(child)) {
                    out.add(child);
                }
            }
        } catch (IOException ignored) {
        }
    }
}