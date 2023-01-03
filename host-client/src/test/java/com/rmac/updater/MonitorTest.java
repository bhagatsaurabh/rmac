package com.rmac.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.Thread.State;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MonitorTest {

  @Test
  @DisplayName("Initialize monitor")
  public void createMonitor() {
    Monitor monitor = spy(new Monitor());

    assertEquals(State.NEW, monitor.thread.getState());
  }

  @Test
  @DisplayName("Start monitor")
  public void startMonitor() {
    Thread mockThread = spy(new Thread(() -> {
    }));
    Monitor monitor = new Monitor();
    monitor.thread = mockThread;

    monitor.start();

    verify(mockThread).start();
  }

  @Test
  @DisplayName("Health check when connection is not established")
  public void healthCheck_NoConnection() {
    Updater.client = null;

    Monitor monitor = new Monitor();
    boolean result = monitor.healthCheck();

    assertFalse(result);
  }

  @Test
  @DisplayName("Health check succeeds")
  public void healthCheck_Success() {
    SocketClient mockSC = mock(SocketClient.class);
    when(mockSC.sendMessage(any())).thenReturn("Up");
    Updater.client = mockSC;

    Monitor monitor = new Monitor();
    boolean result = monitor.healthCheck();

    assertTrue(result);
  }

  @Test
  @DisplayName("Shutdown")
  public void shutdown() {
    Thread mockThread = mock(Thread.class);
    doNothing().when(mockThread).interrupt();

    Monitor monitor = new Monitor();
    monitor.thread = mockThread;
    monitor.shutdown();

    verify(mockThread).interrupt();
  }

  @Test
  @DisplayName("Run Monitor when health is up and sleep is interrupted")
  public void run_HealthUp_Interrupted() throws InterruptedException {
    Monitor monitor = spy(Monitor.class);
    when(monitor.healthCheck()).thenReturn(true);

    monitor.start();
    new Thread(() -> monitor.thread.interrupt()).start();
    monitor.thread.join();

    assertEquals(Long.MAX_VALUE, monitor.healthCheckFailStart);
  }

  @Test
  @DisplayName("Run Monitor when health is down and threshold is reaches")
  public void run_HealthDown_Threshold_NotReached() throws InterruptedException {
    Monitor monitor = spy(Monitor.class);
    when(monitor.healthCheck()).thenReturn(false);

    monitor.thread.start();
    new Thread(() -> monitor.thread.interrupt()).start();
    monitor.thread.join();

    assertNotEquals(Long.MAX_VALUE, monitor.healthCheckFailStart);
  }

  @Test
  @DisplayName("Run Monitor when health is down, threshold is reached, RMAC restart fails")
  public void run_HealthDown_ThresholdReached_Restart_Failed() throws InterruptedException {
    SocketClient mockSC1 = mock(SocketClient.class);
    Updater.client = mockSC1;

    Clock mockClock = mock(Clock.class);
    when(mockClock.millis()).thenReturn(123456789L, 123456789L + 60000L);

    Updater mockUpdater = mock(Updater.class);
    when(mockUpdater.stopRMAC()).thenReturn(true);
    when(mockUpdater.startRMAC()).thenReturn(false);

    Monitor monitor = spy(Monitor.class);
    monitor.updater = mockUpdater;
    monitor.clock = mockClock;
    when(monitor.healthCheck()).thenReturn(false);

    monitor.thread.start();
    new Thread(() -> monitor.thread.interrupt()).start();
    monitor.thread.join();

    assertNotEquals(Long.MAX_VALUE, monitor.healthCheckFailStart);
    verify(mockSC1).shutdown();
    assertNull(Updater.client);
  }

  @Test
  @DisplayName("Run Monitor when health is down, threshold is reached, RMAC restart succeeds")
  public void run_HealthDown_ThresholdReached_Restart_Success()
      throws InterruptedException, InstantiationException, IllegalAccessException {
    SocketClient mockSC1 = mock(SocketClient.class);
    Updater.client = mockSC1;

    SocketClient mockSC2 = mock(SocketClient.class);

    Clock mockClock = mock(Clock.class);
    when(mockClock.millis()).thenReturn(123456789L, 123456789L + 60000L);

    Updater mockUpdater = mock(Updater.class);
    when(mockUpdater.stopRMAC()).thenReturn(true);
    when(mockUpdater.startRMAC()).thenReturn(true);
    when(mockUpdater.getInstance(eq(SocketClient.class))).thenReturn(mockSC2);

    Monitor monitor = spy(Monitor.class);
    monitor.updater = mockUpdater;
    monitor.clock = mockClock;
    when(monitor.healthCheck()).thenReturn(false);

    monitor.thread.start();
    new Thread(() -> monitor.thread.interrupt()).start();
    monitor.thread.join();

    assertEquals(Long.MAX_VALUE, monitor.healthCheckFailStart);
    verify(mockSC1).shutdown();
    assertEquals(mockSC2, Updater.client);
    verify(mockSC2).start();
  }
}
