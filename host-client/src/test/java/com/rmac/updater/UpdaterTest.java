package com.rmac.updater;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

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
        .thenReturn("ServerUrl=testurl")
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

    when(updater.checkForUpdates()).thenReturn(false);

    boolean result = updater.startUpdate();

    assertFalse(result);
  }

  @Test
  @DisplayName("Start update when update fails")
  public void startUpdate_Failed() {
    Updater updater = spy(Updater.class);

    when(updater.checkForUpdates()).thenReturn(true);
    doReturn(true).when(updater).createLock();
    doReturn(true).when(updater).stopRMAC();
    doReturn(true).when(updater).update();
    doReturn(true).when(updater).startRMAC();
    doReturn(false).when(updater).deleteLock();

    boolean result = updater.startUpdate();

    assertTrue(result);
  }

  @Test
  @DisplayName("Start update when update succeeds")
  public void startUpdate_Success() {
    Updater updater = spy(Updater.class);

    when(updater.checkForUpdates()).thenReturn(true);
    doReturn(true).when(updater).createLock();
    doReturn(true).when(updater).stopRMAC();
    doReturn(true).when(updater).update();
    doReturn(true).when(updater).startRMAC();
    doReturn(true).when(updater).deleteLock();

    boolean result = updater.startUpdate();

    assertTrue(result);
  }
}
