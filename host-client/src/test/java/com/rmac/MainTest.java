package com.rmac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import com.rmac.core.Archiver;
import com.rmac.core.CommandHandler;
import com.rmac.core.KeyLog;
import com.rmac.core.ScreenRecorder;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Commands;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
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
import org.slf4j.LoggerFactory;


@RunWith(MockitoJUnitRunner.class)
public class MainTest {

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

    boolean result = Main.validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Runtime location not provided as an argument\r\n", errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when args are given but directory doesn't exist")
  public void validateRuntimeDirectory_Args_NoDirectory() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    FileSystem mockFs = mock(FileSystem.class);
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(false);

    boolean result = Main.validateRuntimeDirectory(args);

    Assert.assertFalse(result);
    assertEquals("Provided runtime directory doesn't exist\r\n", errContent.toString());
  }

  @Test
  @DisplayName("Validate runtime directory when config file doesn't exist")
  public void validateRuntimeDirectory_Args_NoConfig() {
    String[] args = new String[]{"X:\\Test\\RMAC"};
    String configLoc = args[0] + "\\config.rmac";
    FileSystem mockFs = mock(FileSystem.class);
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(false);

    boolean result = Main.validateRuntimeDirectory(args);

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
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(false);

    boolean result = Main.validateRuntimeDirectory(args);

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
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(false);

    boolean result = Main.validateRuntimeDirectory(args);

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
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(false);

    boolean result = Main.validateRuntimeDirectory(args);

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
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(true);
    Mockito.doThrow(IOException.class).when(mockFs).createDirs(scriptsLoc);

    boolean result = Main.validateRuntimeDirectory(args);

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
    Main.fs = mockFs;

    Mockito.when(mockFs.exists(args[0])).thenReturn(true);
    Mockito.when(mockFs.exists(configLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(ffmpegLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(megaLoc)).thenReturn(true);
    Mockito.when(mockFs.exists(jreLoc)).thenReturn(true);
    Mockito.doNothing().when(mockFs).createDirs(scriptsLoc);

    boolean result = Main.validateRuntimeDirectory(args);

    assertTrue(result);
  }

  @Test
  @DisplayName("Add shutdown hook")
  public void addShutdownHook() {
    Runtime runtime = mock(Runtime.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

    Main.addShutdownHook();
    verify(runtime).addShutdownHook(any());
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Initiate shutdown when native hook unregister fails")
  public void initiateShutdown_UnregisterFailed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    Main.fs = mockFs;

    Logger mainLogger = (Logger) LoggerFactory.getLogger(Main.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    mainLogger.addAppender(listAppender);
    Main.log = mainLogger;

    CommandHandler commandHandlerMock = mock(CommandHandler.class);
    ScreenRecorder screenRecorderMock = mock(ScreenRecorder.class);
    KeyLog keyLogMock = mock(KeyLog.class);
    Archiver archiverMock = mock(Archiver.class);
    Main.commandHandler = commandHandlerMock;
    Main.screenRecorder = screenRecorderMock;
    Main.keyLog = keyLogMock;
    Main.archiver = archiverMock;

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
    File lockFileMock = mock(File.class);
    Main.fileLock = fileLockMock;
    Main.randomAccessFile = randomAccessFileMock;
    Main.lockFile = lockFileMock;

    MockedStatic<GlobalScreen> mockedGlobalScreen = mockStatic(GlobalScreen.class);
    mockedGlobalScreen.when(GlobalScreen::unregisterNativeHook)
        .thenThrow(NativeHookException.class);

    Main.initiateShutdown();
    List<ILoggingEvent> logsList = listAppender.list;

    assertTrue(Main.SHUTDOWN);
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
    verify(lockFileMock).delete();

    mockedGlobalScreen.close();
    mockedLoggerFactory.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Initiate shutdown when killing megacmd process, moving logfile and lock releasing fails")
  public void initiateShutdown_KillMegaCMD_MoveLog_LockRelease_AllFailed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    Main.fs = mockFs;

    Logger mainLogger = (Logger) LoggerFactory.getLogger(Main.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    mainLogger.addAppender(listAppender);
    Main.log = mainLogger;

    CommandHandler commandHandlerMock = mock(CommandHandler.class);
    ScreenRecorder screenRecorderMock = mock(ScreenRecorder.class);
    KeyLog keyLogMock = mock(KeyLog.class);
    Archiver archiverMock = mock(Archiver.class);
    Main.commandHandler = commandHandlerMock;
    Main.screenRecorder = screenRecorderMock;
    Main.keyLog = keyLogMock;
    Main.archiver = archiverMock;

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
    File lockFileMock = mock(File.class);
    Main.fileLock = fileLockMock;
    Main.randomAccessFile = randomAccessFileMock;
    Main.lockFile = lockFileMock;
    doThrow(IOException.class).when(fileLockMock).release();

    MockedStatic<GlobalScreen> mockedGlobalScreen = mockStatic(GlobalScreen.class);

    Main.initiateShutdown();
    List<ILoggingEvent> logsList = listAppender.list;

    assertTrue(Main.SHUTDOWN);
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
}
