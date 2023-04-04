package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class MegaClientTest {

  @Test
  @DisplayName("Execute command when network is down")
  public void executeCommand_NetDown() {
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    MockedStatic<MegaCommand> command = mockStatic(MegaCommand.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(false);

    MegaClient client = new MegaClient();
    MegaCommand result = client.executeCommand("test", "test");

    assertNull(result);

    connectivity.close();
    command.close();
  }

  @Test
  @DisplayName("Execute command fails")
  public void executeCommand_Failed() {
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    MockedStatic<MegaCommand> command = mockStatic(MegaCommand.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);
    command.when(() -> MegaCommand.run(any())).thenThrow(IOException.class);

    MegaClient client = new MegaClient();
    MegaCommand result = client.executeCommand("test", "test");

    assertNull(result);

    connectivity.close();
    command.close();
  }

  @Test
  @DisplayName("Execute command succeeds")
  public void executeCommand_Success() {
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    MockedStatic<MegaCommand> command = mockStatic(MegaCommand.class);
    MegaCommand mockCommand = mock(MegaCommand.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);
    command.when(() -> MegaCommand.run(any())).thenReturn(mockCommand);

    MegaClient client = new MegaClient();
    MegaCommand result = client.executeCommand("test", "test");

    assertEquals(mockCommand, result);

    connectivity.close();
    command.close();
  }

  @Test
  @DisplayName("Start server when server runtime doesn't exist")
  public void startServer_NoRuntime() {
    Constants.MEGASERVER_LOCATION = "X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe";
    FileSystem fs = mock(FileSystem.class);

    doReturn(false).when(fs).exists(eq("X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe"));

    RMAC.fs = fs;
    MegaClient client = spy(MegaClient.class);
    boolean result = client.startServer();

    assertFalse(result);
  }

  @Test
  @DisplayName("Start server delay interrupted")
  public void startServer_Interrupted() throws IOException {
    Constants.MEGASERVER_LOCATION = "X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe";
    FileSystem fs = mock(FileSystem.class);
    MegaClient client = spy(MegaClient.class);
    Process proc = mock(Process.class);

    doReturn(true).when(fs).exists(eq("X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe"));
    doReturn(proc).when(client).startProcess(any());

    RMAC.fs = fs;
    MegaClient.SERVER_START_DELAY = -1;

    boolean result = client.startServer();

    verify(client).startProcess(any());
    assertFalse(result);
  }

  @Test
  @DisplayName("Start server fails")
  public void startServer_Failed() throws IOException {
    Constants.MEGASERVER_LOCATION = "X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe";
    FileSystem fs = mock(FileSystem.class);
    MegaClient client = spy(MegaClient.class);
    Process proc = mock(Process.class);

    doReturn(true).when(fs).exists(eq("X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe"));
    doThrow(IOException.class).when(client).startProcess(any());

    RMAC.fs = fs;
    MegaClient.SERVER_START_DELAY = 0;

    boolean result = client.startServer();

    assertFalse(result);
  }

  @Test
  @DisplayName("Start server succeeds")
  public void startServer_Success() throws IOException {
    Constants.MEGASERVER_LOCATION = "X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe";
    FileSystem fs = mock(FileSystem.class);
    MegaClient client = spy(MegaClient.class);
    Process proc = mock(Process.class);

    doReturn(true).when(fs).exists(eq("X:\\test\\RMAC\\megacmd\\MEGACmdServer.exe"));
    doReturn(proc).when(client).startProcess(any());
    doReturn(true).when(client).isServerRunning();

    RMAC.fs = fs;
    MegaClient.SERVER_START_DELAY = 1;

    boolean result = client.startServer();

    assertTrue(MegaClient.SERVER_STARTED);
    assertTrue(result);
  }

  @Test
  @DisplayName("Login when server not started and server start fails")
  public void login_Server_Start_Failed() {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);

    doAnswer(invc -> {
      MegaClient.SERVER_STARTED = false;
      return false;
    }).when(mockClient).startServer();

    MegaClient.SERVER_STARTED = false;
    RMAC.mega = mockClient;
    client.login("testuser", "testpass", true);

    verify(mockClient, never()).executeCommand(any());
  }

  @Test
  @DisplayName("Login when client already logged-in")
  public void login_Already_Logged_In() {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);

    doReturn(true).when(mockClient).isLoggedIn();

    MegaClient.SERVER_STARTED = true;
    RMAC.mega = mockClient;
    client.login("testuser", "testpass", true);

    verify(mockClient, never()).executeCommand(any());
    assertTrue(MegaClient.LOGGED_IN);
  }

  @Test
  @DisplayName("Login command execution fails")
  public void login_Command_Failed() {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);

    doReturn(false).when(mockClient).isLoggedIn();
    doReturn(null).when(mockClient).executeCommand(anyString(), anyString(), anyString());

    MegaClient.SERVER_STARTED = true;
    RMAC.mega = mockClient;
    client.login("testuser", "testpass", true);

    assertFalse(MegaClient.LOGGED_IN);
  }

  @Test
  @DisplayName("Login command execution exceeds delay")
  public void login_Command_Delay_Exceeded() throws IOException {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);
    MegaCommand command = mock(MegaCommand.class);
    command.process = Runtime.getRuntime().exec("\"whoami\"");
    command.isAPIError = new AtomicBoolean(true);

    doReturn(false).when(mockClient).isLoggedIn();
    doReturn(command).when(mockClient).executeCommand(anyString(), anyString(), anyString());

    MegaClient.MAX_LOGIN_DELAY = -1;
    MegaClient.SERVER_STARTED = true;
    RMAC.mega = mockClient;
    client.login("testuser", "testpass", true);

    assertFalse(MegaClient.LOGGED_IN);
  }

  @Test
  @DisplayName("Upload file when not logged in and login fails")
  public void uploadFile_Login_Failed() {
    MegaClient mockClient = mock(MegaClient.class);
    Config config = mock(Config.class);

    doAnswer(invc -> {
      MegaClient.LOGGED_IN = false;
      return false;
    }).when(mockClient).login(anyString(), anyString(), eq(true));

    MegaClient.LOGGED_IN = false;
    RMAC.mega = mockClient;
    RMAC.config = config;
    new MegaClient().uploadFile("X:\\test.txt", "test\\test.txt");

    verify(mockClient, never()).executeCommand(any());
  }

  @Test
  @DisplayName("Upload file when command execution fails")
  public void uploadFile_Command_Failed() {
    MegaClient mockClient = mock(MegaClient.class);
    Config config = mock(Config.class);

    doReturn(null).when(mockClient).executeCommand(any());

    MegaClient.LOGGED_IN = true;
    RMAC.mega = mockClient;
    RMAC.config = config;
    boolean result = new MegaClient().uploadFile("X:\\test.txt", "test\\test.txt");

    assertFalse(result);
  }

  @Test
  @DisplayName("Upload file succeeds")
  public void uploadFile_Success() {
    MegaClient mockClient = mock(MegaClient.class);
    Config config = mock(Config.class);
    MegaCommand command = mock(MegaCommand.class);
    command.process = mock(Process.class);
    command.isAPIError = new AtomicBoolean(false);

    doReturn(command).when(mockClient).executeCommand(any());

    MegaClient.LOGGED_IN = true;
    RMAC.mega = mockClient;
    RMAC.config = config;
    boolean result = new MegaClient().uploadFile("X:\\test.txt", "test\\test.txt");

    assertTrue(result);
  }

  @Test
  @DisplayName("Is server running when process fails")
  public void isServerRunning_Process_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    MegaClient client = spy(MegaClient.class);
    BufferedReader reader = mock(BufferedReader.class);
    Process proc = mock(Process.class);

    doThrow(IOException.class).when(client).startProcess(any());

    RMAC.fs = fs;
    boolean result = client.isServerRunning();

    assertFalse(result);
  }

  @Test
  @DisplayName("Is server running succeeds")
  public void isServerRunning_Success() throws IOException, InterruptedException {
    FileSystem fs = mock(FileSystem.class);
    MegaClient client = spy(MegaClient.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    Process proc = mock(Process.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn("Running").doReturn(null).when(reader).readLine();
    doReturn(0).when(proc).waitFor();

    RMAC.fs = fs;
    boolean result = client.isServerRunning();

    assertTrue(result);

    pipe.close();
  }

  @Test
  @DisplayName("Is logged in when command execution fails")
  public void isLoggedIn_Command_Failed() {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);

    doReturn(null).when(mockClient).executeCommand(any());

    RMAC.mega = mockClient;
    boolean result = client.isLoggedIn();

    assertFalse(result);
  }

  @Test
  @DisplayName("Is logged in when command delay threshold exceeds")
  public void isLoggedIn_Command_Delay_Exceeds() throws InterruptedException {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);
    MegaCommand command = mock(MegaCommand.class);
    command.process = mock(Process.class);

    doReturn(false).when(command.process).waitFor(anyLong(), any());
    doReturn(command).when(mockClient).executeCommand(any());

    RMAC.mega = mockClient;
    boolean result = client.isLoggedIn();

    assertFalse(result);
  }

  @Test
  @DisplayName("Is logged in failed")
  public void isLoggedIn_Fails() throws InterruptedException {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);
    MegaCommand command = mock(MegaCommand.class);
    Process proc = mock(Process.class);
    command.isAPIError = new AtomicBoolean(false);

    doReturn(false).when(proc).waitFor(anyLong(), eq(TimeUnit.SECONDS));
    doReturn(command).when(mockClient).executeCommand(anyString());

    command.process = proc;
    RMAC.mega = mockClient;
    boolean result = client.isLoggedIn();

    assertFalse(result);
  }

  @Test
  @DisplayName("Is logged in interrupted")
  public void isLoggedIn_Interrupted() throws InterruptedException {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);
    MegaCommand command = mock(MegaCommand.class);
    Process proc = mock(Process.class);
    command.isAPIError = new AtomicBoolean(false);

    doThrow(InterruptedException.class).when(proc).waitFor(anyLong(), eq(TimeUnit.SECONDS));
    doReturn(command).when(mockClient).executeCommand(anyString());

    command.process = proc;
    RMAC.mega = mockClient;
    boolean result = client.isLoggedIn();

    assertFalse(result);
  }

  @Test
  @DisplayName("Is logged in succeeded")
  public void isLoggedIn_Success() throws InterruptedException {
    MegaClient mockClient = mock(MegaClient.class);
    MegaClient client = spy(MegaClient.class);
    MegaCommand command = mock(MegaCommand.class);
    Process proc = mock(Process.class);
    command.isAPIError = new AtomicBoolean(false);

    doReturn(true).when(proc).waitFor(anyLong(), eq(TimeUnit.SECONDS));
    doReturn(command).when(mockClient).executeCommand(anyString());

    command.process = proc;
    RMAC.mega = mockClient;
    boolean result = client.isLoggedIn();

    assertTrue(result);
  }
}
