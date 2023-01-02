package com.rmac.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.spi.LoggingEvent;
import com.rmac.RMAC;
import com.rmac.core.Archiver;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.commons.support.ReflectionSupport;

public class RMACFileAppenderTest {

  Archiver mockArchiver = mock(Archiver.class);

  @Before
  public void setup() {
    RMAC.archiver = mockArchiver;
    doNothing().when(mockArchiver).moveToArchive(any(), any());
  }

  @Test
  @DisplayName("Max file size")
  public void maxSize() {
    RMACFileAppender appender = new RMACFileAppender();
    appender.setMaxFileSize("4096");

    assertEquals(4096L, appender.getMaxFileSize());
  }

  @Test
  @DisplayName("FileAppender subAppend when eventsPassed is less than max")
  public void subAppend_Threshold_NotReached() {
    RMACFileAppender appender = spy(new RMACFileAppender());
    appender.eventsPassed = 0;
    appender.maxEventsBeforeRolloverCheck = 10;

    appender.subAppend(new LoggingEvent());

    assertEquals(1, appender.eventsPassed);
  }

  @Test
  @DisplayName("FileAppender subAppend when eventsPassed >= max and reading file size fails")
  public void subAppend_Threshold_Reached_FileRead_Failed() throws IOException {
    RMACFileAppender appender = spy(new RMACFileAppender());
    appender.eventsPassed = 10;
    appender.maxEventsBeforeRolloverCheck = 10;

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.size(any())).thenThrow(IOException.class);

    appender.subAppend(new LoggingEvent());

    assertEquals(1, appender.eventsPassed);
    verify(appender, never()).rollover();
  }

  @Test
  @DisplayName("FileAppender subAppend when file rollover triggers")
  public void subAppend_Rollover() throws IOException {
    RMACFileAppender appender = spy(new RMACFileAppender());
    appender.eventsPassed = 10;
    appender.maxEventsBeforeRolloverCheck = 10;

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.size(any())).thenReturn(Long.MAX_VALUE);
    doNothing().when(appender).rollover();

    appender.subAppend(new LoggingEvent());

    assertEquals(1, appender.eventsPassed);
    verify(appender).rollover();
  }

  @Test
  @DisplayName("Set file when logfile exists and archiver is initialized")
  public void setFile_LogFile_Archive() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\Live\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.exists(any())).thenReturn(true, false);

    appender.setFile(Constants.LOG_LOCATION);

    verify(mockArchiver).moveToArchive(eq(Constants.LOG_LOCATION), eq(ArchiveFileType.OTHER));
    verify(mockFs, never()).delete(eq(Constants.LOG_LOCATION));
    verify(appender).setFile(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Set file when logfile exists and delete fails")
  public void setFile_LogFile_Delete_Failed() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\Live\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.exists(any())).thenReturn(true, true);
    doThrow(IOException.class).when(mockFs).delete(any());

    appender.setFile(Constants.LOG_LOCATION);

    verify(mockArchiver).moveToArchive(eq(Constants.LOG_LOCATION), eq(ArchiveFileType.OTHER));
    verify(mockFs).delete(eq(Constants.LOG_LOCATION));
    verify(appender).setFile(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Set file when logfile exists and delete succeeds")
  public void setFile_LogFile_Delete_Success() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\Live\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    when(mockFs.exists(any())).thenReturn(true, true);
    doNothing().when(mockFs).delete(any());

    appender.setFile(Constants.LOG_LOCATION);

    verify(mockArchiver).moveToArchive(eq(Constants.LOG_LOCATION), eq(ArchiveFileType.OTHER));
    verify(mockFs).delete(eq(Constants.LOG_LOCATION));
    verify(appender).setFile(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Rollover")
  public void rollOver() throws NoSuchMethodException {
    RMACFileAppender appender = spy(new RMACFileAppender());
    doNothing().when(appender).attemptRollover();
    doNothing().when(appender).attemptOpenFile();
    ReflectionSupport.invokeMethod(
        appender.getClass().getSuperclass().getSuperclass().getDeclaredMethod("closeOutputStream"),
        doNothing().when(appender));

    appender.rollover();

    verify(appender).attemptRollover();
    verify(appender).attemptOpenFile();
  }

  @Test
  @DisplayName("Attempt rollover logfile moving fails, delete fails")
  public void attemptRollover_LogFile_Move_Delete_Failed() throws IOException {
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    doThrow(IOException.class).when(mockFs).move(any(), any(), any());
    doThrow(IOException.class).when(mockFs).delete(any());

    appender.attemptRollover();

    verify(mockArchiver, never()).moveToArchive(any(), any());
  }

  @Test
  @DisplayName("Attempt rollover logfile moving fails, delete succeeds")
  public void attemptRollover_LogFile_Move_Failed_Delete_Success() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\RMAC\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    doThrow(IOException.class).when(mockFs).move(any(), any(), any());
    doNothing().when(mockFs).delete(any());

    appender.attemptRollover();

    verify(mockArchiver, never()).moveToArchive(any(), any());
    verify(mockFs).delete(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Attempt rollover logfile moving succeeds")
  public void attemptRollover_LogFile_Move__Success() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\RMAC\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());

    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    doNothing().when(mockFs).move(any(), any(), any());

    appender.attemptRollover();

    verify(mockArchiver).moveToArchive(any(), any());
    verify(mockFs, never()).delete(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Attempt open file fails")
  public void attemptOpenFile_Failed() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\RMAC\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());
    doThrow(IOException.class).when(appender).openFile(eq(Constants.LOG_LOCATION));

    appender.attemptOpenFile();

    verify(appender).openFile(eq(Constants.LOG_LOCATION));
  }

  @Test
  @DisplayName("Attempt open file succeeds")
  public void attemptOpenFile_Success() throws IOException {
    Constants.LOG_LOCATION = "X:\\test\\RMAC\\log.txt";
    RMACFileAppender appender = spy(new RMACFileAppender());
    doNothing().when(appender).openFile(eq(Constants.LOG_LOCATION));

    appender.attemptOpenFile();

    verify(appender).openFile(eq(Constants.LOG_LOCATION));
  }
}
