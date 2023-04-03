package com.rmac.core;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import com.rmac.comms.Message;
import com.rmac.comms.Socket;
import com.rmac.utils.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents an open terminal connection between this RMAC Host, RMAC Bridging Server and the RMAC
 * Console.
 */
@Slf4j
public class Terminal {

  public Thread thread;
  /**
   * The pseudo-terminal process builder.
   */
  public PtyProcessBuilder builder;
  /**
   * The pseudo-terminal process.
   */
  public PtyProcess process;
  /**
   * Identifier representing a unique terminal and its requester (RMAC Console) of the form
   * '{ConsoleId}:{TerminalId}'.
   */
  public String id;
  /**
   * The socket connection between this RMAC Host and the RMAC Bridging Server.
   */
  public Socket socket;
  /**
   * The initial requested dimension of the pseudo-terminal from the RMAC Console.
   */
  public Pair<Integer, Integer> initialDimension;
  /**
   * Whether to signal a 'terminal:close' message to RMAC Console in-case of process termination.
   */
  public boolean emitClose = true;

  /**
   * Scheduler to kill this terminal process on the host if this pseudo-terminal connection is
   * abandoned.
   */
  public ScheduledExecutorService killScheduler;

  /**
   * Initializes a new pseudo-terminal connection.
   *
   * @param id               The unique identifier representing this terminal and its requester
   *                         (RMAC Console).
   * @param socket           The socket connection.
   * @param initialDimension The initial requested pseudo-terminal dimension.
   */
  public Terminal(String id, Socket socket, Pair<Integer, Integer> initialDimension) {
    this.id = id;
    this.socket = socket;
    this.initialDimension = initialDimension;
    this.createThread();

    Map<String, String> env = new HashMap<>(System.getenv());
    env.put("TERM", "xterm");
    builder = new PtyProcessBuilder()
        .setCommand(new String[]{"powershell.exe"})
        .setEnvironment(env);
  }

  /**
   * Create a thread for terminal process.
   */
  public void createThread() {
    thread = new Thread(this::run, "Terminal");
  }

  /**
   * Start-up the pseudo-terminal.
   */
  public void start() {
    this.thread.start();
  }

  /**
   * Start the pseudo-terminal and indefinitely listen and process the incoming data.
   */
  public void run() {
    try {
      process = builder.start();

      if (Objects.nonNull(this.initialDimension)) {
        process.setWinSize(
            new WinSize(this.initialDimension.getFirst(), this.initialDimension.getSecond()));
      }
      InputStream is = process.getInputStream();

      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) >= 0) {
        socket.emit(new Message(
            "terminal:data",
            this.id,
            new String(Arrays.copyOfRange(buffer, 0, len), StandardCharsets.US_ASCII)
        ));
      }

      process.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warn("Terminal Stopped", e);
    } finally {
      if (this.emitClose && this.socket.isOpen()) {
        this.socket.emit(new Message("terminal:close", this.id, null));
      }
      log.info("Terminal Closed");
    }
  }

  /**
   * Kill this terminal process and close the pseudo-terminal connection.
   *
   * @param emitClose Whether to emit a 'terminal:close' signal back to RMAC Console which requested
   *                  the connection.
   */
  public void shutdown(boolean emitClose) {
    if (Objects.nonNull(this.killScheduler) && !this.killScheduler.isTerminated()) {
      this.killScheduler.shutdownNow();
    }
    socket.terminals.remove(this.id);
    this.emitClose = emitClose;
    this.process.destroy();
  }

  /**
   * Write data to the native terminal process.
   *
   * @param data The data to write.
   * @throws IOException If data write fails.
   */
  public void write(String data) throws IOException {
    if (Objects.isNull(this.process) || Objects.isNull(data)) {
      return;
    }

    this.process.getOutputStream().write(data.getBytes());
  }

  /**
   * Schedules a timer for 1 minute to kill the native terminal process.
   */
  public void orphaned() {
    this.killScheduler = Executors.newScheduledThreadPool(1);
    killScheduler.schedule(() -> {
      this.process.destroy();
      killScheduler.shutdown();
    }, 60, TimeUnit.SECONDS);
  }
}
