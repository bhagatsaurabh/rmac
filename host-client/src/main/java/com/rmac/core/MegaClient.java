package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for running MEGA cli client commands.
 */
@Slf4j
public class MegaClient {

  private static boolean SERVER_STARTED = false;
  public static boolean LOGGED_IN = false;

  /**
   * Execute MEGA cli client command as a separate process.
   *
   * @param command command to run.
   * @return Reference to the MEGA command.
   */
  public MegaCommand executeCommand(String... command) {
    if (!Connectivity.checkNetworkState()) {
      log.error("Network is down, ignoring megacmd command");
      return null;
    }

    try {
      return new MegaCommand(command);
    } catch (IOException e) {
      log.error("Could not execute MEGA command", e);
    }

    return null;
  }

  /**
   * Start the MEGA cli server process.
   *
   * @return Result (true = success | false = failed)
   */
  public boolean startServer() {
    try {
      File megaServer = new File(Constants.MEGASERVER_LOCATION);
      if (!megaServer.exists()) {
        log.error("MEGAcmd server not found");
        return false;
      }
      ProcessBuilder builder = new ProcessBuilder(Constants.MEGASERVER_LOCATION);
      builder.start();
      Thread.sleep(3000);
      SERVER_STARTED = this.isServerRunning();
      return SERVER_STARTED;
    } catch (IOException e) {
      log.error("Could not start MEGAcmd server", e);
    } catch (InterruptedException e) {
      log.error("Could not wait for MEGAcmd server to start", e);
    }

    return false;
  }

  /**
   * Login to the associated MEGA account using user/pass, optionally start the server if not
   * already started.
   *
   * @param user        Username of the MEGA account to login into.
   * @param pass        Password for the username
   * @param startServer Whether to start the MEGA cli server process.
   */
  public void login(String user, String pass, boolean startServer) {
    if (!SERVER_STARTED && startServer) {
      RMAC.mega.startServer();
    }
    if (!SERVER_STARTED) {
      log.error("Server is not running, cannot login to MEGA");
      return;
    }
    if (RMAC.mega.isLoggedIn()) {
      LOGGED_IN = true;
      return;
    }

    MegaCommand command = RMAC.mega.executeCommand("login", "\"" + user + "\"",
        "\"" + pass + "\"");

    if (Objects.isNull(command)) {
      LOGGED_IN = false;
      return;
    }

    try {
      if (!command.process.waitFor(25, TimeUnit.SECONDS)) {
        LOGGED_IN = false;
        command.stop();
      }
    } catch (InterruptedException e) {
      log.error("Mega client process interrupted", e);
      LOGGED_IN = false;
      return;
    }

    LOGGED_IN = !command.isAPIError.get();
  }

  /**
   * Create a new MEGA cli client command and upload the given file to specified destination path.
   *
   * @param filePath Path to the file to upload
   * @param destPath Destination path where the file will be stored after upload.
   * @return Result (true = success | false = failed)
   */
  public boolean uploadFile(String filePath, String destPath) {
    if (!LOGGED_IN) {
      RMAC.mega.login(RMAC.config.getMegaUser(), RMAC.config.getMegaPass(), true);
    }
    if (!LOGGED_IN) {
      log.error("Mega client not logged in, ignoring upload");
      return false;
    }

    MegaCommand command = RMAC.mega.executeCommand(
        "put", "-c", "\"" + filePath + "\"", "\"" + destPath + "\""
    );

    if (Objects.isNull(command)) {
      return false;
    }

    try {
      command.process.waitFor();
    } catch (InterruptedException e) {
      log.error("Mega client process interrupted", e);
      return false;
    }

    return !command.isAPIError.get();
  }

  /**
   * Check if MEGA cli server process is running or not.
   *
   * @return Result (true = running | false = not-running)
   */
  public boolean isServerRunning() {
    try {
      ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c",
          "tasklist | find /i \"MEGAcmdServer.exe\" && echo Running || echo Not Running");
      Process proc = builder.start();
      BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      BufferedWriter in = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
      PipeStream err = new PipeStream(proc.getErrorStream(), new NoopOutputStream());
      err.start();
      StringBuilder result = new StringBuilder();
      String curr;
      while ((curr = out.readLine()) != null) {
        result.append(curr.trim());
      }
      proc.waitFor();
      in.close();
      out.close();

      return result.toString().contains("Running");
    } catch (IOException e) {
      log.error("Could not check if MEGAcmdServer is running", e);
    } catch (InterruptedException e) {
      log.error("MEGAcmdServer process check interrupted", e);
    }

    return false;
  }

  /**
   * Check if MEGA cli server has an active session.
   *
   * @return Result (true = logged-in | false = not logged-in)
   */
  public boolean isLoggedIn() {
    MegaCommand command = RMAC.mega.executeCommand("whoami");
    if (Objects.isNull(command)) {
      return false;
    }

    try {
      if (!command.process.waitFor(10, TimeUnit.SECONDS)) {
        command.stop();
        return false;
      }
    } catch (InterruptedException e) {
      log.error("Mega client process interrupted", e);
      return false;
    }

    return !command.isAPIError.get();
  }
}
