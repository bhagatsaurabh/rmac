package com.rmac.core;

import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for running/handling MEGA cli client commands as separate processes.
 */
@Slf4j
public class MegaCommand {

  private final BufferedReader out;
  private final BufferedWriter in;
  public Process process;
  public AtomicBoolean isAPIError;
  private final Thread asyncReader;

  /**
   * Create a process to run given MEGA cli client command.
   *
   * @param command Command to execute.
   * @throws IOException when command cannot be executed or if it fails.
   */
  public MegaCommand(String... command) throws IOException {
    String[] args = Stream
        .concat(
            Arrays.stream(new String[]{"\"" + Constants.MEGACLIENT_LOCATION + "\""}),
            Arrays.stream(command)
        )
        .toArray(String[]::new);
    ProcessBuilder builder = new ProcessBuilder(args);
    builder.directory(new File(Constants.MEGACMD_LOCATION));
    process = builder.start();
    out = new BufferedReader(new InputStreamReader(process.getInputStream()));
    in = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    PipeStream err = new PipeStream(process.getErrorStream(), new NoopOutputStream());
    err.start();

    isAPIError = new AtomicBoolean(false);
    asyncReader = new Thread(() -> {
      String curr;
      try {
        while (true) {
          if ((curr = out.readLine()) != null && curr.contains("API:err")) {
            isAPIError.set(true);
            break;
          }
        }
      } catch (IOException e) {
        log.error("Could not read process output", e);
      }
    });
    asyncReader.start();
  }

  /**
   * Stop the process running the specified command.
   */
  public void stop() {
    asyncReader.interrupt();
    process.destroy();

    try {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    } catch (IOException e) {
      log.error("Could not close MEGA command streams", e);
    }
  }
}
