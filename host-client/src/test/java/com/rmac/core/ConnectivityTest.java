package com.rmac.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.FileSystem;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ConnectivityTest {

  @Test
  @DisplayName("Check network state failed")
  public void checkNetworkState_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).startProcess(any());

    RMAC.fs = fs;
    boolean result = Connectivity.checkNetworkState();

    assertFalse(result);
  }

  @Test
  @DisplayName("Check network state interrupted")
  public void checkNetworkState_Interrupted() throws IOException, InterruptedException {
    FileSystem fs = mock(FileSystem.class);
    Process proc = mock(Process.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    PipeStream pipeStream = mock(PipeStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(pipeStream);
    doReturn(proc).when(fs).startProcess(any());
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn("false").doReturn(null).when(reader).readLine();
    doThrow(InterruptedException.class).when(proc).waitFor();

    RMAC.fs = fs;
    boolean result = Connectivity.checkNetworkState();

    assertFalse(result);

    pipe.close();
  }

  @Test
  @DisplayName("Check network state succeeds")
  public void checkNetworkState_Success() throws IOException, InterruptedException {
    FileSystem fs = mock(FileSystem.class);
    Process proc = mock(Process.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    PipeStream pipeStream = mock(PipeStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(pipeStream);
    doReturn(proc).when(fs).startProcess(any());
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn("false").doReturn(null).when(reader).readLine();
    doReturn(0).when(proc).waitFor();

    RMAC.fs = fs;
    RMAC.NETWORK_STATE = true;
    boolean result = Connectivity.checkNetworkState();

    assertFalse(result);
    assertFalse(RMAC.NETWORK_STATE);

    pipe.close();
  }

  @Test
  @DisplayName("Check network state when state changes from down to up and client not registered")
  public void checkNetworkState_Down_To_Up_NotRegistered()
      throws IOException, InterruptedException {
    FileSystem fs = mock(FileSystem.class);
    Process proc = mock(Process.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    Archiver archiver = mock(Archiver.class);
    Service service = mock(Service.class);
    PipeStream pipeStream = mock(PipeStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(pipeStream);
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(proc).when(fs).startProcess(any());
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn("true").doReturn(null).when(reader).readLine();
    doReturn(0).when(proc).waitFor();

    RMAC.fs = fs;
    RMAC.archiver = archiver;
    RMAC.service = service;
    RMAC.NETWORK_STATE = false;
    boolean result = Connectivity.checkNetworkState();

    assertTrue(result);
    assertTrue(RMAC.NETWORK_STATE);
    verify(archiver).uploadArchiveAsync();
    verify(service).registerClientAsync();

    pipe.close();
  }
}
