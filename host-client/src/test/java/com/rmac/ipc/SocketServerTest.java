package com.rmac.ipc;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SocketServerTest {

  @Test
  @DisplayName("Create and start server socket")
  public void createServerSocket() {
    ServerSocket ssMock = mock(ServerSocket.class);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = new SocketServer();

    assertEquals(State.NEW, socketServer.server.getState());
    assertNotNull(socketServer.serverSocket);

    mockedSS.close();
  }

  @Test
  @DisplayName("Create and start server socket fails")
  public void createServerSocket_Failed() {
    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenThrow(IOException.class);

    SocketServer socketServer = new SocketServer();

    assertNull(socketServer.server);
    assertNull(socketServer.serverSocket);

    mockedSS.close();
  }

  @Test
  @DisplayName("Start when server socket is null")
  public void start_Skipped() {
    ServerSocket ssMock = mock(ServerSocket.class);
    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = new SocketServer();
    socketServer.serverSocket = null;
    socketServer.server = mock(Thread.class);
    socketServer.start();

    verify(socketServer.server, never()).start();

    mockedSS.close();
  }

  @Test
  @DisplayName("Start when server socket is created successfully")
  public void start_Success() {
    ServerSocket ssMock = mock(ServerSocket.class);
    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    socketServer.server = mock(Thread.class);
    socketServer.start();

    verify(socketServer.server).start();

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when client connection fails")
  public void serve_Accept_Failed() throws IOException {
    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenThrow(IOException.class);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    socketServer.serve();

    assertNull(socketServer.socket);
    assertNull(socketServer.out);
    assertNull(socketServer.in);

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when socket is closed, stream close fails")
  public void serve_Socket_Closed_StreamClose_Failed() throws IOException {
    Socket mockSocket = mock(Socket.class);
    InputStream is = spy(new ByteArrayInputStream("".getBytes()));
    OutputStream os = spy(new ByteArrayOutputStream());
    when(mockSocket.isClosed()).thenReturn(true);
    when(mockSocket.getInputStream()).thenReturn(is);
    when(mockSocket.getOutputStream()).thenReturn(os);
    doThrow(IOException.class).when(is).close();

    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenReturn(mockSocket);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    socketServer.serve();

    assertNotNull(socketServer.socket);
    assertNotNull(socketServer.out);
    assertNotNull(socketServer.in);
    verify(socketServer, never()).processMessage(anyString());
    verify(socketServer, times(1)).serve();

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when socket is closed, stream close succeeds")
  public void serve_Socket_Closed_StreamClose_Success() throws IOException {
    Socket mockSocket = mock(Socket.class);
    InputStream is = spy(new ByteArrayInputStream("".getBytes()));
    OutputStream os = spy(new ByteArrayOutputStream());
    when(mockSocket.isClosed()).thenReturn(true);
    when(mockSocket.getInputStream()).thenReturn(is);
    when(mockSocket.getOutputStream()).thenReturn(os);
    doNothing().when(mockSocket).close();
    doNothing().when(is).close();
    doNothing().when(os).close();

    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenReturn(mockSocket);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    socketServer.serve();

    assertNotNull(socketServer.socket);
    assertNotNull(socketServer.out);
    assertNotNull(socketServer.in);
    verify(socketServer, never()).processMessage(anyString());
    verify(socketServer, times(1)).serve();

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when process message fails")
  public void serve_ProcessMessage_Failed1() throws IOException {
    Socket mockSocket = mock(Socket.class);
    InputStream is = spy(new ByteArrayInputStream("test1\ntest2\n".getBytes()));
    OutputStream os = spy(new ByteArrayOutputStream());
    when(mockSocket.isClosed()).thenReturn(false);
    when(mockSocket.getInputStream()).thenReturn(is);
    when(mockSocket.getOutputStream()).thenReturn(os);
    doNothing().when(mockSocket).close();
    doNothing().when(is).close();
    doNothing().when(os).close();

    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenReturn(mockSocket);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    doThrow(RuntimeException.class).when(socketServer).processMessage(eq("test1"));
    socketServer.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(mockSocket, times(1)).isClosed();
    verify(socketServer, times(1)).serve();

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when process message fails with IOException")
  public void serve_ProcessMessage_Failed2() throws IOException {
    Socket mockSocket = mock(Socket.class);
    InputStream is = spy(new ByteArrayInputStream("test1\ntest2".getBytes()));
    OutputStream os = spy(new ByteArrayOutputStream());
    when(mockSocket.isClosed()).thenReturn(false);
    when(mockSocket.getInputStream()).thenReturn(is);
    when(mockSocket.getOutputStream()).thenReturn(os);
    doNothing().when(mockSocket).close();
    doNothing().when(is).close();
    doNothing().when(os).close();

    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenReturn(mockSocket);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    doThrow(IOException.class).when(socketServer).processMessage(eq("test1"));
    doNothing().when(socketServer).processMessage(eq("test2"));
    socketServer.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(socketServer, times(1)).serve();
    verify(socketServer, times(2)).processMessage(anyString());

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when process message receives 'Exit'")
  public void serve_ProcessMessage_Exit() throws IOException {
    Socket mockSocket = mock(Socket.class);
    InputStream is = spy(new ByteArrayInputStream("Exit\n".getBytes()));
    OutputStream os = spy(new ByteArrayOutputStream());
    when(mockSocket.isClosed()).thenReturn(false);
    when(mockSocket.getInputStream()).thenReturn(is);
    when(mockSocket.getOutputStream()).thenReturn(os);
    doNothing().when(mockSocket).close();
    doNothing().when(is).close();
    doNothing().when(os).close();

    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenReturn(mockSocket);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    doCallRealMethod().doNothing().when(socketServer).serve();
    socketServer.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(socketServer, times(2)).serve();
    verify(socketServer, times(1)).processMessage(anyString());

    mockedSS.close();
  }

  @Test
  @DisplayName("Process message when message is 'Stop' and stream close fails")
  public void processMessage_Stop_StreamClose_Failed() throws Exception {
    Socket socket = mock(Socket.class);
    PrintStream out = mock(PrintStream.class);
    BufferedReader in = mock(BufferedReader.class);
    doThrow(IOException.class).when(in).close();
    doNothing().when(out).close();
    doThrow(IOException.class).when(socket).close();

    ServerSocket ssMock = mock(ServerSocket.class);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    int statusCode = catchSystemExit(() -> {
      SocketServer socketServer = spy(new SocketServer());
      socketServer.out = out;
      socketServer.in = in;
      socketServer.socket = socket;

      socketServer.processMessage("Stop");
    });

    assertEquals(0, statusCode);

    mockedSS.close();
  }

  @Test
  @DisplayName("Process message when message is 'Stop' and stream close succeeds")
  public void processMessage_Stop_StreamClose_Success() throws Exception {
    Socket socket = mock(Socket.class);
    PrintStream out = mock(PrintStream.class);
    BufferedReader in = mock(BufferedReader.class);
    doNothing().when(in).close();
    doNothing().when(out).close();
    doNothing().when(socket).close();

    ServerSocket ssMock = mock(ServerSocket.class);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    int statusCode = catchSystemExit(() -> {
      SocketServer socketServer = spy(new SocketServer());
      socketServer.out = out;
      socketServer.in = in;
      socketServer.socket = socket;

      socketServer.processMessage("Stop");
    });

    assertEquals(0, statusCode);

    mockedSS.close();
  }

  @Test
  @DisplayName("Process message when message is 'Stop' and stream close succeeds")
  public void processMessage_Check() throws Exception {
    PrintStream out = mock(PrintStream.class);
    doNothing().when(out).println(eq("Up"));
    doNothing().when(out).flush();

    ServerSocket ssMock = mock(ServerSocket.class);

    MockedStatic<SocketServer> mockedSS = mockStatic(SocketServer.class);
    mockedSS.when(() -> SocketServer.getInstance(anyInt())).thenReturn(ssMock);

    SocketServer socketServer = spy(new SocketServer());
    socketServer.out = out;
    socketServer.processMessage("Check");

    verify(out).println(eq("Up"));
    verify(out).flush();

    mockedSS.close();
  }
}
