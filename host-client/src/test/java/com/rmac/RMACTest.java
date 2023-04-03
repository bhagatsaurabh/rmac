package com.rmac;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import com.rmac.comms.BridgeClient;
import com.rmac.comms.IPC;
import com.rmac.core.Archiver;
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.core.KeyRecorder;
import com.rmac.core.ScriptFiles;
import com.rmac.core.Service;
import com.rmac.process.CommandHandler;
import com.rmac.process.KernelDump;
import com.rmac.process.KeyLog;
import com.rmac.process.ScreenRecorder;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Commands;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;


@RunWith(MockitoJUnitRunner.class)
public class RMACTest {

  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;

  @Before
  public void setUpStreams() {
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void restoreStreams() {
    System.setErr(originalErr);
  }

  @Test
  @DisplayName("Validate runtime directory when no args given")
  public void validateRuntimeDirectory_NoArgs() {
    String[] args = new String[]{};

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime location not provided as an argument\r\n", errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when args are given but directory doesn't exist")
  public void validateRuntimeDirectory_Args_NoDirectory() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(false);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Provided runtime directory doesn't exist\r\n", errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when config file doesn't exist")
  public void validateRuntimeDirectory_Args_NoConfig() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(false);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime files or config missing in runtime directory\r\n",
        errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when ffmpeg runtime doesn't exist")
  public void validateRuntimeDirectory_Args_NoFFMPEG() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    String ffmpegLoc = args[0] + "\\ffmpeg.exe";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(false);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime files or config missing in runtime directory\r\n",
        errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when megacmd runtime doesn't exist")
  public void validateRuntimeDirectory_Args_NoMegaCMD() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    String ffmpegLoc = args[0] + "\\ffmpeg.exe";
    String megaLoc = args[0] + "\\megacmd";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(false);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime files or config missing in runtime directory\r\n",
        errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when java runtime doesn't exist")
  public void validateRuntimeDirectory_Args_NoJRE() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    String ffmpegLoc = args[0] + "\\ffmpeg.exe";
    String megaLoc = args[0] + "\\megacmd";
    String jreLoc = args[0] + "\\jre";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(false);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime files or config missing in runtime directory\r\n",
        errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when scripts dir creation fails")
  public void validateRuntimeDirectory_Args_ScriptsCreationFailed() throws IOException {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    String ffmpegLoc = args[0] + "\\ffmpeg.exe";
    String megaLoc = args[0] + "\\megacmd";
    String jreLoc = args[0] + "\\jre";
    String scriptsLoc = args[0] + "\\scripts";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(true);
    Mockito.doThrow(IOException.class).when(mockFs).createDirs(scriptsLoc);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Could not create scripts directory\r\n",
        errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when scripts dir creation succeeds")
  public void validateRuntimeDirectory_Args_ScriptsCreationSuccess() throws IOException {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    String ffmpegLoc = args[0] + "\\ffmpeg.exe";
    String megaLoc = args[0] + "\\megacmd";
    String jreLoc = args[0] + "\\jre";
    String scriptsLoc = args[0] + "\\scripts";
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(true);
    Mockito.doNothing().when(mockFs).createDirs(scriptsLoc);

    boolean result = new RMAC().validateRuntimeDirectory(args);

    assertTrue(result);
  }

  @Test
  @DisplayName("Add shutdown hook")
  public void addShutdownHook() {
    Runtime runtime = mock(Runtime.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

    new RMAC().addShutdownHook();
    verify(runtime).addShutdownHook(any());

    mockedRuntime.close();
  }

  @Test
  @DisplayName("Initiate shutdown when native hook unregister fails")
  public void initiateShutdown_UnregisterFailed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;

    CommandHandler commandHandlerMock = mock(CommandHandler.class);
    ScreenRecorder screenRecorderMock = mock(ScreenRecorder.class);
    KeyLog keyLogMock = mock(KeyLog.class);
    Archiver archiverMock = mock(Archiver.class);
    RMAC.commandHandler = commandHandlerMock;
    RMAC.screenRecorder = screenRecorderMock;
    RMAC.keyLog = keyLogMock;
    RMAC.archiver = archiverMock;

    Runtime runtime = mock(Runtime.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

    Appender<ILoggingEvent> appenderMock = mock(Appender.class);
    Logger mockedLogger = mock(Logger.class);
    MockedStatic<LoggerFactory> mockedLoggerFactory = mockStatic(LoggerFactory.class);
    mockedLoggerFactory.when(() -> LoggerFactory.getLogger(anyString())).thenReturn(mockedLogger);
    when(mockedLogger.getAppender(anyString())).thenReturn(appenderMock);

    FileLock fileLockMock = mock(FileLock.class);
    RandomAccessFile randomAccessFileMock = mock(RandomAccessFile.class);
    RMAC.fileLock = fileLockMock;
    RMAC.randomAccessFile = randomAccessFileMock;

    MockedStatic<GlobalScreen> mockedGlobalScreen = mockStatic(GlobalScreen.class);
    mockedGlobalScreen.when(GlobalScreen::unregisterNativeHook)
        .thenThrow(NativeHookException.class);

    RMAC.initiateShutdown();
    List<ILoggingEvent> logsList = listAppender.list;

    assertTrue(RMAC.SHUTDOWN);
    verify(commandHandlerMock).shutdown();
    verify(screenRecorderMock).shutdown();
    verify(keyLogMock).shutdown();
    mockedGlobalScreen.verify(() -> GlobalScreen.removeNativeKeyListener(any()));
    assertEquals("Could not unregister JNH", logsList.get(1).getMessage());
    verify(archiverMock).cleanUp();
    verify(runtime).exec(Commands.C_MEGACMD_KILL);
    verify(appenderMock).stop();
    verify(mockFs).move(eq(Constants.LOG_LOCATION), anyString(), any());
    verify(archiverMock).moveToArchive(anyString(), eq(ArchiveFileType.OTHER));
    verify(fileLockMock).release();
    verify(randomAccessFileMock).close();
    verify(mockFs).delete(eq(Constants.INSTANCE_LOCK_LOCATION));

    mockedGlobalScreen.close();
    mockedLoggerFactory.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Initiate shutdown when killing megacmd process, moving logfile and lock releasing fails")
  public void initiateShutdown_KillMegaCMD_MoveLog_LockRelease_AllFailed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;

    CommandHandler commandHandlerMock = mock(CommandHandler.class);
    ScreenRecorder screenRecorderMock = mock(ScreenRecorder.class);
    KeyLog keyLogMock = mock(KeyLog.class);
    Archiver archiverMock = mock(Archiver.class);
    RMAC.commandHandler = commandHandlerMock;
    RMAC.screenRecorder = screenRecorderMock;
    RMAC.keyLog = keyLogMock;
    RMAC.archiver = archiverMock;

    Runtime runtime = mock(Runtime.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    when(runtime.exec(eq(Commands.C_MEGACMD_KILL))).thenThrow(IOException.class);

    Appender<ILoggingEvent> appenderMock = mock(Appender.class);
    Logger mockedLogger = mock(Logger.class);
    MockedStatic<LoggerFactory> mockedLoggerFactory = mockStatic(LoggerFactory.class);
    mockedLoggerFactory.when(() -> LoggerFactory.getLogger(anyString())).thenReturn(mockedLogger);
    when(mockedLogger.getAppender(anyString())).thenReturn(appenderMock);

    doThrow(IOException.class).when(mockFs).move(eq(Constants.LOG_LOCATION), anyString(), any());

    FileLock fileLockMock = mock(FileLock.class);
    RandomAccessFile randomAccessFileMock = mock(RandomAccessFile.class);
    RMAC.fileLock = fileLockMock;
    RMAC.randomAccessFile = randomAccessFileMock;
    doThrow(IOException.class).when(fileLockMock).release();

    MockedStatic<GlobalScreen> mockedGlobalScreen = mockStatic(GlobalScreen.class);

    RMAC.initiateShutdown();
    List<ILoggingEvent> logsList = listAppender.list;

    assertTrue(RMAC.SHUTDOWN);
    verify(commandHandlerMock).shutdown();
    verify(screenRecorderMock).shutdown();
    verify(keyLogMock).shutdown();
    mockedGlobalScreen.verify(() -> GlobalScreen.removeNativeKeyListener(any()));
    verify(archiverMock).cleanUp();
    assertEquals("Could not kill megacmdserver process", logsList.get(2).getMessage());
    verify(appenderMock).stop();
    assertEquals("Couldn't remove lock file: " + Constants.INSTANCE_LOCK_LOCATION,
        logsList.get(3).getMessage());

    mockedGlobalScreen.close();
    mockedLoggerFactory.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Lock instance failed")
  public void lockInstance_Failed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    doThrow(IOException.class).when(mockFs).create("X:\\test\\test.lock");
    RMAC.fs = mockFs;

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;

    boolean result = new RMAC().lockInstance("X:\\test\\test.lock");
    List<ILoggingEvent> logsList = listAppender.list;

    assertFalse(result);
    assertEquals("Couldn't create/lock lock-file: X:\\test\\test.lock",
        logsList.get(0).getMessage());
  }

  @Test
  @DisplayName("Lock instance failed with null FileLock")
  public void lockInstance_Failed_NullFileLock() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    RandomAccessFile mockRAF = mock(RandomAccessFile.class);
    FileChannel mockFileChannel = mock(FileChannel.class);
    when(mockFs.createRandomAccessFile("X:\\test\\test.lock", "rw")).thenReturn(mockRAF);
    when(mockRAF.getChannel()).thenReturn(mockFileChannel);
    when(mockFileChannel.tryLock()).thenReturn(null);

    boolean result = new RMAC().lockInstance("X:\\test\\test.lock");

    assertFalse(result);
    verify(mockFileChannel).tryLock();
  }

  @Test
  @DisplayName("Lock instance success")
  public void lockInstance_Success() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;

    RandomAccessFile mockRAF = mock(RandomAccessFile.class);
    FileChannel mockFileChannel = mock(FileChannel.class);
    FileLock mockFileLock = mock(FileLock.class);
    when(mockFs.createRandomAccessFile("X:\\test\\test.lock", "rw")).thenReturn(mockRAF);
    when(mockRAF.getChannel()).thenReturn(mockFileChannel);
    when(mockFileChannel.tryLock()).thenReturn(mockFileLock);

    boolean result = new RMAC().lockInstance("X:\\test\\test.lock");

    verify(mockFileChannel).tryLock();
    assertEquals(mockFileLock, RMAC.randomAccessFile.getChannel().tryLock());
    assertTrue(result);
  }

  @Test
  @DisplayName("Copy RMAC native DLL when file doesn't exist")
  public void copyDLL_NoFile() {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.getResourceAsStream(any(), eq("/rmac-native.dll"))).thenReturn(null);

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;

    boolean result = new RMAC().copyDLL();
    List<ILoggingEvent> logsList = listAppender.list;

    verify(mockFs).getResourceAsStream(any(), eq("/rmac-native.dll"));
    assertEquals("Could not read RMAC dll from JAR", logsList.get(0).getMessage());
    assertFalse(result);
  }

  @Test
  @DisplayName("Copy RMAC native DLL fails")
  public void copyDLL_Failed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    InputStream mockIs = mock(InputStream.class);
    when(mockFs.getResourceAsStream(any(Class.class), eq("/rmac-native.dll"))).thenReturn(mockIs);
    doThrow(IOException.class).when(mockFs).copy(any(), any(), any());

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;

    boolean result = new RMAC().copyDLL();
    List<ILoggingEvent> logsList = listAppender.list;

    assertEquals(
        "Failed to copy RMAC dll from jar to Runtime, audio recording mode will be restricted to Active, ActiveAudioRecording=true config property will be ignored",
        logsList.get(0).getMessage());
    assertFalse(result);
  }

  @Test
  @DisplayName("Copy RMAC native DLL succeeds")
  public void copyDLL_Success() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    InputStream mockIs = mock(InputStream.class);
    when(mockFs.getResourceAsStream(any(Class.class), eq("/rmac-native.dll"))).thenReturn(mockIs);
    doNothing().when(mockFs).copy(any(), any(), any());

    boolean result = new RMAC().copyDLL();

    assertTrue(result);
  }

  @Test
  @DisplayName("Start method when validateRuntimeDirectory fails")
  public void start_ValidateRuntimeDirectory_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    int statusCode = catchSystemExit(() -> {
      RMAC rmac = spy(RMAC.class);
      when(rmac.validateRuntimeDirectory(args)).thenReturn(false);

      rmac.start(args);

      assertEquals("Runtime directory validation failed\r\n", errContent.toString());
    });

    assertEquals(0, statusCode);
  }

  @Test
  @DisplayName("Start method when set current location fails")
  public void start_SetCurrentLocation_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    RMAC rmac = spy(RMAC.class);
    when(rmac.validateRuntimeDirectory(args)).thenReturn(true);
    MockedStatic<Constants> constantsMocked = mockStatic(Constants.class);
    constantsMocked.when(() -> Constants.setRuntimeLocation(any()))
        .thenAnswer((Answer<Void>) invc -> null);
    constantsMocked.when(Constants::setCurrentLocation).thenReturn(false);

    int statusCode = catchSystemExit(() -> {
      rmac.start(args);

      assertEquals("Could not set current location\r\n", errContent.toString());
    });

    assertEquals(0, statusCode);

    constantsMocked.close();
  }

  @Test
  @DisplayName("Start method when acquiring instance lock fails")
  public void start_AcquireInstanceLock_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;
    MockedStatic<LoggerFactory> mockedLoggerFactory = mockStatic(LoggerFactory.class);
    mockedLoggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class)))
        .thenReturn(rmacLogger);

    RMAC rmac = spy(RMAC.class);
    doReturn(true).when(rmac).validateRuntimeDirectory(any());

    MockedStatic<Constants> constantsMocked = mockStatic(Constants.class);
    constantsMocked.when(() -> Constants.setRuntimeLocation(any()))
        .thenAnswer((Answer<Void>) invc -> null);
    constantsMocked.when(Constants::setCurrentLocation).thenReturn(true);

    int statusCode = catchSystemExit(() -> {
      rmac.start(args);
      List<ILoggingEvent> logsList = listAppender.list;

      assertEquals("Failed to acquire instance lock, another instance is running\r\n",
          logsList.get(0).getMessage());
    });

    assertEquals(0, statusCode);

    constantsMocked.close();
    mockedLoggerFactory.close();
  }

  @Test
  @DisplayName("Start method when loading config fails")
  public void start_ConfigLoad_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;
    MockedStatic<LoggerFactory> mockedLoggerFactory = mockStatic(LoggerFactory.class);
    mockedLoggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class)))
        .thenReturn(rmacLogger);

    RMAC rmac = spy(RMAC.class);
    doReturn(true).when(rmac).validateRuntimeDirectory(any());
    doReturn(true).when(rmac).lockInstance(any());
    Config mockConfig = mock(Config.class);
    doThrow(IOException.class).when(mockConfig).loadConfig();
    doReturn(mockConfig).when(rmac).getInstance(eq(Config.class));

    MockedStatic<Constants> constantsMocked = mockStatic(Constants.class);
    constantsMocked.when(() -> Constants.setRuntimeLocation(any()))
        .thenAnswer((Answer<Void>) invc -> null);
    constantsMocked.when(Constants::setCurrentLocation).thenReturn(true);

    int statusCode = catchSystemExit(() -> rmac.start(args));

    List<ILoggingEvent> logsList = listAppender.list;

    assertEquals(0, statusCode);
    assertEquals("Instance lock acquired", logsList.get(0).getMessage());
    assertEquals("Could not load config file", logsList.get(1).getMessage());

    constantsMocked.close();
    mockedLoggerFactory.close();
  }

  @Test
  @DisplayName("Start method succeeds")
  public void start_Success() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    Logger rmacLogger = (Logger) LoggerFactory.getLogger(RMAC.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    rmacLogger.addAppender(listAppender);
    RMAC.log = rmacLogger;
    MockedStatic<LoggerFactory> mockedLoggerFactory = mockStatic(LoggerFactory.class);
    mockedLoggerFactory.when(() -> LoggerFactory.getLogger(any(Class.class)))
        .thenReturn(rmacLogger);

    RMAC rmac = spy(RMAC.class);
    doReturn(true).when(rmac).validateRuntimeDirectory(any());
    doReturn(true).when(rmac).lockInstance(any());
    Config mockConfig = mock(Config.class);
    IPC mockSS = mock(IPC.class);
    FileUploader mockFU = mock(FileUploader.class);
    Archiver mockArchiver = mock(Archiver.class);
    ScriptFiles mockSF = mock(ScriptFiles.class);
    KeyLog mockKL = mock(KeyLog.class);
    KeyRecorder mockKR = mock(KeyRecorder.class);
    CommandHandler mockCH = mock(CommandHandler.class);
    ScreenRecorder mockSR = mock(ScreenRecorder.class);
    KernelDump mockKD = mock(KernelDump.class);
    BridgeClient mockBC = mock(BridgeClient.class);
    doNothing().when(mockConfig).loadConfig();
    doReturn(mockConfig)
        .doReturn(mockSS)
        .doReturn(mockFU)
        .doReturn(mockArchiver)
        .doReturn(mockSF)
        .doReturn(mockKL)
        .doReturn(mockKR)
        .doReturn(mockCH)
        .doReturn(mockSR)
        .doReturn(mockKD)
        .doReturn(mockBC)
        .when(rmac).getInstance(any());
    doReturn(true).when(rmac).copyDLL();
    doNothing().when(mockArchiver).uploadArchive();
    doNothing().when(rmac).addShutdownHook();

    MockedStatic<Constants> constantsMocked = mockStatic(Constants.class);
    constantsMocked.when(() -> Constants.setRuntimeLocation(any()))
        .thenAnswer((Answer<Void>) invc -> null);
    constantsMocked.when(Constants::setCurrentLocation).thenReturn(true);

    RMAC.service = mock(Service.class);
    rmac.start(args);
    List<ILoggingEvent> logsList = listAppender.list;

    assertEquals("Instance lock acquired", logsList.get(0).getMessage());
    assertEquals("RMAC client initialized successfully", logsList.get(1).getMessage());
    verify(rmac).copyDLL();
    assertTrue(RMAC.NATIVE_POSSIBLE);
    verify(rmac).addShutdownHook();

    constantsMocked.close();
    mockedLoggerFactory.close();
  }

  @Test
  @DisplayName("Get instance")
  public void getInstance()
      throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    String testObj = (String) (new RMAC().getInstance(String.class));
    assertEquals(testObj.getClass(), String.class);
  }
}
