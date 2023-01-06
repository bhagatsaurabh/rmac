package com.rmac.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry/exit point for RMACUpdater.
 * <br>
 * Whenever this process starts, it connects with the currently active RMAC client process running
 * on this host machine and checks for updates by calling RMAC Server api, if there are no updates,
 * this process starts monitoring RMAC client's health.
 * <br><br>
 * Performs following tasks in sequence:
 * <br>
 * <ol>
 *   <li>Try to connect to RMAC client</li>
 *   <li>Verify if the workspace is clear (update location exists and lock file doesn't exist, if lockfile exists, it indicates a failure in previous update attempt, clear any leftover files)</li>
 *   <li>Read current RMAC client's version from its jar.</li>
 *   <li>Make an API call to RMAC Server to fetch the latest update's download link with checksum.</li>
 *   <li>Download the new RMAC client jar and store in update location.</li>
 *   <li>Create a lock file.</li>
 *   <li>Stop the currently running RMAC client process by sending an 'exit signal' through local socket connection, check if RMAC client process is still running, if still running then it means RMAC client was not able to shutdown on its own, try to force-stop.</li>
 *   <li>Replace the old RMAC client jar with new one stored previously in update location.</li>
 *   <li>Start the new RMAC client process.</li>
 *   <li>Delete the lock file.</li>
 *   <li>Start monitoring the running RMAC client's health</li>
 * </ol>
 */
@Slf4j
public class Updater {

  public static String version;
  public static SocketClient client;
  public static Monitor monitor;
  public static int MAX_RETRIES_UPDATE_DOWNLOAD = 5;
  public static int ATTEMPT = 0;
  public static int COOLDOWN = 2000;
  public static int DELAYED_START = 3000;
  public static String SERVER_URL = "";
  public static int HEALTH_CHECK_INTERVAL = 3000;
  public static boolean SHUTDOWN = false;
  public static int CLIENT_STOP_WAIT = 5000;
  public static int CLIENT_START_WAIT = 3000;

  public static FileLock fileLock;
  public static RandomAccessFile randomAccessFile;

  public static FileSystem fs = new FileSystem();
  public static Service service = new Service();

  public static void main(String[] args)
      throws InstantiationException, IllegalAccessException, IOException {
    new Updater().start(args);
  }

  public void start(String[] args)
      throws InstantiationException, IllegalAccessException, IOException {
    if (args.length < 1) {
      log.error("Runtime location not provided as an argument");
      System.exit(0);
    }

    // Delayed Start
    try {
      synchronized (this) {
        this.wait(DELAYED_START);
      }
    } catch (InterruptedException | IllegalArgumentException e) {
      log.error("Could not delay start", e);
    }

    Constants.setRuntimeLocation(args[0]);
    if (!Constants.setCurrentLocation()) {
      System.exit(0);
    }

    // Try acquiring a lock for this instance, if it fails then probably another instance is running
    if (!this.lockInstance(Constants.INSTANCE_LOCK_LOCATION)) {
      log.error("Failed to acquire instance lock");
      System.exit(0);
    } else {
      log.info("Instance lock acquired");
    }

    this.loadConfig(args[0]);

    client = (SocketClient) this.getInstance(SocketClient.class);
    client.start();

    Updater.fs.createDirs(
        Constants.UPDATE_LOCATION.substring(0, Constants.UPDATE_LOCATION.length() - 1));

    this.verifyWorkspace();
    this.readVersion();
    boolean isUpdateProcessed = this.startUpdate();
    if (isUpdateProcessed) {
      // If RMAC updater attempted to update RMAC client, that means the old socket connection might be severed
      // (might not be, if the update started but failed to stop the old RMAC client),
      // create a new socket connection nonetheless.
      try {
        // Sending Exit signal might fail if the connection with old RMAC client is already closed.
        client.shutdown();
      } catch (Exception e) {
        log.warn("Could not send 'Exit' to old RMAC client");
      }
      client = (SocketClient) this.getInstance(SocketClient.class);
      client.start();
    }
    log.info("RMACClient health monitoring started");
    monitor = (Monitor) this.getInstance(Monitor.class);
    monitor.updater = this;
    monitor.start();

    this.addShutdownHook();
  }

  /**
   * Read and load config file properties.
   *
   * @param configPath Path to the Runtime Location which consists RMAC config file.
   */
  public void loadConfig(String configPath) {
    try {
      BufferedReader configReader = fs.getReader(configPath + "\\config.rmac");
      String curr;
      while ((curr = configReader.readLine()) != null) {
        if (curr.contains("ServerUrl")) {
          String[] pair = curr.trim().split("=");
          SERVER_URL = pair[1].trim();
        } else if (curr.contains("ClientHealthCheckInterval")) {
          String[] pair = curr.trim().split("=");
          HEALTH_CHECK_INTERVAL = Integer.parseInt(pair[1].trim());
        }
      }
      configReader.close();
    } catch (IOException e) {
      log.error("Could not read config file");
      System.exit(0);
    }
  }

  /**
   * Check for any updates and start the update process.
   */
  public boolean startUpdate() {
    if (this.checkForUpdates()) {
      boolean result =
          this.createUpdateLock()
              && this.stopRMAC()
              && this.update()
              && this.startRMAC()
              && this.deleteUpdateLock();
      if (!result) {
        log.error("Update Failed");
      }
      return true;
    }
    return false;
  }

  /**
   * Read the version of RMAC client jar from its MANIFEST.MF file.
   */
  public void readVersion() {
    try (JarFile jarFile = fs.getJarFile(Constants.RMAC_LOCATION)) {
      Manifest manifest = jarFile.getManifest();
      version = manifest.getMainAttributes().getValue("Version");
    } catch (IOException e) {
      log.error("Couldn't read version", e);
    } finally {
      if (Objects.isNull(version)) {
        version = "Unknown";
      }
    }
    log.info("RMAC Version: " + version);
  }

  /**
   * Call the RMAC Server api and get the latest update's download url.
   *
   * @return Whether there is any update available (true=new-update | false=no-update).
   */
  public boolean checkForUpdates() {
    String downloadUrl, checksum;
    String[] data = Updater.service.getUpdate(version);
    if (Objects.nonNull(data) && data.length != 0) {
      downloadUrl = data[0];
      checksum = data[1];
    } else {
      log.info("No update");
      return false;
    }

    String updateFile = Constants.UPDATE_LOCATION + "RMACClient.jar";
    if (Updater.fs.exists(updateFile)) {
      try {
        boolean result = Checksum.verifyChecksum(updateFile, checksum);
        if (!result) {
          Updater.fs.delete(updateFile);
        } else {
          return true;
        }
      } catch (NoSuchAlgorithmException | IOException e) {
        try {
          Updater.fs.delete(updateFile);
        } catch (IOException ioe) {
          log.error("Could not delete update file", ioe);
        }
        log.error("Cannot verify update checksum", e);
      }
    }

    return this.downloadUpdate(downloadUrl, checksum);
  }

  /**
   * Try downloading the new RMAC client jar.
   *
   * @param signedUrl Temporary download url
   * @return Whether download was successful (true=succeeded | false=failed).
   */
  public boolean downloadUpdate(String signedUrl, String checksum) {
    for (int i = 0; i <= MAX_RETRIES_UPDATE_DOWNLOAD; i++) {
      try {
        this.attemptDownload(signedUrl, checksum);
        break;
      } catch (Exception e) {
        log.error("Could not download update", e);
        if (i == MAX_RETRIES_UPDATE_DOWNLOAD) {
          log.warn(
              "Max retries exceeded, update can only be done on next restart or manually via HostCommand");
          return false;
        }
        try {
          synchronized (this) {
            this.wait(COOLDOWN);
          }
        } catch (InterruptedException | IllegalArgumentException ex) {
          log.error("Could not wait for cooldown", e);
        }
      }
    }
    return true;
  }

  /**
   * Attempt to download the new RMAC client jar.
   *
   * @param signedUrl Temporary download url.
   * @throws IOException If download fails.
   */
  public void attemptDownload(String signedUrl, String checksum) throws Exception {
    ATTEMPT += 1;
    ReadableByteChannel readableByteChannel = Channels.newChannel(this.getStream(signedUrl));
    FileOutputStream fileOutputStream = fs.getFOS(Constants.UPDATE_LOCATION + "RMACClient.jar");
    FileChannel fileChannel = fileOutputStream.getChannel();
    fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    fileChannel.close();
    fileOutputStream.close();
    readableByteChannel.close();

    // Verify checksum
    if (!Checksum.verifyChecksum(Constants.UPDATE_LOCATION + "RMACClient.jar", checksum)) {
      throw new Exception("Checksum mismatch");
    }
    ATTEMPT = 0;
  }

  /**
   * Create a temporary lock file to indicate an update is underway.
   *
   * @return Whether lock file was successfully created (true=succeeded | false=failed).
   */
  public boolean createUpdateLock() {
    try {
      fs.create(Constants.UPDATE_LOCK_LOCATION);
    } catch (IOException e) {
      log.error("Could not create update lockfile", e);
      return false;
    }
    log.info("Update lockfile created");
    return true;
  }

  /**
   * Delete the lock file.
   *
   * @return Whether lock file deletion was successful (true=success).
   */
  public boolean deleteUpdateLock() {
    try {
      fs.delete(Constants.UPDATE_LOCK_LOCATION);
    } catch (IOException e) {
      log.error("Could not delete update lockfile", e);
      return false;
    }
    log.info("Update lockfile deleted");
    return true;
  }

  /**
   * Stop currently running RMAC client process.
   *
   * @return Whether attempting to stop RMAC client process was successful (true=success).
   */
  public boolean stopRMAC() {
    if (Objects.nonNull(client)) {
      client.sendMessage("Stop");
      try {
        synchronized (this) {
          this.wait(CLIENT_STOP_WAIT);
        }
      } catch (InterruptedException | IllegalArgumentException e) {
        log.error("Could not wait for RMAC client to stop on its own", e);
      }
    }
    try {
      ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-enc",
          Commands.C_RMAC_CLIENT_PID);
      Process proc = this.startProcess(builder);
      BufferedReader out = fs.getReader(proc.getInputStream());
      BufferedWriter in = fs.getWriter(proc.getOutputStream());
      PipeStream err = PipeStream.make(proc.getErrorStream(), new NoopOutputStream());
      err.start();
      StringBuilder info = new StringBuilder();
      String curr;
      while ((curr = out.readLine()) != null) {
        info.append(curr.trim());
      }

      proc.waitFor();
      in.close();
      out.close();

      if (!info.toString().trim().equals("")) {
        int pid = Integer.parseInt(info.toString());
        log.info("RMACClient PID: " + pid);
        Process killProc = Runtime.getRuntime().exec("taskkill /pid " + pid);
        killProc.waitFor();
      }
    } catch (IOException | NumberFormatException e) {
      log.error("Could not get PID", e);
    } catch (InterruptedException e) {
      log.error("Could not wait for taskkill to complete", e);
    }
    log.info("Assuming RMAC is stopped");
    return true;
  }

  /**
   * Replace the old RMAC client jar file with the new downloaded update.
   *
   * @return Whether this file replace was successful (true=success).
   */
  public boolean update() {
    try {
      fs.delete(Constants.RMAC_LOCATION);
    } catch (IOException e) {
      log.error("Could not delete old client jar");
    }
    try {
      fs.copy(Constants.RMAC_LOCATION, Constants.UPDATE_LOCATION + "RMACClient.jar",
          StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      log.error("Could not copy new update to destination", e);
      return false;
    }
    try {
      fs.delete(Constants.UPDATE_LOCATION + "RMACClient.jar");
    } catch (IOException e) {
      log.error("Could not delete staged client jar");
    }
    log.info("Update complete");
    return true;
  }

  /**
   * Start the newly updated RMAC client jar.
   *
   * @return Whether this attempt to start RMAC was successful (true=success).
   */
  public boolean startRMAC() {
    try {
      Runtime.getRuntime().exec("cmd.exe /c \"" + Constants.START_RMAC_LOCATION + "\"");
      synchronized (this) {
        this.wait(CLIENT_START_WAIT);
      }
    } catch (IOException | InterruptedException | IllegalArgumentException e) {
      log.error("Could not start RMAC", e);
      return false;
    }
    log.info("RMAC re-started");
    return true;
  }

  /**
   * Verify if update location exists and there is no lock file.
   * <br>
   * <br>
   * <cite>
   * If lock file exists, it indicates a previously failed attempt to update RMAC client, delete any
   * leftover files from previous failed attempt.
   * </cite>
   */
  public void verifyWorkspace() {
    if (fs.exists(Constants.UPDATE_LOCK_LOCATION)) {
      log.info("Lockfile found, cleaning failed update");

      try {
        fs.delete(Constants.UPDATE_LOCATION + "RMACClient.jar");
        fs.delete(Constants.UPDATE_LOCK_LOCATION);
      } catch (IOException e) {
        log.error("Workspace verification failed");
      }
    }
    log.info("Workspace verified");
  }

  /**
   * Register the JVM shutdown hook.
   */
  public void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (!SHUTDOWN) {
        try {
          shutdown();
        } catch (Exception e) {
          log.error("Client connection shutdown failed");
        }
      }
    }));
  }

  /**
   * Close the socket client and health monitoring process.
   */
  public void shutdown() throws Exception {
    log.warn("Shutting down...");
    if (Objects.nonNull(client)) {
      client.shutdown();
    }
    if (Objects.nonNull(monitor)) {
      monitor.shutdown();
    }

    try {
      Updater.fileLock.release();
      Updater.randomAccessFile.close();
      fs.delete(Constants.INSTANCE_LOCK_LOCATION);
      log.info("Instance lock released");
    } catch (Exception e) {
      log.error("Couldn't remove lock file: " + Constants.INSTANCE_LOCK_LOCATION, e);
    }
  }

  /**
   * Try acquiring a lock to the given file.
   *
   * @param lockFile File to acquire a lock on.
   * @return result (true = success | false = failed)
   */
  public boolean lockInstance(final String lockFile) {
    try {
      Updater.randomAccessFile = fs.createRandomAccessFile(lockFile, "rw");
      Updater.fileLock = Updater.randomAccessFile.getChannel().tryLock();
      if (Objects.nonNull(Updater.fileLock)) {
        return true;
      }
    } catch (Exception e) {
      log.error("Couldn't create/lock lock-file: " + lockFile, e);
    }
    return false;
  }

  public InputStream getStream(String url) throws IOException {
    return new URL(url).openStream();
  }

  public Process startProcess(ProcessBuilder builder) throws IOException {
    return builder.start();
  }

  public Object getInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
    return clazz.newInstance();
  }
}
