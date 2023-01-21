package com.rmac.process;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.process.KeyLog;
import com.rmac.process.KeyLogCommand;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Pair;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.time.Instant;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class KeyLogTest {

  @Test
  @DisplayName("KeyLog initialization when key-logging is configured as disabled")
  public void keyLog_Config_Disabled() throws FileNotFoundException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(false).when(config).getKeyLog();

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = new KeyLog();

    assertEquals(State.NEW, log.thread.getState());
    verify(fs, never()).getWriter(eq(Constants.KEYLOG_LOCATION));
    verify(fs).getNoopWriter();
  }

  @Test
  @DisplayName("KeyLog initialization when key-logging is configured as enabled")
  public void keyLog_Config_Enabled() throws FileNotFoundException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(true).when(config).getKeyLog();

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);
    doNothing().when(log).openFileWriter();

    assertEquals(State.NEW, log.thread.getState());
    verify(fs).getWriter(eq(Constants.KEYLOG_LOCATION));
    verify(fs, never()).getNoopWriter();
  }

  @Test
  @DisplayName("Config change listener when changed prop is not 'KeyLog'")
  public void config_Change_Listener_NoProp() {
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doNothing().when(config).updateConfig();

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);
    doNothing().when(log).openFileWriter();
    config.setProperty("ClientName", "TestName");

    verify(log, never()).resume();
    verify(log, never()).pause();
  }

  @Test
  @DisplayName("Config change listener when config is set to enabled")
  public void config_Change_Listener_Enabled() {
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doNothing().when(config).updateConfig();

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);
    doNothing().when(log).openFileWriter();

    doNothing().when(log).resume();

    config.setProperty("KeyLog", true);

    verify(log).resume();
    verify(log, never()).pause();
  }

  @Test
  @DisplayName("Config change listener when config is set to disabled")
  public void config_Change_Listener_Disabled() {
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doNothing().when(config).updateConfig();

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();
    doNothing().when(log).pause();

    config.setProperty("KeyLog", false);

    verify(log, never()).resume();
    verify(log).pause();
  }

  @Test
  @DisplayName("Start")
  public void start() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    Thread thread = spy(new Thread(() -> {
    }));

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = new KeyLog();
    log.thread = thread;
    log.start();

    verify(thread).start();
  }

  @Test
  @DisplayName("Println with value when newline switch is on and not busy")
  public void println_NewLine_NotBusy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Instant> instant = mockStatic(Instant.class);
    PrintWriter writer = mock(PrintWriter.class);
    Instant mockInstant = mock(Instant.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    doReturn("0000-00-00-00-00-000").when(mockInstant).toString();
    instant.when(Instant::now).thenReturn(mockInstant);

    KeyLog.isNewLine = true;
    log.writer = writer;
    log.isBusy = false;
    log.println("Test value");

    assertTrue(KeyLog.isNewLine);
    verify(writer).println(eq("[0000-00-00-00-00-000] Test value"));
    verify(writer).flush();

    instant.close();
  }

  @Test
  @DisplayName("Println with value when busy")
  public void println_Busy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    KeyLog.isNewLine = false;
    log.isBusy = true;
    log.println("Test value");

    assertTrue(KeyLog.isNewLine);
    assertEquals(1, log.buffer.size());
    assertEquals(KeyLogCommand.PRINTLN_WITH_VALUE, log.buffer.get(0).getFirst());
    assertEquals("Test value", log.buffer.get(0).getSecond());
  }

  @Test
  @DisplayName("Println without value when newline switch is on and not busy")
  public void println_NoValue_NewLine_NotBusy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Instant> instant = mockStatic(Instant.class);
    PrintWriter writer = mock(PrintWriter.class);
    Instant mockInstant = mock(Instant.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    doReturn("0000-00-00-00-00-000").when(mockInstant).toString();
    instant.when(Instant::now).thenReturn(mockInstant);

    KeyLog.isNewLine = true;
    log.writer = writer;
    log.isBusy = false;
    log.println();

    assertTrue(KeyLog.isNewLine);
    verify(writer).println(eq("[0000-00-00-00-00-000]\n"));
    verify(writer).flush();

    instant.close();
  }

  @Test
  @DisplayName("Println without value when busy")
  public void println_NoValue_Busy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    KeyLog.isNewLine = false;
    log.isBusy = true;
    log.println();

    assertTrue(KeyLog.isNewLine);
    assertEquals(1, log.buffer.size());
    assertEquals(KeyLogCommand.PRINTLN_WITH_VALUE, log.buffer.get(0).getFirst());
    assertEquals("", log.buffer.get(0).getSecond());
  }

  @Test
  @DisplayName("Print when newline switch is on and not busy")
  public void print_NewLine_NotBusy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Instant> instant = mockStatic(Instant.class);
    PrintWriter writer = mock(PrintWriter.class);
    Instant mockInstant = mock(Instant.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    doReturn("0000-00-00-00-00-000").when(mockInstant).toString();
    instant.when(Instant::now).thenReturn(mockInstant);

    KeyLog.isNewLine = true;
    log.writer = writer;
    log.isBusy = false;
    log.print("Test value");

    assertFalse(KeyLog.isNewLine);
    verify(writer).print(eq("[0000-00-00-00-00-000] Test value"));
    verify(writer).flush();

    instant.close();
  }

  @Test
  @DisplayName("Print when busy")
  public void print_Busy() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);

    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    KeyLog.isNewLine = false;
    log.isBusy = true;
    log.print("Test value");

    assertFalse(KeyLog.isNewLine);
    assertEquals(1, log.buffer.size());
    assertEquals(KeyLogCommand.PRINT, log.buffer.get(0).getFirst());
    assertEquals("Test value", log.buffer.get(0).getSecond());
  }

  @Test
  @DisplayName("Open file writer fails")
  public void openFileWriter_Failed() throws FileNotFoundException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    RMAC.fs = fs;
    RMAC.config = config;
    KeyLog log = spy(KeyLog.class);

    doThrow(FileNotFoundException.class).when(fs).getWriter(eq("X:\\test\\Live\\key.txt"));

    log.openFileWriter();
  }

  @Test
  @DisplayName("Open file writer succeeds")
  public void openFileWriter_Success() throws FileNotFoundException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;
    doReturn(false).when(config).getKeyLog();
    KeyLog log = spy(KeyLog.class);

    doReturn(writer).when(fs).getWriter(eq("X:\\test\\Live\\key.txt"));

    log.openFileWriter();

    verify(fs).getWriter(eq("X:\\test\\Live\\key.txt"));
    assertEquals(writer, log.writer);
  }

  @Test
  @DisplayName("Open noop file writer")
  public void openNoopWriter() throws FileNotFoundException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    PrintWriter noopWriter = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;
    doReturn(true).when(config).getKeyLog();
    doReturn(writer).when(fs).getWriter(anyString());
    KeyLog log = spy(KeyLog.class);

    doReturn(noopWriter).when(fs).getNoopWriter();

    log.openNoopWriter();

    verify(writer).flush();
    verify(writer).close();
    verify(fs).getNoopWriter();
    assertEquals(noopWriter, log.writer);
  }

  @Test
  @DisplayName("Run interrupted")
  public void run_Interrupted() throws IOException, InterruptedException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    FileUploader uploader = mock(FileUploader.class);
    RMAC.fs = fs;
    RMAC.config = config;
    doReturn(true).when(config).getKeyLog();
    doReturn(writer).when(fs).getWriter(anyString());
    KeyLog log = spy(KeyLog.class);

    doReturn(-1).when(config).getKeyLogUploadInterval();

    log.start();
    log.thread.join();

    verify(fs, never()).move(anyString(), anyString(), any());
    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Run when key-logging is disabled")
  public void run_Config_Disabled() throws IOException, InterruptedException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    FileUploader uploader = mock(FileUploader.class);
    RMAC.fs = fs;
    RMAC.config = config;
    doReturn(true).when(config).getKeyLog();
    doReturn(writer).when(fs).getWriter(anyString());
    KeyLog log = spy(KeyLog.class);

    doReturn(false).when(config).getKeyLog();
    doReturn(1).doReturn(-1).when(config).getKeyLogUploadInterval();

    log.start();
    log.thread.join();

    verify(fs, never()).move(anyString(), anyString(), any());
    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Run when upload fails and writer already closed")
  public void run_Upload_Failed_Writer_Closed() throws IOException, InterruptedException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Constants.TEMP_LOCATION = "X:\\temp";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;
    doReturn(true).when(config).getKeyLog();
    doReturn(writer).when(fs).getWriter(anyString());
    KeyLog log = spy(KeyLog.class);

    doReturn(1).doReturn(-1).when(config).getKeyLogUploadInterval();
    doThrow(IOException.class).when(fs).move(anyString(), anyString(), any());
    doNothing().doThrow(RuntimeException.class).when(writer).flush();
    doNothing().when(log).openFileWriter();
    doNothing().when(log).unloadBuffer();

    log.start();
    log.thread.join();

    verify(writer, times(2)).flush();
    verify(log).openFileWriter();
    verify(log).unloadBuffer();
    assertFalse(log.isBusy);
  }

  @Test
  @DisplayName("Run succeeds")
  public void run_Success() throws IOException, InterruptedException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Constants.TEMP_LOCATION = "X:\\temp";
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    PrintWriter newWriter = mock(PrintWriter.class);
    FileUploader uploader = mock(FileUploader.class);
    RMAC.fs = fs;
    RMAC.config = config;
    RMAC.uploader = uploader;

    doReturn(true).when(config).getKeyLog();
    doReturn(writer).doReturn(newWriter).when(fs).getWriter(anyString());

    KeyLog log = spy(KeyLog.class);

    doReturn(1).doReturn(-1).when(config).getKeyLogUploadInterval();
    doNothing().when(writer).flush();
    doNothing().when(log).openFileWriter();
    doNothing().when(log).unloadBuffer();

    log.start();
    log.thread.join();

    verify(writer).flush();
    verify(newWriter).flush();
    verify(log, never()).openFileWriter();
    verify(log).unloadBuffer();
    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.KEY));
    assertFalse(log.isBusy);
  }

  @Test
  @DisplayName("Unload buffer")
  public void unloadBuffer() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;

    doReturn(true).when(config).getKeyLog();

    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();
    doNothing().when(log).println(anyString());
    doNothing().when(log).println();
    doNothing().when(log).print(anyString());

    log.buffer.add(new Pair<>(KeyLogCommand.PRINT, "Test 1"));
    log.buffer.add(new Pair<>(KeyLogCommand.PRINTLN_WITH_VALUE, "Test 2"));
    log.unloadBuffer();

    verify(log).print(eq("Test 1"));
    verify(log).println(eq("Test 2"));
    assertEquals(0, log.buffer.size());
  }

  @Test
  @DisplayName("Shutdown when writer is not initialized")
  public void shutdown_NoWriter() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    RMAC.fs = fs;
    RMAC.config = config;
    Thread thread = mock(Thread.class);

    doReturn(true).when(config).getKeyLog();

    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();

    log.writer = null;
    log.thread = thread;
    log.shutdown();

    verify(thread).interrupt();
  }

  @Test
  @DisplayName("Shutdown when writer is initialized")
  public void shutdown_Writer() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;
    Thread thread = mock(Thread.class);

    doReturn(true).when(config).getKeyLog();

    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();

    log.writer = writer;
    log.thread = thread;
    log.shutdown();

    verify(thread).interrupt();
    verify(writer).close();
  }

  @Test
  @DisplayName("Pause")
  public void pause() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    RMAC.fs = fs;
    RMAC.config = config;

    doReturn(true).when(config).getKeyLog();

    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();
    doNothing().when(log).openNoopWriter();

    log.pause();

    verify(log).openNoopWriter();
    assertFalse(log.isBusy);
  }

  @Test
  @DisplayName("Resume")
  public void resume() {
    Config config = mock(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);
    RMAC.fs = fs;
    RMAC.config = config;

    doReturn(true).when(config).getKeyLog();

    KeyLog log = spy(KeyLog.class);

    doNothing().when(log).openFileWriter();
    doNothing().when(log).openNoopWriter();

    log.writer = writer;
    log.resume();

    verify(writer).close();
    verify(log).openFileWriter();
    assertFalse(log.isBusy);
  }
}
