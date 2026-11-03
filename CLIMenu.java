
package group.pkg22_project;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class CLIMenu {
    private final Scanner in = new Scanner(System.in);
    private final AppFacade app;

    public CLIMenu() {
        this.app = new AppFacade();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== Data Backup Scheduler ===");
            System.out.println("1 List jobs");
            System.out.println("2 Add job");
            System.out.println("3 Backup now");
            System.out.println("4 List versions");
            System.out.println("5 Restore");
            System.out.println("6 Show log");
            System.out.println("7 Exit");
            System.out.println("8 Delete job");
            System.out.println("9 Enable job");
            System.out.println("10 Disable job");
            System.out.print("Choice: ");
            String choice = in.nextLine().trim();
            switch (choice) {
                case "1" -> listJobs();
                case "2" -> addJob();
                case "3" -> backupNow();
                case "4" -> listVersions();
                case "5" -> restoreJob();
                case "6" -> showLog();
                case "7" -> {
                    app.shutdown();
                    running = false;
                }
                case "8" -> deleteJob();
                case "9" -> setEnabled(true);
                case "10" -> setEnabled(false);
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private void listJobs() {
        List<BackupJob> jobs = app.listJobs();
        if (jobs.isEmpty()) {
            System.out.println("No jobs yet.");
            return;
        }
        for (BackupJob job : jobs) {
            System.out.println("ID:        " + job.getJobId());
            System.out.println("Name:      " + job.getName());
            System.out.println("Source:    " + job.getSourcePath());
            System.out.println("Dest:      " + job.getDestPath());
            System.out.println("Status:    " + job.getStatusText());
            System.out.println("Keep:      " + job.getKeepVersions() + " versions");
            System.out.println("----------------------------------------");
        }
    }

    private void addJob() {
        System.out.print("Name: ");
        String name = in.nextLine().trim();
        System.out.print("Source folder: ");
        String source = in.nextLine().trim();
        System.out.print("Destination folder: ");
        String dest = in.nextLine().trim();
        System.out.print("Interval minutes: ");
        int interval = Integer.parseInt(in.nextLine().trim());
        System.out.print("Keep versions: ");
        int keep = Integer.parseInt(in.nextLine().trim());
        BackupJob job = new BackupJob(
                UUID.randomUUID().toString().substring(0, 8),
                name, source, dest, interval, keep);
        app.addJob(job);
        System.out.println("Created job " + job.getJobId());
    }

    private void backupNow() {
        System.out.print("Job id: ");
        String id = in.nextLine().trim();
        BackupVersion result = app.backupNow(id);
        if (result == null) {
            System.out.println("Unknown job.");
        } else {
            System.out.println(result.getDetails());
        }
    }

    private void listVersions() {
        System.out.print("Job id: ");
        String id = in.nextLine().trim();
        System.out.println(app.listVersions(id));
    }

    private void restoreJob() {
        System.out.print("Job id: ");
        String id = in.nextLine().trim();
        System.out.print("Version id: ");
        String version = in.nextLine().trim();
        System.out.print("Target folder: ");
        String target = in.nextLine().trim();
        app.restore(id, version, Path.of(target));
    }

    private void showLog() {
        for (String line : app.getRecentLog()) {
            System.out.println(line);
        }
    }

    private void deleteJob() {
        System.out.print("Job id to delete: ");
        String id = in.nextLine().trim();
        System.out.print("Are you sure? (yes/no): ");
        String confirm = in.nextLine().trim().toLowerCase();
        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Delete cancelled.");
            return;
        }
        if (app.deleteJob(id)) {
            System.out.println("Deleted job " + id);
        } else {
            System.out.println("Unknown job.");
        }
    }

    private void setEnabled(boolean enabled) {
        System.out.print("Job id: ");
        String id = in.nextLine().trim();
        if (app.setJobEnabled(id, enabled)) {
            System.out.println((enabled ? "Enabled" : "Disabled") + " job " + id);
        } else {
            System.out.println("Unknown job.");
        }
    }
}