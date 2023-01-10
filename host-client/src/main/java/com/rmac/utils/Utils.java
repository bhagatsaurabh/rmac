package com.rmac.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods.
 */
public class Utils {

  public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");
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
    ProcessBuilder ffmpegCam = new ProcessBuilder(
        "powershell", "-enc", Commands.C_FFMPEG_GET_WEBCAM_SNAP
    );
    ffmpegCam.directory(new File(Constants.RUNTIME_LOCATION));
    Map<String, String> env = ffmpegCam.environment();
    env.put("CAM_FILE_NAME", filePath);

    Process process = ffmpegCam.start();
    PipeStream err = PipeStream.make(process.getErrorStream(), new NoopOutputStream());
    PipeStream out = PipeStream.make(process.getInputStream(), new NoopOutputStream());
    err.start();
    out.start();

    return process;
  }

  public static Thread async(Runnable runnable) {
    Thread t = new Thread(runnable);
    t.start();
    return t;
  }
}
