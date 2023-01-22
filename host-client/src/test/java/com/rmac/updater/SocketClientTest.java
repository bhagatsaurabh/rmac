package com.rmac.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Thread.State;
import java.net.Socket;
import java.net.SocketException;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class SocketClientTest {

  @Test
  @DisplayName("Initializing Socket")
  public void createSocketClient() {
    SocketClient sc = spy(SocketClient.class);

    assertEquals(State.NEW, sc.client.getState());
  }

  @Test
  @DisplayName("Start method")
  public void start() {
    Thread mockThread = mock(Thread.class);
    SocketClient sc = spy(SocketClient.class);

    sc.client = mockThread;
    sc.start();

    verify(mockThread).start();
  }

  @Test
  @DisplayName("Connect fails")
  public void connect_Failed() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    ByteArrayOutputStream os = new ByteArrayOutputStream();

    doReturn(mockSocket).when(sc).getSocket();
    when(mockSocket.getOutputStream()).thenReturn(os);
    when(mockSocket.getInputStream()).thenThrow(IOException.class);

    SocketClient.ATTEMPT = 2;
    try {
      sc.connect();
    } catch (IOException e) {
      assertEquals(3, SocketClient.ATTEMPT);
    }
  }

  @Test
  @DisplayName("Connect succeeds")
  public void connect_Success() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ByteArrayInputStream is = new ByteArrayInputStream("".getBytes());

    doReturn(mockSocket).when(sc).getSocket();
    when(mockSocket.getOutputStream()).thenReturn(os);
    when(mockSocket.getInputStream()).thenReturn(is);

    SocketClient.ATTEMPT = 2;
    sc.connect();
    assertEquals(0, SocketClient.ATTEMPT);
  }

  @Test
  @DisplayName("Process message fails")
  public void processMessage_Failed() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);

    doThrow(IOException.class).when(out).writeBytes(anyString());

    SocketClient.MESSAGE = "Test";
    sc.out = out;
    boolean doContinue = sc.processMessage();

    assertTrue(doContinue);
  }

  @Test
  @DisplayName("Process message succeeds")
  public void processMessage_Success() {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);

    SocketClient.MESSAGE = "Exit";
    sc.out = out;
    boolean doContinue = sc.processMessage();

    assertFalse(doContinue);
  }

  @Test
  @DisplayName("Send message when socket is not connected")
  public void sendMessage_NoSocket() {
    SocketClient sc = spy(SocketClient.class);

    sc.socket = null;
    String response = sc.sendMessage("Test");

    assertNull(response);
  }

  @Test
  @DisplayName("Send message fails while reading response")
  public void sendMessage_Response_Failed1() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    Thread client = spy(sc.client);
    BufferedReader in = mock(BufferedReader.class);
    when(in.readLine()).thenThrow(SocketException.class);

    sc.in = in;
    sc.socket = mockSocket;
    sc.client = client;
    String response = sc.sendMessage("Test");

    assertNull(response);
  }

  @Test
  @DisplayName("Send message fails while reading response")
  public void sendMessage_Response_Failed2() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    Thread client = spy(sc.client);
    BufferedReader in = mock(BufferedReader.class);
    when(in.readLine()).thenThrow(IOException.class);

    sc.in = in;
    sc.socket = mockSocket;
    sc.client = client;
    String response = sc.sendMessage("Test");

    assertNull(response);
  }

  @Test
  @DisplayName("Send message succeeds with response 'Exit' and stream close fails")
  public void sendMessage_Success_Exit_StreamClose_Failed() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    Thread client = spy(sc.client);
    BufferedReader in = mock(BufferedReader.class);
    DataOutputStream out = mock(DataOutputStream.class);

    doThrow(IOException.class).when(mockSocket).close();
    when(in.readLine()).thenReturn("Exit");

    sc.in = in;
    sc.out = out;
    sc.socket = mockSocket;
    sc.client = client;
    String response = sc.sendMessage("Test");

    assertEquals("Exit", response);
    verify(out).close();
    verify(in).close();
  }

  @Test
  @DisplayName("Send message succeeds with response 'Exit'")
  public void sendMessage_Success_Exit() throws IOException {
    SocketClient sc = spy(SocketClient.class);
    Socket mockSocket = mock(Socket.class);
    Thread client = spy(sc.client);
    BufferedReader in = mock(BufferedReader.class);
    DataOutputStream out = mock(DataOutputStream.class);

    when(in.readLine()).thenReturn("Exit");

    sc.in = in;
    sc.out = out;
    sc.socket = mockSocket;
    sc.client = client;
    String response = sc.sendMessage("Test");

    assertEquals("Exit", response);
    verify(out).close();
    verify(in).close();
    verify(mockSocket).close();
  }

  @Test
  @DisplayName("Shutdown")
  public void shutdown() throws Exception {
    SocketClient sc = spy(SocketClient.class);

    when(sc.sendMessage(eq("Exit"))).thenReturn("");

    sc.shutdown();

    verify(sc).sendMessage(eq("Exit"));
  }

  @Test
  @DisplayName("Run when connect failed and max retries exceeded")
  public void run_Connect_Failed_Retries_Exceeded() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);

    doThrow(IOException.class).when(sc).connect();

    SocketClient.MAX_RETRIES_SOCKET_CLIENT = 0;
    sc.client.start();
    sc.client.join();

    verify(sc, times(1)).connect();
  }

  @Test
  @DisplayName("Run when connect failed and cooldown interrupted")
  public void run_Connect_Failed_Cooldown_Interrupted() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);

    doThrow(IOException.class).when(sc).connect();

    SocketClient.MAX_RETRIES_SOCKET_CLIENT = 1;
    sc.client.start();
    new Thread(() -> sc.client.interrupt()).start();
    sc.client.join();

    verify(sc, times(2)).connect();
  }

  @Test
  @DisplayName("Run when connect succeeds, wait is interrupted and streams close fails")
  public void run_Wait_Interrupted_StreamClose_Failed() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);
    BufferedReader in = mock(BufferedReader.class);
    Socket socket = mock(Socket.class);

    doNothing().when(sc).connect();
    doThrow(IOException.class).when(socket).close();

    sc.out = out;
    sc.in = in;
    sc.socket = socket;

    sc.client.start();
    new Thread(() -> sc.client.interrupt()).start();
    sc.client.join();

    verify(sc, times(1)).connect();
    verify(out).close();
    verify(in).close();
    verify(sc, never()).processMessage();
  }

  @Test
  @DisplayName("Run when connect succeeds, wait is interrupted")
  public void run_Wait_Interrupted() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);
    BufferedReader in = mock(BufferedReader.class);
    Socket socket = mock(Socket.class);

    doNothing().when(sc).connect();

    sc.out = out;
    sc.in = in;
    sc.socket = socket;

    sc.client.start();
    new Thread(() -> sc.client.interrupt()).start();
    sc.client.join();

    verify(sc, times(1)).connect();
    verify(out).close();
    verify(in).close();
    verify(socket).close();
    verify(sc, never()).processMessage();
  }

  @Test
  @DisplayName("Run when connect succeeds, process message no exit")
  public void run_ProcessMessage_NoExit() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);
    BufferedReader in = mock(BufferedReader.class);
    Socket socket = mock(Socket.class);

    doNothing().when(sc).connect();
    doReturn(true).when(sc).processMessage();

    sc.out = out;
    sc.in = in;
    sc.socket = socket;

    sc.client.start();
    try {
      Thread.sleep(100);
      synchronized (sc.client) {
        sc.client.notify();
      }
      Thread.sleep(100);
      sc.client.interrupt();
    } catch (InterruptedException ignored) {
    }
    sc.client.join();

    verify(sc, times(1)).connect();
    verify(out).close();
    verify(in).close();
    verify(socket).close();
    verify(sc).processMessage();
  }

  @Test
  @DisplayName("Run when connect succeeds, process message exit")
  public void run_ProcessMessage_Exit() throws IOException, InterruptedException {
    SocketClient sc = spy(SocketClient.class);
    DataOutputStream out = mock(DataOutputStream.class);
    BufferedReader in = mock(BufferedReader.class);
    Socket socket = mock(Socket.class);

    doNothing().when(sc).connect();
    doReturn(false).when(sc).processMessage();

    sc.out = out;
    sc.in = in;
    sc.socket = socket;

    sc.client.start();
    try {
      Thread.sleep(100);
      synchronized (sc.client) {
        sc.client.notify();
      }
      Thread.sleep(100);
      sc.client.interrupt();
    } catch (InterruptedException ignored) {
    }
    sc.client.join();

    verify(sc, times(1)).connect();
    verify(out).close();
    verify(in).close();
    verify(socket).close();
    verify(sc).processMessage();
  }
}
