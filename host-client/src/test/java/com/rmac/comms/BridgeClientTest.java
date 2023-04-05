package com.rmac.comms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.core.Config;
import com.rmac.core.Connectivity;
import com.rmac.core.Terminal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.java_websocket.enums.ReadyState;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class BridgeClientTest {

  @Test
  @DisplayName("BridgeClient Initialize socket fails")
  public void bridgeClient_InitializeSocket_Failed() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    Config config = mock(Config.class);
    RMAC.config = config;

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = new BridgeClient();

    mockedSocket.close();
  }

  @Test
  @DisplayName("BridgeClient Initialize socket succeeds")
  public void bridgeClient_InitializeSocket_Success() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    Config config = mock(Config.class);
    RMAC.config = config;
    Socket mockSocket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenReturn(mockSocket);

    BridgeClient bridgeClient = new BridgeClient();

    mockedSocket.close();
  }

  @Test
  @DisplayName("BridgeClient connectivity changes")
  public void bridgeClient_Connectivity_Change() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    Config config = mock(Config.class);
    RMAC.config = config;
    Socket mockSocket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenReturn(mockSocket);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    doNothing().when(bridgeClient).networkHandler(anyBoolean());
    Connectivity.listeners.forEach((listener) -> listener.accept(true));

    verify(bridgeClient).networkHandler(eq(true));

    mockedSocket.close();
  }

  @Test
  @DisplayName("BridgeClient config changes")
  public void bridgeClient_Config_Change() throws NoSuchFieldException, IllegalAccessException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    Config config = new Config();
    RMAC.config = config;
    config.setConfig("BridgeServerUrl", "https://test.abc.co", false);
    Socket mockSocket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenReturn(mockSocket);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    doNothing().when(bridgeClient).networkHandler(anyBoolean());
    RMAC.config.listeners.forEach((listener) -> listener.accept("test-key", "test-value"));

    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    verify(bridgeClient).sendMessage(argument.capture());
    assertEquals("config", argument.getValue().getEvent());
    assertEquals(config, argument.getValue().getData());

    mockedSocket.close();
  }

  @Test
  @DisplayName("Start BridgeClient")
  public void startBridgeClient() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Thread mockThread = spy(new Thread(() -> {
    }));

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = new BridgeClient();
    bridgeClient.thread = mockThread;

    bridgeClient.start();

    verify(mockThread).start();

    mockedSocket.close();
  }

  @Test
  @DisplayName("Start BridgeClient fails")
  public void startBridgeClient_Failed() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = new BridgeClient();
    bridgeClient.thread = null;

    bridgeClient.start();

    mockedSocket.close();
  }

  @Test
  @DisplayName("Run when connection succeeds")
  public void run_Connection_Success() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);

    doReturn(true).when(bridgeClient).connect();
    doNothing().when(bridgeClient).unloadBuffer();

    bridgeClient.run();

    verify(bridgeClient).unloadBuffer();
    assertNull(bridgeClient.thread);

    mockedSocket.close();
  }

  @Test
  @DisplayName("Run when connection fails, network up, max tries exceeded")
  public void run_Connection_Failed_NetworkUp_MaxTriesExceeded() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    MockedStatic<Connectivity> mockedConnectivity = mockStatic(Connectivity.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);
    mockedConnectivity.when(Connectivity::checkNetworkState).thenReturn(true);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    BridgeClient.MAX_RETRIES = 1;
    BridgeClient.RECONNECT_COOLDOWN = 123456;

    doReturn(false, true).when(bridgeClient).connect();
    doNothing().when(bridgeClient).unloadBuffer();
    doNothing().when(bridgeClient).waitDefinite(anyInt());

    bridgeClient.run();

    verify(bridgeClient).unloadBuffer();
    assertNull(bridgeClient.thread);
    verify(bridgeClient, times(1)).waitDefinite(eq(123456));

    mockedSocket.close();
    mockedConnectivity.close();
  }

  @Test
  @DisplayName("Run when connection fails, network down")
  public void run_Connection_Failed_NetworkDown() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    MockedStatic<Connectivity> mockedConnectivity = mockStatic(Connectivity.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);
    mockedConnectivity.when(Connectivity::checkNetworkState).thenReturn(false);

    BridgeClient bridgeClient = spy(BridgeClient.class);

    doReturn(false, true).when(bridgeClient).connect();
    doNothing().when(bridgeClient).unloadBuffer();
    doNothing().when(bridgeClient).waitIndefinite();

    bridgeClient.run();

    verify(bridgeClient).unloadBuffer();
    assertNull(bridgeClient.thread);
    verify(bridgeClient, times(1)).waitIndefinite();

    mockedSocket.close();
    mockedConnectivity.close();
  }

  @Test
  @DisplayName("Connect when socket already open")
  public void connect_Socket_Already_Open() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    doReturn(true).when(socket).isOpen();
    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;

    boolean result = bridgeClient.connect();

    verify(socket, times(0)).connectBlocking();
    verify(socket, times(0)).reconnectBlocking();
    assertTrue(result);

    mockedSocket.close();
  }

  @Test
  @DisplayName("Connect when socket not yet connected and fails")
  public void connect_Socket_NotYetConnected_Failed() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    doReturn(false).when(socket).isOpen();
    doReturn(ReadyState.NOT_YET_CONNECTED).when(socket).getReadyState();
    doReturn(false).when(socket).connectBlocking();
    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;

    boolean result = bridgeClient.connect();

    verify(socket).connectBlocking();
    verify(socket, times(0)).reconnectBlocking();
    assertFalse(result);

    mockedSocket.close();
  }

  @Test
  @DisplayName("Connect when socket closed and succeeds")
  public void connect_Socket_Closed_Success() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    doReturn(false).when(socket).isOpen();
    doReturn(ReadyState.CLOSED).when(socket).getReadyState();
    doReturn(true).when(socket).reconnectBlocking();
    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;

    boolean result = bridgeClient.connect();

    verify(socket, times(0)).connectBlocking();
    verify(socket).reconnectBlocking();
    assertTrue(result);

    mockedSocket.close();
  }

  @Test
  @DisplayName("Reconnect")
  public void reconnect() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);

    doNothing().when(bridgeClient).run();

    bridgeClient.reconnect();
    bridgeClient.thread.join();

    verify(bridgeClient).run();

    mockedSocket.close();
  }

  @Test
  @DisplayName("Wait-indefinite")
  public void waitIndefinite() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.thread = new Thread(() -> {
      ScheduledExecutorService stopper = Executors.newScheduledThreadPool(1);
      stopper.schedule(() -> bridgeClient.thread.interrupt(), 100, TimeUnit.MILLISECONDS);
      bridgeClient.waitIndefinite();
    });
    bridgeClient.thread.start();
    bridgeClient.thread.join();

    verify(bridgeClient).waitIndefinite();

    mockedSocket.close();
  }

  @Test
  @DisplayName("Wait-definite")
  public void waitDefinite() throws InterruptedException {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.thread = new Thread(() -> bridgeClient.waitDefinite(-1));
    bridgeClient.thread.start();
    bridgeClient.thread.join();

    verify(bridgeClient).waitDefinite(eq(-1));

    mockedSocket.close();
  }

  @Test
  @DisplayName("Send message 'hostid' when connection not ready")
  public void sendMessage_HostId_NotReady() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    BridgeClient.bufferedMessages.clear();

    doReturn(false).when(bridgeClient).isReady();

    Message message = Message.create("hostid", null, null);
    bridgeClient.sendMessage(message);

    assertEquals(0, BridgeClient.bufferedMessages.size());

    mockedSocket.close();
  }

  @Test
  @DisplayName("Send message when connection not ready")
  public void sendMessage_NotReady() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    BridgeClient.bufferedMessages.clear();

    doReturn(false).when(bridgeClient).isReady();

    Message message = Message.create("config", null, null);
    bridgeClient.sendMessage(message);

    assertEquals(1, BridgeClient.bufferedMessages.size());
    Message bufferedMessage = BridgeClient.bufferedMessages.poll();
    assert bufferedMessage != null;
    assertEquals("config", bufferedMessage.getEvent());

    mockedSocket.close();
  }

  @Test
  @DisplayName("Send message when connection is ready")
  public void sendMessage_Ready() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;

    doReturn(true).when(bridgeClient).isReady();

    Message message = Message.create("config", null, null);
    bridgeClient.sendMessage(message);

    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    verify(socket).emit(argument.capture());
    assertEquals("config", argument.getValue().getEvent());

    mockedSocket.close();
  }

  @Test
  @DisplayName("Is ready")
  public void isReady() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;

    doReturn(true).when(socket).isOpen();
    boolean result = bridgeClient.isReady();

    verify(socket).isOpen();
    assertTrue(result);

    mockedSocket.close();
  }

  @Test
  @DisplayName("Unload buffer, not ready")
  public void unloadBuffer_NotReady() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;
    BridgeClient.bufferedMessages = new LinkedList<>();
    BridgeClient.bufferedMessages.add(Message.create("test-event", null, null));

    doReturn(false).when(bridgeClient).isReady();
    bridgeClient.unloadBuffer();

    assertEquals(1, BridgeClient.bufferedMessages.size());
    verify(socket, times(0)).emit(any(Message.class));

    mockedSocket.close();
  }

  @Test
  @DisplayName("Unload buffer, ready")
  public void unloadBuffer_Ready() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;
    BridgeClient.bufferedMessages = new LinkedList<>();
    BridgeClient.bufferedMessages.add(Message.create("test-event", null, null));

    doReturn(true).when(bridgeClient).isReady();
    bridgeClient.unloadBuffer();

    assertEquals(0, BridgeClient.bufferedMessages.size());
    ArgumentCaptor<Message> argument = ArgumentCaptor.forClass(Message.class);
    verify(socket).emit(argument.capture());
    assertEquals("test-event", argument.getValue().getEvent());

    mockedSocket.close();
  }

  @Test
  @DisplayName("Shutdown, no socket")
  public void shutdown_NoSocket() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = null;

    bridgeClient.shutdown();

    mockedSocket.close();
  }

  @Test
  @DisplayName("Shutdown")
  public void shutdown() {
    MockedStatic<Socket> mockedSocket = mockStatic(Socket.class);
    RMAC.config = mock(Config.class);
    Socket socket = mock(Socket.class);
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    socket.terminals.put("test-ray-id", terminal);

    mockedSocket.when(() -> Socket.create(any())).thenThrow(URISyntaxException.class);

    BridgeClient bridgeClient = spy(BridgeClient.class);
    bridgeClient.socket = socket;
    bridgeClient.shutdown();

    verify(terminal).shutdown(eq(true));

    mockedSocket.close();
  }
}
