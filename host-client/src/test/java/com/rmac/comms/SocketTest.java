package com.rmac.comms;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.gson.Gson;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.rmac.RMAC;
import com.rmac.core.Config;
import com.rmac.core.Terminal;
import com.rmac.process.CommandHandler;
import com.rmac.utils.Pair;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class SocketTest {

  @Test
  @DisplayName("Socket should send 'identity' signal when connection is opened")
  public void socket_Identity_On_Open() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    Config config = mock(Config.class);
    MockedStatic<Message> mockedMessage = mockStatic(Message.class);

    RMAC.config = config;
    socket.onOpen(null);

    verify(socket).emit(any());
    mockedMessage.verify(() -> Message.create(eq("identity"), eq(null), eq(config)));

    mockedMessage.close();
  }

  @Test
  @DisplayName("Socket onMessage")
  public void socket_OnMessage() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    doNothing().when(socket).parse(anyString());

    socket.onMessage("raw data");

    verify(socket).parse(eq("raw data"));
  }

  @Test
  @DisplayName("Socket onClose when BridgeClient running")
  public void socket_OnClose_BridgeClient_Running() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal mockTerminal = mock(Terminal.class);
    socket.terminals.put("test-terminal", mockTerminal);
    BridgeClient mockBC = mock(BridgeClient.class);
    mockBC.thread = new Thread(() -> {
    });

    RMAC.bridgeClient = mockBC;
    socket.onClose(0, "", true);

    verify(mockTerminal).orphaned();
    verify(mockBC, times(0)).reconnect();
  }

  @Test
  @DisplayName("Socket onClose when BridgeClient stopped")
  public void socket_OnClose_BridgeClient_Stopped() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal mockTerminal = mock(Terminal.class);
    socket.terminals.put("test-terminal", mockTerminal);
    BridgeClient mockBC = mock(BridgeClient.class);
    mockBC.thread = null;

    RMAC.bridgeClient = mockBC;
    socket.onClose(0, "", true);

    verify(mockTerminal).orphaned();
    verify(mockBC).reconnect();
  }

  @Test
  @DisplayName("Socket onError")
  public void socket_OnError() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    socket.onError(null);
  }

  @Test
  @DisplayName("Socket onEmit when socket is not open")
  public void socket_Emit_NotOpen() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    doReturn(false).when(socket).isOpen();

    socket.emit(null);

    verify(socket, times(0)).send(anyString());
  }

  @Test
  @DisplayName("Socket onEmit when socket is open")
  public void socket_Emit_Open() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    RMAC.config = mock(Config.class);

    doReturn(true).when(socket).isOpen();
    doNothing().when(socket).send(anyString());

    Message message = new Message("test-event", null, "Test data");

    socket.emit(message);

    verify(socket).send(eq(new Gson().toJson(message)));
  }

  @Test
  @DisplayName("Socket parse when event is 'config'")
  public void socket_Parse_Event_Config() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    RMAC.config = mock(Config.class);
    MockedStatic<Message> mockedMessage = mockStatic(Message.class);

    doNothing().when(socket).emit(any());

    String incomingMessage = "{\"event\":\"config\", \"rayId\":\"test-ray-id\"}";
    socket.parse(incomingMessage);

    verify(socket).emit(any());
    mockedMessage.verify(() -> Message.create(eq("config"), eq("test-ray-id"), eq(RMAC.config)));

    mockedMessage.close();
  }

  @Test
  @DisplayName("Socket parse when event is 'command' and invalid")
  public void socket_Parse_Event_Command_Invalid() throws URISyntaxException, IOException {
    Socket socket = spy(new Socket("ws://localhost"));
    RMAC.config = mock(Config.class);
    CommandHandler mockCH = mock(CommandHandler.class);
    RMAC.commandHandler = mockCH;

    String incomingMessage = "{\"event\":\"command\", \"rayId\":\"test-ray-id\", \"data\":\"\"}";
    socket.parse(incomingMessage);

    verify(mockCH, times(0)).execute(any(String[].class));
  }

  @Test
  @DisplayName("Socket parse when event is 'command' and failed")
  public void socket_Parse_Event_Command_Failed() throws URISyntaxException, IOException {
    Socket socket = spy(new Socket("ws://localhost"));
    RMAC.config = mock(Config.class);
    CommandHandler mockCH = mock(CommandHandler.class);
    RMAC.commandHandler = mockCH;

    doThrow(IOException.class).when(mockCH).execute(any(String[].class));

    String incomingMessage = "{\"event\":\"command\", \"rayId\":\"test-ray-id\", \"data\":\"test-command\"}";
    socket.parse(incomingMessage);

    verify(mockCH).execute(any(String[].class));
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:open' and already opened")
  public void socket_Parse_Event_TerminalOpen_Already_Opened() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    socket.terminals.put("test-ray-id", null);
    MockedStatic<Terminal> mockedTerminal = mockStatic(Terminal.class);

    String incomingMessage = "{\"event\":\"terminal:open\", \"rayId\":\"test-ray-id\", \"data\":\"\"}";
    socket.parse(incomingMessage);

    mockedTerminal.verify(
        () -> Terminal.create(anyString(), any(Socket.class), any(Pair.class)), times(0));

    mockedTerminal.close();
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:open'")
  public void socket_Parse_Event_TerminalOpen() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    MockedStatic<Terminal> mockedTerminal = mockStatic(Terminal.class);
    Terminal terminal = mock(Terminal.class);

    mockedTerminal.when(() -> Terminal.create(anyString(), any(Socket.class), any()))
        .thenReturn(terminal);

    String incomingMessage = "{\"event\":\"terminal:open\", \"rayId\":\"test-ray-id\", \"data\":\"\"}";
    socket.parse(incomingMessage);

    mockedTerminal.verify(() -> Terminal.create(eq("test-ray-id"), eq(socket), eq(null)));
    verify(terminal).start();
    assertEquals(terminal, socket.terminals.get("test-ray-id"));

    mockedTerminal.close();
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:data' no terminal")
  public void socket_Parse_Event_TerminalData_NoTerminal() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    String incomingMessage = "{\"event\":\"terminal:data\", \"rayId\":\"test-ray-id\", \"data\":\"test-data\"}";
    socket.parse(incomingMessage);
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:data' and failed")
  public void socket_Parse_Event_TerminalData_Failed() throws URISyntaxException, IOException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    socket.terminals.put("test-ray-id", terminal);

    doThrow(IOException.class).when(terminal).write(anyString());

    String incomingMessage = "{\"event\":\"terminal:data\", \"rayId\":\"test-ray-id\", \"data\":\"test-data\"}";
    socket.parse(incomingMessage);
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:data' and succeeds")
  public void socket_Parse_Event_TerminalData_Success() throws URISyntaxException, IOException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    socket.terminals.put("test-ray-id", terminal);

    String incomingMessage = "{\"event\":\"terminal:data\", \"rayId\":\"test-ray-id\", \"data\":\"test-data\"}";
    socket.parse(incomingMessage);

    verify(terminal).write(eq("test-data"));
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:resize' and no terminal")
  public void socket_Parse_Event_TerminalResize_NoTerminal() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    String incomingMessage = "{\"event\":\"terminal:resize\", \"rayId\":\"test-ray-id\", \"data\":\"250:100\"}";
    socket.parse(incomingMessage);
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:resize' and not started")
  public void socket_Parse_Event_TerminalResize_NotStarted() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    PtyProcess process = mock(PtyProcess.class);
    terminal.process = process;
    socket.terminals.put("test-ray-id", terminal);

    ArgumentCaptor<WinSize> argument = ArgumentCaptor.forClass(WinSize.class);

    String incomingMessage = "{\"event\":\"terminal:resize\", \"rayId\":\"test-ray-id\", \"data\":\"250:100\"}";
    socket.parse(incomingMessage);

    verify(process).setWinSize(argument.capture());
    assertEquals(250, argument.getValue().getColumns());
    assertEquals(100, argument.getValue().getRows());
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:resize' and started")
  public void socket_Parse_Event_TerminalResize_Started() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    terminal.process = null;
    socket.terminals.put("test-ray-id", terminal);

    String incomingMessage = "{\"event\":\"terminal:resize\", \"rayId\":\"test-ray-id\", \"data\":\"250:100\"}";
    socket.parse(incomingMessage);

    assertEquals(250L, (long) terminal.initialDimension.getFirst());
    assertEquals(100L, (long) terminal.initialDimension.getSecond());
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:close' and no terminal")
  public void socket_Parse_Event_TerminalClose_NoTerminal() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));

    String incomingMessage = "{\"event\":\"terminal:close\", \"rayId\":\"test-ray-id\", \"data\":\"250:100\"}";
    socket.parse(incomingMessage);
  }

  @Test
  @DisplayName("Socket parse when event is 'terminal:close'")
  public void socket_Parse_Event_TerminalClose() throws URISyntaxException {
    Socket socket = spy(new Socket("ws://localhost"));
    socket.terminals = new HashMap<>();
    Terminal terminal = mock(Terminal.class);
    socket.terminals.put("test-ray-id", terminal);

    String incomingMessage = "{\"event\":\"terminal:close\", \"rayId\":\"test-ray-id\", \"data\":\"250:100\"}";
    socket.parse(incomingMessage);

    verify(terminal).shutdown(eq(false));
  }
}
