package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import com.rmac.RMAC;
import com.rmac.comms.Message;
import com.rmac.comms.Socket;
import com.rmac.utils.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class TerminalTest {

  @Test
  @DisplayName("Create thread")
  public void create_Thread() {
    Socket mockSocket = mock(Socket.class);
    Terminal terminal = spy(new Terminal("abc", mockSocket, new Pair<>(100, 100)));

    assertEquals(State.NEW, terminal.thread.getState());
  }

  @Test
  @DisplayName("Start terminal")
  public void start_Terminal() {
    Thread mockThread = mock(Thread.class);
    Socket mockSocket = mock(Socket.class);
    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 100));
    terminal.thread = mockThread;

    terminal.start();

    verify(mockThread).start();
  }

  @Test
  @DisplayName("Run when successful with emit close event")
  public void run_Success_Emit_Close() throws IOException, InterruptedException {
    Socket mockSocket = mock(Socket.class);
    PtyProcessBuilder mockBuilder = mock(PtyProcessBuilder.class);
    PtyProcess mockProcess = mock(PtyProcess.class);
    InputStream mockIS = mock(InputStream.class);
    Config mockConfig = mock(Config.class);

    doReturn(mockProcess).when(mockBuilder).start();
    doReturn(mockIS).when(mockProcess).getInputStream();
    doReturn(10).doReturn(-1).when(mockIS).read(any(byte[].class));
    doReturn(true).when(mockSocket).isOpen();
    doReturn(0).when(mockProcess).waitFor();

    RMAC.config = mockConfig;
    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.builder = mockBuilder;
    terminal.emitClose = true;
    terminal.run();

    verify(mockBuilder).start();
    verify(mockProcess).setWinSize(any(WinSize.class));
    verify(mockSocket, times(2)).emit(any(Message.class));
  }

  @Test
  @DisplayName("Run when interrupted")
  public void run_Failed_Interrupted() throws IOException, InterruptedException {
    Socket mockSocket = mock(Socket.class);
    PtyProcessBuilder mockBuilder = mock(PtyProcessBuilder.class);
    PtyProcess mockProcess = mock(PtyProcess.class);
    InputStream mockIS = mock(InputStream.class);
    Config mockConfig = mock(Config.class);

    doReturn(mockProcess).when(mockBuilder).start();
    doReturn(mockIS).when(mockProcess).getInputStream();
    doReturn(10).doReturn(-1).when(mockIS).read(any(byte[].class));
    doReturn(true).when(mockSocket).isOpen();
    doThrow(InterruptedException.class).when(mockProcess).waitFor();

    RMAC.config = mockConfig;
    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.builder = mockBuilder;
    terminal.emitClose = false;
    terminal.run();

    verify(mockBuilder).start();
    verify(mockProcess).setWinSize(any(WinSize.class));
    verify(mockSocket, times(1)).emit(any(Message.class));
  }

  @Test
  @DisplayName("Shutdown when no scheduler")
  public void shutdown_NoScheduler() {
    Socket mockSocket = mock(Socket.class);
    PtyProcess mockProcess = mock(PtyProcess.class);

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.process = mockProcess;
    mockSocket.terminals = new HashMap<>();
    mockSocket.terminals.put("abc", terminal);
    terminal.killScheduler = null;

    terminal.shutdown(false);

    assertFalse(terminal.emitClose);
    assertEquals(mockSocket.terminals.size(), 0);
    verify(mockProcess).destroy();
  }

  @Test
  @DisplayName("Shutdown when scheduler is terminated")
  public void shutdown_Scheduler_Terminated() {
    Socket mockSocket = mock(Socket.class);
    PtyProcess mockProcess = mock(PtyProcess.class);
    ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);

    doReturn(true).when(mockScheduler).isTerminated();

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.process = mockProcess;
    terminal.killScheduler = mockScheduler;
    mockSocket.terminals = new HashMap<>();
    mockSocket.terminals.put("abc", terminal);

    terminal.shutdown(false);

    assertFalse(terminal.emitClose);
    assertEquals(mockSocket.terminals.size(), 0);
    verify(mockProcess).destroy();
  }

  @Test
  @DisplayName("Write when process is null")
  public void write_NoProcess() throws IOException {
    Socket mockSocket = mock(Socket.class);

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.process = null;
    terminal.write("Test data");
  }

  @Test
  @DisplayName("Write when data is null")
  public void write_NoData() throws IOException {
    Socket mockSocket = mock(Socket.class);
    PtyProcess mockProcess = mock(PtyProcess.class);

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.process = mockProcess;
    terminal.write(null);

    verify(mockProcess, times(0)).getOutputStream();
  }

  @Test
  @DisplayName("Write succeeded")
  public void write_Success() throws IOException {
    Socket mockSocket = mock(Socket.class);
    PtyProcess mockProcess = mock(PtyProcess.class);
    OutputStream mockOS = mock(OutputStream.class);

    doReturn(mockOS).when(mockProcess).getOutputStream();

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.process = mockProcess;
    terminal.write("test data");

    verify(mockProcess, times(1)).getOutputStream();
    verify(mockOS).write(eq("test data".getBytes()));
  }

  @Test
  @DisplayName("Orphaned")
  public void orphaned() {
    Socket mockSocket = mock(Socket.class);
    ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
    MockedStatic<Executors> mockedExecutors = mockStatic(Executors.class);

    mockedExecutors.when(() -> Executors.newScheduledThreadPool(eq(1))).thenReturn(mockScheduler);

    Terminal terminal = new Terminal("abc", mockSocket, new Pair<>(100, 200));
    terminal.orphaned();

    verify(mockScheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));

    mockedExecutors.close();
  }
}
