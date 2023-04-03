package com.rmac.updater;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdaterTest {

  @Test
  @DisplayName("Start when no arguments are passed")
  public void start_NoArgs() throws Exception {
    Updater updater = new Updater();
    MockedStatic<Constants> constants = mockStatic(Constants.class);

    int statusCode = catchSystemExit(() -> updater.start(new String[]{}));

    assertEquals(0, statusCode);
    constants.verify(() -> Constants.setRuntimeLocation(anyString()), never());

    constants.close();
  }

  @Test
  @DisplayName("Start when setting current location fails")
  public void start_SetCurrentLocation_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    MockedStatic<Constants> constants = mockStatic(Constants.class);

    constants.when(() -> Constants.setRuntimeLocation(anyString())).thenAnswer(invc -> null);
    constants.when(Constants::setCurrentLocation).thenReturn(false);

    Updater.DELAYED_START = -1;
    int statusCode = catchSystemExit(() -> {
      Updater updater = spy(new Updater());
      updater.start(args);
    });

    assertEquals(0, statusCode);

    constants.close();
  }

  @Test
  @DisplayName("Start when acquiring instance lock fails")
  public void start_InstanceLock_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};

    MockedStatic<Constants> constants = mockStatic(Constants.class);

    constants.when(() -> Constants.setRuntimeLocation(anyString())).thenAnswer(invc -> null);
    constants.when(Constants::setCurrentLocation).thenReturn(true);

    Updater.DELAYED_START = 1;
    Updater updater = spy(new Updater());
    doReturn(false).when(updater).lockInstance(anyString());

    int statusCode = catchSystemExit(() -> updater.start(args));

    assertEquals(0, statusCode);
    verify(updater, never()).loadConfig(anyString());

    constants.close();
  }

  @Test
  @DisplayName("Start when update is not processed")
  public void start_Update_Not_Processed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};
    Constants.INSTANCE_LOCK_LOCATION = "X:\\test\\RMAC\\updater.lock";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";

    MockedStatic<Constants> constants = mockStatic(Constants.class);
    SocketClient sc1 = mock(SocketClient.class);
    Monitor monitor = mock(Monitor.class);
    FileSystem fs = mock(FileSystem.class);

    constants.when(() -> Constants.setRuntimeLocation(anyString())).thenAnswer(invc -> null);
    constants.when(Constants::setCurrentLocation).thenReturn(true);

    Updater.DELAYED_START = 1;
    Updater.fs = fs;
    Updater updater = spy(new Updater());
    doReturn(true).when(updater).lockInstance(eq(Constants.INSTANCE_LOCK_LOCATION));
    doNothing().when(updater).loadConfig(anyString());
    doReturn(sc1).when(updater).getInstance(eq(SocketClient.class));
    doReturn(monitor).when(updater).getInstance(eq(Monitor.class));
    doNothing().when(updater).verifyWorkspace();
    doNothing().when(updater).readVersion();
    doReturn(false).when(updater).startUpdate();
    doNothing().when(updater).addShutdownHook();

    updater.start(args);

    verify(sc1).start();
    verify(sc1, never()).shutdown();
    verify(monitor).start();

    constants.close();
  }

  @Test
  @DisplayName("Start when update is processed and disconnection with old client fails")
  public void start_Update_Processed_OldClient_Shutdown_Failed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};
    Constants.INSTANCE_LOCK_LOCATION = "X:\\test\\RMAC\\updater.lock";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";

    MockedStatic<Constants> constants = mockStatic(Constants.class);
    SocketClient sc1 = mock(SocketClient.class);
    SocketClient sc2 = mock(SocketClient.class);
    Monitor monitor = mock(Monitor.class);
    FileSystem fs = mock(FileSystem.class);

    constants.when(() -> Constants.setRuntimeLocation(anyString())).thenAnswer(invc -> null);
    constants.when(Constants::setCurrentLocation).thenReturn(true);
    doThrow(Exception.class).when(sc1).shutdown();

    Updater.DELAYED_START = 1;
    Updater.fs = fs;
    Updater updater = spy(new Updater());
    doReturn(true).when(updater).lockInstance(eq(Constants.INSTANCE_LOCK_LOCATION));
    doNothing().when(updater).loadConfig(anyString());
    doReturn(sc1).doReturn(sc2).when(updater).getInstance(eq(SocketClient.class));
    doReturn(monitor).when(updater).getInstance(eq(Monitor.class));
    doNothing().when(updater).verifyWorkspace();
    doNothing().when(updater).readVersion();
    doReturn(true).when(updater).startUpdate();
    doNothing().when(updater).addShutdownHook();

    updater.start(args);

    verify(sc1).start();
    verify(sc1).shutdown();
    verify(sc2).start();
    verify(monitor).start();

    constants.close();
  }

  @Test
  @DisplayName("Start when update is processed")
  public void start_Update_Processed() throws Exception {
    String[] args = new String[]{"X:\\test\\RMAC"};
    Constants.INSTANCE_LOCK_LOCATION = "X:\\test\\RMAC\\updater.lock";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";

    MockedStatic<Constants> constants = mockStatic(Constants.class);
    SocketClient sc1 = mock(SocketClient.class);
    SocketClient sc2 = mock(SocketClient.class);
    Monitor monitor = mock(Monitor.class);
    FileSystem fs = mock(FileSystem.class);

    constants.when(() -> Constants.setRuntimeLocation(anyString())).thenAnswer(invc -> null);
    constants.when(Constants::setCurrentLocation).thenReturn(true);

    Updater.DELAYED_START = 1;
    Updater.fs = fs;
    Updater updater = spy(new Updater());
    doReturn(true).when(updater).lockInstance(eq(Constants.INSTANCE_LOCK_LOCATION));
    doNothing().when(updater).loadConfig(anyString());
    doReturn(sc1).doReturn(sc2).when(updater).getInstance(eq(SocketClient.class));
    doReturn(monitor).when(updater).getInstance(eq(Monitor.class));
    doNothing().when(updater).verifyWorkspace();
    doNothing().when(updater).readVersion();
    doReturn(true).when(updater).startUpdate();
    doNothing().when(updater).addShutdownHook();

    updater.start(args);

    verify(sc1).start();
    verify(sc1).shutdown();
    verify(sc2).start();
    verify(monitor).start();

    constants.close();
  }

  @Test
  @DisplayName("Load config succeeds")
  public void loadConfig_Success() throws IOException {
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);

    when(fs.getReader(anyString())).thenReturn(reader);
    when(reader.readLine())
        .thenReturn("ApiServerUrl=testurl")
        .thenReturn("ClientHealthCheckInterval=1000")
        .thenReturn("Test=Test")
        .thenReturn(null);

    Updater.fs = fs;
    updater.loadConfig("X:\\test\\RMAC\\config.rmac");

    assertEquals("testurl", Updater.SERVER_URL);
    assertEquals(1000, Updater.HEALTH_CHECK_INTERVAL);
  }

  @Test
  @DisplayName("Load config fails")
  public void loadConfig_Failed() throws Exception {
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);

    when(fs.getReader(anyString())).thenReturn(reader);
    when(reader.readLine()).thenThrow(IOException.class);

    Updater.fs = fs;
    int statusCode = catchSystemExit(() -> updater.loadConfig("X:\\test\\RMAC\\config.rmac"));

    assertEquals(0, statusCode);
  }

  @Test
  @DisplayName("Start update when no update")
  public void startUpdate_NoUpdate() {
    Updater updater = spy(Updater.class);

    doReturn(false).when(updater).checkForUpdates();

    boolean result = updater.startUpdate();

    assertFalse(result);
  }

  @Test
  @DisplayName("Start update when update fails")
  public void startUpdate_Failed() {
    Updater updater = spy(Updater.class);

    doReturn(true).when(updater).checkForUpdates();
    doReturn(true).when(updater).createUpdateLock();
    doReturn(true).when(updater).stopRMAC();
    doReturn(true).when(updater).update();
    doReturn(true).when(updater).startRMAC();
    doReturn(false).when(updater).deleteUpdateLock();

    boolean result = updater.startUpdate();

    assertTrue(result);
  }

  @Test
  @DisplayName("Start update when update succeeds")
  public void startUpdate_Success() {
    Updater updater = spy(Updater.class);

    doReturn(true).when(updater).checkForUpdates();
    doReturn(true).when(updater).createUpdateLock();
    doReturn(true).when(updater).stopRMAC();
    doReturn(true).when(updater).update();
    doReturn(true).when(updater).startRMAC();
    doReturn(true).when(updater).deleteUpdateLock();

    boolean result = updater.startUpdate();

    assertTrue(result);
  }

  @Test
  @DisplayName("Read version fails")
  public void readVersion_Failed() throws IOException {
    Constants.RMAC_LOCATION = "X:\\test\\Live\\RMACClient.jar";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).getJarFile(anyString());

    Updater.fs = fs;
    Updater.version = null;
    updater.readVersion();

    assertEquals("Unknown", Updater.version);
  }

  @Test
  @DisplayName("Read version succeeds")
  public void readVersion_Success() throws IOException {
    Constants.RMAC_LOCATION = "X:\\test\\Live\\RMACClient.jar";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    JarFile jarFile = mock(JarFile.class);
    Manifest manifest = mock(Manifest.class);
    Attributes attr = mock(Attributes.class);

    doReturn(jarFile).when(fs).getJarFile(anyString());
    doReturn(manifest).when(jarFile).getManifest();
    doReturn(attr).when(manifest).getMainAttributes();
    doReturn("1.0.0").when(attr).getValue(eq("Version"));

    Updater.fs = fs;
    updater.readVersion();

    assertEquals("1.0.0", Updater.version);
  }

  @Test
  @DisplayName("Check for updates when service returns no data")
  public void checkForUpdates_NoData() {
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);

    doReturn(new String[]{}).when(service).getUpdate(anyString());

    Updater.version = "1.0.0";
    Updater.service = service;
    boolean result = updater.checkForUpdates();

    assertFalse(result);
    verify(updater, never()).downloadUpdate(anyString(), anyString());
  }

  @Test
  @DisplayName("Check for updates when previously downloaded update doesn't already exist and download fails")
  public void checkForUpdates_Update_NotDownloaded_Download_Failed() {
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(new String[]{"testurl", "testchecksum"}).when(service).getUpdate(anyString());
    doReturn(false).when(fs).exists(anyString());
    doReturn(false).when(updater).downloadUpdate(anyString(), anyString());

    Updater.version = "1.0.0";
    Updater.service = service;
    Updater.fs = fs;
    boolean result = updater.checkForUpdates();

    assertFalse(result);
    verify(updater).downloadUpdate(eq("testurl"), eq("testchecksum"));
  }

  @Test
  @DisplayName("Check for updates when previously downloaded update exists, integrity check fails, download succeeds")
  public void checkForUpdates_Integrity_Failed_Download_Success() throws IOException {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    doReturn(new String[]{"testurl", "testchecksum"}).when(service).getUpdate(anyString());
    doReturn(true).when(fs).exists(anyString());
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString())).thenReturn(false);
    doReturn(true).when(updater).downloadUpdate(anyString(), anyString());

    Updater.version = "1.0.0";
    Updater.service = service;
    Updater.fs = fs;
    boolean result = updater.checkForUpdates();

    assertTrue(result);
    verify(fs).delete(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));
    verify(updater).downloadUpdate(eq("testurl"), eq("testchecksum"));

    checksum.close();
  }

  @Test
  @DisplayName("Check for updates when previously downloaded update exists, integrity succeeds")
  public void checkForUpdates_Integrity_Success() {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    doReturn(new String[]{"testurl", "testchecksum"}).when(service).getUpdate(anyString());
    doReturn(true).when(fs).exists(anyString());
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString())).thenReturn(true);

    Updater.version = "1.0.0";
    Updater.service = service;
    Updater.fs = fs;
    boolean result = updater.checkForUpdates();

    assertTrue(result);
    verify(updater, never()).downloadUpdate(eq("testurl"), eq("testchecksum"));

    checksum.close();
  }

  @Test
  @DisplayName("Check for updates when previously downloaded update exists, integrity error, delete fails")
  public void checkForUpdates_Integrity_Error_Delete_Error() throws IOException {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    doReturn(new String[]{"testurl", "testchecksum"}).when(service).getUpdate(anyString());
    doReturn(true).when(fs).exists(anyString());
    doThrow(IOException.class).when(fs).delete(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString()))
        .thenThrow(IOException.class);
    doReturn(true).when(updater).downloadUpdate(anyString(), anyString());

    Updater.version = "1.0.0";
    Updater.service = service;
    Updater.fs = fs;
    boolean result = updater.checkForUpdates();

    assertTrue(result);
    verify(updater).downloadUpdate(eq("testurl"), eq("testchecksum"));

    checksum.close();
  }

  @Test
  @DisplayName("Check for updates when previously downloaded update exists, integrity error, delete succeeds")
  public void checkForUpdates_Integrity_Error_Delete_Success() {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Service service = mock(Service.class);
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    doReturn(new String[]{"testurl", "testchecksum"}).when(service).getUpdate(anyString());
    doReturn(true).when(fs).exists(anyString());
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString()))
        .thenThrow(IOException.class);
    doReturn(true).when(updater).downloadUpdate(anyString(), anyString());

    Updater.version = "1.0.0";
    Updater.service = service;
    Updater.fs = fs;
    boolean result = updater.checkForUpdates();

    assertTrue(result);
    verify(updater).downloadUpdate(eq("testurl"), eq("testchecksum"));

    checksum.close();
  }

  @Test
  @DisplayName("Download update fails and max retries exceeded")
  public void downloadUpdate_Failed_Retries_Exceeded() throws Exception {
    Updater updater = spy(Updater.class);

    doThrow(IOException.class).when(updater).attemptDownload(anyString(), anyString());

    Updater.MAX_RETRIES_UPDATE_DOWNLOAD = 0;

    boolean result = updater.downloadUpdate("testurl", "testchecksum");

    assertFalse(result);
  }

  @Test
  @DisplayName("Download update fails and cooldown interrupted")
  public void downloadUpdate_Failed_Cooldown_Interrupted() throws Exception {
    Updater updater = spy(Updater.class);

    doThrow(IOException.class).when(updater).attemptDownload(anyString(), anyString());

    Updater.MAX_RETRIES_UPDATE_DOWNLOAD = 1;
    Updater.COOLDOWN = -1;
    boolean result = updater.downloadUpdate("testurl", "testchecksum");

    assertFalse(result);
    verify(updater, times(2)).attemptDownload(eq("testurl"), eq("testchecksum"));
  }

  @Test
  @DisplayName("Download update succeeds")
  public void downloadUpdate_Success() throws Exception {
    Updater updater = spy(Updater.class);

    doThrow(IOException.class).doNothing().when(updater).attemptDownload(anyString(), anyString());

    Updater.MAX_RETRIES_UPDATE_DOWNLOAD = 1;
    Updater.COOLDOWN = 1;
    boolean result = updater.downloadUpdate("testurl", "testchecksum");

    assertTrue(result);
    verify(updater, times(2)).attemptDownload(eq("testurl"), eq("testchecksum"));
  }

  @Test
  @DisplayName("Attempt download when checksum mismatch")
  public void attemptDownload_Exception() throws Exception {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    MockedStatic<Channels> channels = mockStatic(Channels.class);
    ReadableByteChannel rbChannel = mock(ReadableByteChannel.class);
    FileOutputStream fos = mock(FileOutputStream.class);
    FileSystem fs = mock(FileSystem.class);
    FileChannel fChannel = mock(FileChannel.class);
    InputStream is = mock(InputStream.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    channels.when(() -> Channels.newChannel((InputStream) any())).thenReturn(rbChannel);
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString())).thenReturn(false);
    doReturn(is).when(updater).getStream(anyString());
    doReturn(fos).when(fs).getFOS(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));
    doReturn(fChannel).when(fos).getChannel();

    Updater.ATTEMPT = 2;
    Updater.fs = fs;
    assertThrows(Exception.class, () -> updater.attemptDownload("testurl", "testchecksum"));

    verify(fChannel).close();
    verify(fos).close();
    verify(rbChannel).close();
    assertEquals(3, Updater.ATTEMPT);

    channels.close();
    checksum.close();
  }

  @Test
  @DisplayName("Attempt download when checksum succeeds")
  public void attemptDownload_Success() throws Exception {
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    MockedStatic<Channels> channels = mockStatic(Channels.class);
    ReadableByteChannel rbChannel = mock(ReadableByteChannel.class);
    FileOutputStream fos = mock(FileOutputStream.class);
    FileSystem fs = mock(FileSystem.class);
    FileChannel fChannel = mock(FileChannel.class);
    InputStream is = mock(InputStream.class);
    MockedStatic<Checksum> checksum = mockStatic(Checksum.class);

    channels.when(() -> Channels.newChannel((InputStream) any())).thenReturn(rbChannel);
    checksum.when(() -> Checksum.verifyChecksum(anyString(), anyString())).thenReturn(true);
    doReturn(is).when(updater).getStream(anyString());
    doReturn(fos).when(fs).getFOS(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));
    doReturn(fChannel).when(fos).getChannel();

    Updater.ATTEMPT = 2;
    Updater.fs = fs;
    updater.attemptDownload("testurl", "testchecksum");

    verify(fChannel).close();
    verify(fos).close();
    verify(rbChannel).close();
    assertEquals(0, Updater.ATTEMPT);

    channels.close();
    checksum.close();
  }

  @Test
  @DisplayName("Create update lock fails")
  public void createUpdateLock_Failed() throws IOException {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).create(eq(Constants.UPDATE_LOCK_LOCATION));

    Updater.fs = fs;
    boolean result = updater.createUpdateLock();

    assertFalse(result);
  }

  @Test
  @DisplayName("Create update lock succeeds")
  public void createUpdateLock_Success() {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    Updater.fs = fs;
    boolean result = updater.createUpdateLock();

    assertTrue(result);
  }

  @Test
  @DisplayName("Delete update lock fails")
  public void deleteUpdateLock_Failed() throws IOException {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).delete(eq(Constants.UPDATE_LOCK_LOCATION));

    Updater.fs = fs;
    boolean result = updater.deleteUpdateLock();

    assertFalse(result);
  }

  @Test
  @DisplayName("Delete update lock succeeds")
  public void deleteUpdateLock_Success() {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    Updater.fs = fs;
    boolean result = updater.deleteUpdateLock();

    assertTrue(result);
  }

  @Test
  @DisplayName("Stop RMAC client when no connection and pid command execution fails")
  public void stopRMAC_NoConnection_PIDCommand_Failed() throws IOException {
    Commands.C_RMAC_CLIENT_PID = "\"testcommand\"";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);

    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any());
    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doThrow(IOException.class).when(reader).readLine();

    Updater.client = null;
    Updater.fs = fs;
    boolean result = updater.stopRMAC();

    assertTrue(result);

    pipe.close();
  }

  @Test
  @DisplayName("Stop RMAC client when connection exists, stop wait interrupted and kill command succeeds")
  public void stopRMAC_Connection_StopInterrupted_KillCommand_Success() throws IOException {
    Commands.C_RMAC_CLIENT_PID = "\"whoami\"";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);
    SocketClient client = mock(SocketClient.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);
    Process pidProc = mock(Process.class);
    Process killProc = mock(Process.class);

    doReturn(mock(InputStream.class)).when(pidProc).getInputStream();
    doReturn(mock(OutputStream.class)).when(pidProc).getOutputStream();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any());
    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doReturn("1534").doReturn(null).when(reader).readLine();
    doReturn(pidProc).when(updater).startProcess(any());
    doReturn(killProc).when(mockRuntime).exec(anyString());
    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    Updater.CLIENT_STOP_WAIT = -1;
    Updater.client = client;
    Updater.fs = fs;
    boolean result = updater.stopRMAC();

    assertTrue(result);
    verify(mockPipe).start();
    verify(reader).close();
    verify(writer).close();

    pipe.close();
    runtime.close();
  }

  @Test
  @DisplayName("Stop RMAC client when connection exists, kill command interrupted")
  public void stopRMAC_KillCommand_Interrupted() throws IOException, InterruptedException {
    Commands.C_RMAC_CLIENT_PID = "\"whoami\"";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);
    SocketClient client = mock(SocketClient.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);
    Process pidProc = mock(Process.class);
    Process killProc = mock(Process.class);

    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any());
    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doReturn("1534").doReturn(null).when(reader).readLine();
    doReturn(mock(InputStream.class)).when(pidProc).getInputStream();
    doReturn(mock(OutputStream.class)).when(pidProc).getOutputStream();
    doReturn(pidProc).when(updater).startProcess(any());
    doThrow(new InterruptedException()).when(killProc).waitFor();
    doReturn(killProc).when(mockRuntime).exec(anyString());
    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    Updater.CLIENT_STOP_WAIT = 1;
    Updater.client = client;
    Updater.fs = fs;
    boolean result = updater.stopRMAC();

    assertTrue(result);
    verify(mockPipe).start();
    verify(reader).close();
    verify(writer).close();

    pipe.close();
    runtime.close();
  }

  @Test
  @DisplayName("Update when delete fails and replace fails")
  public void update_Delete_Failed_Replace_Failed() throws IOException {
    Constants.RMAC_LOCATION = "X:\\test\\Live\\RMACClient.jar";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).delete(anyString());
    doThrow(IOException.class).when(fs).copy(anyString(), anyString(), any());

    Updater.fs = fs;
    boolean result = updater.update();

    assertFalse(result);
    verify(fs, never()).delete(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));
  }

  @Test
  @DisplayName("Update when replace succeeds, delete fails")
  public void update_Replace_Success_Delete_Failed() throws IOException {
    Constants.RMAC_LOCATION = "X:\\test\\Live\\RMACClient.jar";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).delete(eq(Constants.UPDATE_LOCATION + "RMACClient.jar"));

    Updater.fs = fs;
    boolean result = updater.update();

    assertTrue(result);
  }

  @Test
  @DisplayName("Update succeeds")
  public void update_Success() {
    Constants.RMAC_LOCATION = "X:\\test\\Live\\RMACClient.jar";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    Updater.fs = fs;
    boolean result = updater.update();

    assertTrue(result);
  }

  @Test
  @DisplayName("Start RMAC client when start wait interrupted")
  public void startRMAC_Wait_Interrupted() {
    Updater updater = spy(Updater.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);

    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    Updater.CLIENT_START_WAIT = -1;
    boolean result = updater.startRMAC();

    assertFalse(result);

    runtime.close();
  }

  @Test
  @DisplayName("Start RMAC client succeeds")
  public void startRMAC_Success() {
    Updater updater = spy(Updater.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);

    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    Updater.CLIENT_START_WAIT = 1;
    boolean result = updater.startRMAC();

    assertTrue(result);

    runtime.close();
  }

  @Test
  @DisplayName("Verify workspace when update lockfile doesn't exist")
  public void verifyWorkspace_NoLockFile() throws IOException {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(false).when(fs).exists(anyString());

    Updater.fs = fs;
    updater.verifyWorkspace();

    verify(fs, never()).delete(anyString());
  }

  @Test
  @DisplayName("Verify workspace when update lockfile exists and delete fails")
  public void verifyWorkspace_Delete_Failed() throws IOException {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(true).when(fs).exists(anyString());
    doThrow(IOException.class).when(fs).delete(anyString());

    Updater.fs = fs;
    updater.verifyWorkspace();

    verify(fs, never()).delete(eq(Constants.UPDATE_LOCK_LOCATION));
  }

  @Test
  @DisplayName("Verify workspace succeeds")
  public void verifyWorkspace_Success() throws IOException {
    Constants.UPDATE_LOCK_LOCATION = "X:\\test\\RMAC\\update.lock";
    Constants.UPDATE_LOCATION = "X:\\test\\RMAC\\update\\";
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(true).when(fs).exists(anyString());

    Updater.fs = fs;
    updater.verifyWorkspace();

    verify(fs, times(2)).delete(anyString());
  }

  @Test
  @DisplayName("Add shutdown hook")
  public void addShutdownHook() {
    Runtime runtime = mock(Runtime.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

    new Updater().addShutdownHook();
    verify(runtime).addShutdownHook(any());

    mockedRuntime.close();
  }

  @Test
  @DisplayName("Shutdown when instance lock release fails")
  public void shutdown_InstanceLock_Release_Failed() throws Exception {
    Constants.INSTANCE_LOCK_LOCATION = "X:\\test\\RMAC\\updater.lock";
    Updater updater = spy(Updater.class);
    FileLock fileLock = mock(FileLock.class);
    RandomAccessFile raFile = mock(RandomAccessFile.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).delete(anyString());

    Updater.fs = fs;
    Updater.client = null;
    Updater.monitor = null;
    Updater.fileLock = fileLock;
    Updater.randomAccessFile = raFile;
    updater.shutdown();

    verify(fileLock).release();
    verify(raFile).close();
  }

  @Test
  @DisplayName("Shutdown succeeds")
  public void shutdown_Success() throws Exception {
    Constants.INSTANCE_LOCK_LOCATION = "X:\\test\\RMAC\\updater.lock";
    Updater updater = spy(Updater.class);
    FileLock fileLock = mock(FileLock.class);
    RandomAccessFile raFile = mock(RandomAccessFile.class);
    FileSystem fs = mock(FileSystem.class);
    SocketClient sc = mock(SocketClient.class);
    Monitor monitor = mock(Monitor.class);

    Updater.fs = fs;
    Updater.fileLock = fileLock;
    Updater.randomAccessFile = raFile;
    Updater.client = sc;
    Updater.monitor = monitor;
    updater.shutdown();

    verify(fileLock).release();
    verify(raFile).close();
    verify(fs).delete(eq(Constants.INSTANCE_LOCK_LOCATION));
    verify(monitor).shutdown();
    verify(sc).shutdown();
  }

  @Test
  @DisplayName("Lock instance failed")
  public void lockInstance_Failed1() throws FileNotFoundException {
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(FileNotFoundException.class).when(fs).createRandomAccessFile(anyString(), anyString());

    Updater.fs = fs;
    boolean result = updater.lockInstance(anyString());

    assertFalse(result);
  }

  @Test
  @DisplayName("Lock instance failed, null fileLock")
  public void lockInstance_Failed2() throws IOException {
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    FileChannel channel = mock(FileChannel.class);
    RandomAccessFile raFile = mock(RandomAccessFile.class);

    doReturn(raFile).when(fs).createRandomAccessFile(anyString(), anyString());
    doReturn(channel).when(raFile).getChannel();
    doReturn(null).when(channel).tryLock();

    Updater.fs = fs;
    boolean result = updater.lockInstance(anyString());

    assertFalse(result);
  }

  @Test
  @DisplayName("Lock instance succeeds")
  public void lockInstance_Success() throws IOException {
    Updater updater = spy(Updater.class);
    FileSystem fs = mock(FileSystem.class);
    FileChannel channel = mock(FileChannel.class);
    RandomAccessFile raFile = mock(RandomAccessFile.class);
    FileLock fileLock = mock(FileLock.class);

    doReturn(raFile).when(fs).createRandomAccessFile(anyString(), anyString());
    doReturn(channel).when(raFile).getChannel();
    doReturn(fileLock).when(channel).tryLock();

    Updater.fs = fs;
    boolean result = updater.lockInstance(anyString());

    assertTrue(result);
  }

  @Test
  @DisplayName("Start process")
  public void startProcess() throws IOException {
    ProcessBuilder builder = mock(ProcessBuilder.class);

    new Updater().startProcess(builder);

    verify(builder).start();
  }

  @Test
  @DisplayName("Get instance")
  public void getInstance() throws InstantiationException, IllegalAccessException {
    String testObj = (String) (new Updater().getInstance(String.class));
    assertEquals(testObj.getClass(), String.class);
  }
}
