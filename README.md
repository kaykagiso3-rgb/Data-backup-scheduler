# Data Backup Scheduler

A Java application for scheduling local file backups with versioning and restore support.  
Developed by Group 22 for SDN260S – Software Design II (Cape Peninsula University of Technology).

## Features

- Create backup jobs with source, destination, interval, and retention settings
- Manual and scheduled backups
- Incremental backups using SHA-256 hashing
- Full version snapshots (hard links for unchanged files)
- List versions and restore any previous version
- Enable / Disable jobs
- Delete jobs with confirmation
- Activity logging
- Both CLI and Swing GUI interfaces
- Automatic creation of missing source/destination folders

## Requirements

- Java 17 or higher
- NetBeans (recommended) or any IDE that supports Java

## How to Run

1. Clone or download the repository
2. Open the project in NetBeans
3. Run `Group22_Project.java` (or the main class)

### CLI
A text menu will appear in the console. Use the numbered options to manage jobs.

### GUI
A Swing window will open with buttons for all major functions.

## Project Structure
