
package group.pkg22_project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class BackupGUI {
    private final AppFacade app;
    private final JTextArea output = new JTextArea(20, 70);

    public BackupGUI() {
        this.app = new AppFacade();
        output.setEditable(false);
        output.setFont(new Font("Consolas", Font.PLAIN, 13));
        output.setBackground(new Color(250, 250, 250));
        output.setBorder(new EmptyBorder(8, 8, 8, 8));
    }

    public void showWindow() {
        JFrame frame = new JFrame("Data Backup Scheduler - Group 22");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(new Color(245, 245, 245));

        // Title
        JLabel title = new JLabel("Data Backup Scheduler", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(12, 0, 8, 0));
        title.setForeground(new Color(40, 40, 40));

        // Button panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        buttons.setBackground(new Color(245, 245, 245));
        buttons.setBorder(new EmptyBorder(4, 8, 8, 8));

        JButton listBtn = createButton("List jobs");
        JButton addBtn = createButton("Add job");
        JButton backupBtn = createButton("Backup now");
        JButton versionsBtn = createButton("List versions");
        JButton restoreBtn = createButton("Restore");
        JButton logBtn = createButton("Show log");
        JButton deleteBtn = createButton("Delete job");
        JButton enableBtn = createButton("Enable");
        JButton disableBtn = createButton("Disable");

        buttons.add(listBtn);
        buttons.add(addBtn);
        buttons.add(backupBtn);
        buttons.add(versionsBtn);
        buttons.add(restoreBtn);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(logBtn);
        buttons.add(deleteBtn);
        buttons.add(enableBtn);
        buttons.add(disableBtn);

        listBtn.addActionListener(e -> listJobs());
        addBtn.addActionListener(e -> addJob());
        backupBtn.addActionListener(e -> backupNow());
        versionsBtn.addActionListener(e -> listVersions());
        restoreBtn.addActionListener(e -> restoreJob());
        logBtn.addActionListener(e -> showLog());
        deleteBtn.addActionListener(e -> deleteJob());
        enableBtn.addActionListener(e -> setEnabled(true));
        disableBtn.addActionListener(e -> setEnabled(false));

        // Output area
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(0, 12, 12, 12),
                BorderFactory.createLineBorder(new Color(200, 200, 200))));

        frame.add(title, BorderLayout.NORTH);
        frame.add(buttons, BorderLayout.CENTER);
        frame.add(scroll, BorderLayout.SOUTH);

        frame.setMinimumSize(new Dimension(820, 520));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                app.shutdown();
            }
        });
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 32));
        return btn;
    }

    private void listJobs() {
        output.setText("");
        List<BackupJob> jobs = app.listJobs();
        if (jobs.isEmpty()) {
            output.append("No jobs yet.\n");
            return;
        }
        for (BackupJob job : jobs) {
            output.append("ID:        " + job.getJobId() + "\n");
            output.append("Name:      " + job.getName() + "\n");
            output.append("Source:    " + job.getSourcePath() + "\n");
            output.append("Dest:      " + job.getDestPath() + "\n");
            output.append("Status:    " + job.getStatusText() + "\n");
            output.append("Keep:      " + job.getKeepVersions() + " versions\n");
            output.append("----------------------------------------\n");
        }
    }

    private void addJob() {
        String name = JOptionPane.showInputDialog(null, "Job name:");
        if (name == null || name.isBlank()) return;
        String source = JOptionPane.showInputDialog(null, "Source folder:", "source");
        String dest = JOptionPane.showInputDialog(null, "Destination folder:", "backup");
        String intervalText = JOptionPane.showInputDialog(null, "Interval minutes:", "15");
        String keepText = JOptionPane.showInputDialog(null, "Keep versions:", "3");
        try {
            int interval = Integer.parseInt(intervalText.trim());
            int keep = Integer.parseInt(keepText.trim());
            BackupJob job = new BackupJob(
                    UUID.randomUUID().toString().substring(0, 8),
                    name.trim(), source.trim(), dest.trim(), interval, keep);
            app.addJob(job);
            listJobs();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Could not add job: " + ex.getMessage());
        }
    }

    private void backupNow() {
        String id = JOptionPane.showInputDialog(null, "Job id to back up:");
        if (id == null) return;
        BackupVersion result = app.backupNow(id.trim());
        if (result == null) {
            JOptionPane.showMessageDialog(null, "Unknown job.");
        } else {
            output.append(result.getDetails() + "\n");
        }
    }

    private void listVersions() {
        String id = JOptionPane.showInputDialog(null, "Job id:");
        if (id == null) return;
        output.append("Versions: " + app.listVersions(id.trim()) + "\n");
    }

    private void restoreJob() {
        String id = JOptionPane.showInputDialog(null, "Job id:");
        if (id == null) return;
        String version = JOptionPane.showInputDialog(null, "Version id:");
        String target = JOptionPane.showInputDialog(null, "Target folder:", "restore");
        if (version == null || target == null) return;
        app.restore(id.trim(), version.trim(), Path.of(target.trim()));
        output.append("Restore requested for " + version + "\n");
        showLog();
    }

    private void showLog() {
        output.setText("");
        for (String line : app.getRecentLog()) {
            output.append(line + "\n");
        }
    }

    private void deleteJob() {
        String id = JOptionPane.showInputDialog(null, "Job id to delete:");
        if (id == null || id.isBlank()) return;

        int choice = JOptionPane.showConfirmDialog(
                null,
                "Delete job " + id + "?\nThis cannot be undone.",
                "Confirm delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        if (app.deleteJob(id.trim())) {
            output.append("Deleted job " + id + "\n");
            listJobs();
        } else {
            JOptionPane.showMessageDialog(null, "Unknown job.");
        }
    }

    private void setEnabled(boolean enabled) {
        String id = JOptionPane.showInputDialog(null, "Job id:");
        if (id == null) return;
        if (app.setJobEnabled(id.trim(), enabled)) {
            output.append((enabled ? "Enabled" : "Disabled") + " job " + id + "\n");
            listJobs();
        } else {
            JOptionPane.showMessageDialog(null, "Unknown job.");
        }
    }
}