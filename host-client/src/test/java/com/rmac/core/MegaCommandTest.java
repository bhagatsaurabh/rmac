package com.rmac.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
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

public class MegaCommandTest {

  @Test
  @DisplayName("MegaCommand initialization")
  public void createMegaCommand() {
    Constants.MEGACLIENT_LOCATION = "X:\\test\\RMAC\\mega\\megaclient.exe";

    MegaCommand cmd = new MegaCommand("test");

    assertArrayEquals(cmd.args, new String[]{"\"X:\\test\\RMAC\\mega\\megaclient.exe\"", "test"});
  }

  @Test(expected = IOException.class)
  @DisplayName("Execute fails")
  public void execute_Failed() throws IOException {
    Constants.MEGACLIENT_LOCATION = "X:\\test\\RMAC\\mega\\megaclient.exe";
    Constants.MEGACMD_LOCATION = "X:\\test\\RMAC\\mega";
    MegaCommand cmd = spy(new MegaCommand("test"));

    doThrow(IOException.class).when(cmd).startProcess(any());

    cmd.execute();
  }

  @Test
  @DisplayName("Execute when reading output fails")
  public void execute_OutputRead_Failed() throws IOException, InterruptedException {
    Constants.MEGACLIENT_LOCATION = "X:\\test\\RMAC\\mega\\megaclient.exe";
    Constants.MEGACMD_LOCATION = "X:\\test\\RMAC\\mega";
    MegaCommand cmd = spy(new MegaCommand("test"));
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    Process proc = mock(Process.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(proc).when(cmd).startProcess(any());
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doThrow(IOException.class).when(reader).readLine();

    RMAC.fs = fs;
    cmd.execute();
    cmd.asyncReader.join();

    assertFalse(cmd.isAPIError.get());

    pipe.close();
  }

  @Test
  @DisplayName("Execute succeeds with error")
  public void execute_Success_With_Error() throws IOException, InterruptedException {
    Constants.MEGACLIENT_LOCATION = "X:\\test\\RMAC\\mega\\megaclient.exe";
    Constants.MEGACMD_LOCATION = "X:\\test\\RMAC\\mega";
    MegaCommand cmd = spy(new MegaCommand("test"));
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    Process proc = mock(Process.class);
    InputStream is = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);

    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);
    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(proc).when(cmd).startProcess(any());
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn("API:err test", (Object) null).when(reader).readLine();

    RMAC.fs = fs;
    cmd.execute();
    cmd.asyncReader.join();

    assertTrue(cmd.isAPIError.get());

    pipe.close();
  }

  @Test
  @DisplayName("Stop when stream close fails")
  public void stop_StreamClose_Failed() throws IOException {
    Thread thread = mock(Thread.class);
    Process proc = mock(Process.class);
    BufferedReader out = mock(BufferedReader.class);
    BufferedWriter in = mock(BufferedWriter.class);

    doThrow(IOException.class).when(in).close();

    MegaCommand cmd = new MegaCommand();
    cmd.asyncReader = thread;
    cmd.process = proc;
    cmd.in = in;
    cmd.out = out;
    cmd.stop();

    verify(thread).interrupt();
    verify(proc).destroy();
    verify(out, never()).close();
  }

  @Test
  @DisplayName("Stop succeeds")
  public void stop_Success() throws IOException {
    Thread thread = mock(Thread.class);
    Process proc = mock(Process.class);
    BufferedReader out = mock(BufferedReader.class);
    BufferedWriter in = mock(BufferedWriter.class);

    MegaCommand cmd = new MegaCommand();
    cmd.asyncReader = thread;
    cmd.process = proc;
    cmd.in = in;
    cmd.out = out;
    cmd.stop();

    verify(thread).interrupt();
    verify(proc).destroy();
    verify(in).close();
    verify(out).close();
  }

  @Test
  @DisplayName("Start process")
  public void startProcess() throws IOException {
    ProcessBuilder builder = mock(ProcessBuilder.class);

    MegaCommand cmd = new MegaCommand();
    cmd.startProcess(builder);

    verify(builder).start();
  }
}
