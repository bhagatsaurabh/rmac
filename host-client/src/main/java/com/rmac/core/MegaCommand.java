package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for running/handling MEGA cli client commands as separate processes.
 */
@Slf4j
public class MegaCommand {

  public BufferedReader out;
  public BufferedWriter in;
  public Process process;
  public AtomicBoolean isAPIError;
  public Thread asyncReader;
  public String[] args;

  /**
   * Create a process to run given MEGA cli client command.
   *
   * @param command Command to execute.
   */
  public MegaCommand(String... command) {
    this.args = Stream
        .concat(
            Arrays.stream(new String[]{"\"" + Constants.MEGACLIENT_LOCATION + "\""}),
            Arrays.stream(command)
        )
        .toArray(String[]::new);
  }

  /**
   * Starts a new process for MEGAcmd to execute the specified command.
   *
   * @throws IOException If command execution fails.
   */
  public void execute() throws IOException {
    ProcessBuilder builder = new ProcessBuilder(args);
    builder.directory(new File(Constants.MEGACMD_LOCATION));
    process = this.startProcess(builder);
    out = RMAC.fs.getReader(process.getInputStream());
    in = RMAC.fs.getWriter(process.getOutputStream());
    PipeStream err = PipeStream.make(process.getErrorStream(), new NoopOutputStream());
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
      if (Objects.nonNull(in)) {
        in.close();
      }
      if (Objects.nonNull(out)) {
        out.close();
      }
    } catch (IOException e) {
      log.error("Could not close MEGA command streams", e);
    }
  }

  /**
   * Run the specified MEGAcmd command.
   *
   * @param command The command to execute.
   * @return The MegaCommand instance running the command.
   * @throws IOException If command execution fails.
   */
  public static MegaCommand run(String[] command) throws IOException {
    MegaCommand cmd = new MegaCommand(command);
    cmd.execute();
    return cmd;
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
