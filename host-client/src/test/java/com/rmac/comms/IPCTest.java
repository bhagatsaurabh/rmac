package com.rmac.comms;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class IPCTest {

  @Test
  @DisplayName("Create and start server socket")
  public void createServerSocket() {
    ServerSocket ssMock = mock(ServerSocket.class);

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = new IPC();

    assertEquals(State.NEW, ipc.server.getState());
    assertNotNull(ipc.serverSocket);

    mockedSS.close();
    Mockito.reset();
  }

  @Test
  @DisplayName("Create and start server socket fails")
  public void createServerSocket_Failed() {
    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenThrow(IOException.class);

    IPC ipc = new IPC();

    assertNull(ipc.server);
    assertNull(ipc.serverSocket);

    mockedSS.close();
  }

  @Test
  @DisplayName("Start when server socket is null")
  public void start_Skipped() {
    ServerSocket ssMock = mock(ServerSocket.class);
    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = new IPC();
    ipc.serverSocket = null;
    ipc.server = mock(Thread.class);
    ipc.start();

    verify(ipc.server, never()).start();

    mockedSS.close();
  }

  @Test
  @DisplayName("Start when server socket is created successfully")
  public void start_Success() {
    ServerSocket ssMock = mock(ServerSocket.class);
    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    ipc.server = mock(Thread.class);
    ipc.start();

    verify(ipc.server).start();

    mockedSS.close();
  }

  @Test
  @DisplayName("Serve when client connection fails")
  public void serve_Accept_Failed() throws IOException {
    ServerSocket ssMock = mock(ServerSocket.class);
    when(ssMock.accept()).thenThrow(IOException.class);

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    ipc.serve();

    assertNull(ipc.socket);
    assertNull(ipc.out);
    assertNull(ipc.in);

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    ipc.serve();

    assertNotNull(ipc.socket);
    assertNotNull(ipc.out);
    assertNotNull(ipc.in);
    verify(ipc, never()).processMessage(anyString());
    verify(ipc, times(1)).serve();

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    ipc.serve();

    assertNotNull(ipc.socket);
    assertNotNull(ipc.out);
    assertNotNull(ipc.in);
    verify(ipc, never()).processMessage(anyString());
    verify(ipc, times(1)).serve();

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    doThrow(RuntimeException.class).when(ipc).processMessage(eq("test1"));
    ipc.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(mockSocket, times(1)).isClosed();
    verify(ipc, times(1)).serve();

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    doThrow(IOException.class).when(ipc).processMessage(eq("test1"));
    doNothing().when(ipc).processMessage(eq("test2"));
    ipc.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(ipc, times(1)).serve();
    verify(ipc, times(2)).processMessage(anyString());

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    doCallRealMethod().doNothing().when(ipc).serve();
    ipc.serve();

    verify(mockSocket).close();
    verify(os).close();
    verify(is).close();
    verify(ipc, times(2)).serve();
    verify(ipc, times(1)).processMessage(anyString());

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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    int statusCode = catchSystemExit(() -> {
      IPC ipc = spy(new IPC());
      ipc.out = out;
      ipc.in = in;
      ipc.socket = socket;

      ipc.processMessage("Stop");
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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    int statusCode = catchSystemExit(() -> {
      IPC ipc = spy(new IPC());
      ipc.out = out;
      ipc.in = in;
      ipc.socket = socket;

      ipc.processMessage("Stop");
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

    MockedStatic<IPC> mockedSS = mockStatic(IPC.class);
    mockedSS.when(() -> IPC.getInstance(anyInt())).thenReturn(ssMock);

    IPC ipc = spy(new IPC());
    ipc.out = out;
    ipc.processMessage("Check");

    verify(out).println(eq("Up"));
    verify(out).flush();

    mockedSS.close();
  }
}
