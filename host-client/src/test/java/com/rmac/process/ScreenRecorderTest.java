package com.rmac.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.process.ScreenRecorder;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.State;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ScreenRecorderTest {

  @Test
  @DisplayName("ScreenRecorder initialization")
  public void screenRecorder() {
    ScreenRecorder sr = new ScreenRecorder();

    assertEquals(State.NEW, sr.thread.getState());
  }

  @Test
  @DisplayName("Start")
  public void start() {
    Thread t = mock(Thread.class);

    ScreenRecorder sr = new ScreenRecorder();
    sr.thread = t;
    sr.start();

    verify(t).start();
  }

  @Test
  @DisplayName("Run when disabled and idling interrupted")
  public void run_Disabled_Interrupted() throws IOException, InterruptedException {
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Config config = mock(Config.class);

    doReturn(false).when(config).getScreenRecording();

    RMAC.config = config;
    ScreenRecorder.IDLE_TIME = -1;
    sr.run();

    verify(sr, never()).getFFMPEGProcessBuilder(anyString());
  }

  @Test
  @DisplayName("Run when enabled and process fails")
  public void run_Process_Failed() throws IOException, InterruptedException {
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Config config = mock(Config.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    FileUploader uploader = mock(FileUploader.class);

    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00-000");
    doReturn(false, true).when(config).getScreenRecording();
    doThrow(IOException.class).when(sr).getFFMPEGProcessBuilder(anyString());

    RMAC.config = config;
    RMAC.uploader = uploader;
    ScreenRecorder.IDLE_TIME = 1;
    sr.run();

    verify(uploader, never()).uploadFile(anyString(), any());

    utils.close();
  }

  @Test
  @DisplayName("Run succeeds")
  public void run_Success() throws IOException, InterruptedException {
    Constants.CURRENT_LOCATION = "X:\\test\\Live";
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Config config = mock(Config.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    FileUploader uploader = mock(FileUploader.class);
    ProcessBuilder builder = mock(ProcessBuilder.class);
    Process proc = mock(Process.class);

    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00-000");
    doReturn(true, false).when(config).getScreenRecording();
    doReturn(builder).when(sr).getFFMPEGProcessBuilder(anyString());
    doReturn(proc).when(builder).start();
    doReturn(0).when(proc).waitFor();

    RMAC.config = config;
    RMAC.uploader = uploader;
    ScreenRecorder.IDLE_TIME = -1;
    sr.run();

    verify(uploader).uploadFile(eq("X:\\test\\Live\\0000-00-00-00-00-00-000.mkv"),
        eq(ArchiveFileType.SCREEN));

    utils.close();
  }

  @Test
  @DisplayName("Shutdown when current process is null and command execution fails")
  public void shutdown_Failed() throws IOException {
    ScreenRecorder sr = spy(ScreenRecorder.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);

    doThrow(IOException.class).when(mockRuntime).exec(anyString());
    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    ScreenRecorder.currFFMPEGProc = null;
    sr.shutdown();

    runtime.close();
  }

  @Test
  @DisplayName("Shutdown succeeds")
  public void shutdown_Success() throws IOException, InterruptedException {
    ScreenRecorder sr = spy(ScreenRecorder.class);
    MockedStatic<Runtime> runtime = mockStatic(Runtime.class);
    Runtime mockRuntime = mock(Runtime.class);
    Process proc = mock(Process.class);

    doReturn(0).when(proc).waitFor();
    doReturn(proc).when(mockRuntime).exec(anyString());
    runtime.when(Runtime::getRuntime).thenReturn(mockRuntime);

    ScreenRecorder.currFFMPEGProc = proc;
    sr.shutdown();

    verify(proc).destroy();

    runtime.close();
  }

  @Test
  @DisplayName("Get FFMPEG process builder when active audio recording is disabled and mic's not active")
  public void getFFMPEGProcessBuilder_NoActiveAudio_NoMicActive()
      throws IOException, InterruptedException {
    Constants.FFMPEG_LOCATION = "X:\\test\\RMAC\\ffmpeg.exe";
    Constants.CURRENT_LOCATION = "X:\\test\\Live";
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Config config = mock(Config.class);

    doReturn(true).when(config).getAudioRecording();
    doReturn(false).when(config).getActiveAudioRecording();
    doReturn(25).when(config).getFPS();
    doReturn("00:00:00").when(config).getVideoDurationFormatted();
    doReturn("TestMic").when(sr).getDefaultMicName();
    doReturn(false).when(sr).isMicActive(eq("TestMic"));

    RMAC.config = config;
    sr.getFFMPEGProcessBuilder("test.mkv");
  }

  @Test
  @DisplayName("Get FFMPEG process builder when active audio recording is disabled and mic's active")
  public void getFFMPEGProcessBuilder_NoActiveAudio_MicActive()
      throws IOException, InterruptedException {
    Constants.FFMPEG_LOCATION = "X:\\test\\RMAC\\ffmpeg.exe";
    Constants.CURRENT_LOCATION = "X:\\test\\Live";
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Config config = mock(Config.class);

    doReturn(true).when(config).getAudioRecording();
    doReturn(false).when(config).getActiveAudioRecording();
    doReturn(25).when(config).getFPS();
    doReturn("00:00:00").when(config).getVideoDurationFormatted();
    doReturn("TestMic").when(sr).getDefaultMicName();
    doReturn(true).when(sr).isMicActive(eq("TestMic"));

    RMAC.config = config;
    sr.getFFMPEGProcessBuilder("test.mkv");
  }

  @Test
  @DisplayName("Get default mic name")
  public void getDefaultMicName() throws IOException, InterruptedException {
    Constants.RUNTIME_LOCATION = "X:\\test\\Live";
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Process proc = mock(Process.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    InputStream is = mock(InputStream.class);
    InputStream es = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(es).when(proc).getErrorStream();
    doReturn(proc).when(sr).startProcess(any());
    doReturn("TestMic", (Object) null).when(reader).readLine();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn(0).when(proc).waitFor();

    RMAC.fs = fs;
    String result = sr.getDefaultMicName();

    assertEquals("TestMic", result);
  }

  @Test
  @DisplayName("Is mic active")
  public void isMicActive() throws IOException, InterruptedException {
    Constants.RUNTIME_LOCATION = "X:\\test\\Live";
    Constants.RMAC_DLL_LOCATION = "X:\\test\\Live\\rmac-native.dll";
    ScreenRecorder sr = spy(ScreenRecorder.class);
    Process proc = mock(Process.class);
    BufferedReader reader = mock(BufferedReader.class);
    BufferedWriter writer = mock(BufferedWriter.class);
    InputStream is = mock(InputStream.class);
    InputStream es = mock(InputStream.class);
    OutputStream os = mock(OutputStream.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(is).when(proc).getInputStream();
    doReturn(os).when(proc).getOutputStream();
    doReturn(es).when(proc).getErrorStream();
    doReturn(proc).when(sr).startProcess(any());
    doReturn("True", (Object) null).when(reader).readLine();
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn(writer).when(fs).getWriter(any(OutputStream.class));
    doReturn(0).when(proc).waitFor();

    RMAC.fs = fs;
    boolean result = sr.isMicActive("TestMic");

    assertTrue(result);
  }

  @Test
  @DisplayName("Start process")
  public void startProcess() throws IOException {
    ProcessBuilder builder = mock(ProcessBuilder.class);

    ScreenRecorder sr = new ScreenRecorder();
    sr.startProcess(builder);

    verify(builder).start();
  }
}
