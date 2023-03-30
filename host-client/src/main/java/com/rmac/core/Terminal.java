package com.rmac.core;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import com.rmac.comms.Message;
import com.rmac.comms.Socket;
import com.rmac.utils.Pair;
import com.rmac.utils.PipeStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Terminal {

  public Thread thread;
  public PtyProcessBuilder builder;
  public PtyProcess process;
  public String id;
  public Socket socket;
  public Pair<Integer, Integer> initialDimension;

  public ScheduledExecutorService killScheduler;

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

  public void createThread() {
    thread = new Thread(this::run, "Terminal");
  }

  public void start() {
    this.thread.start();
  }

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
      socket.terminals.remove(this.id);
      log.info("Terminal Closed");
    }
  }

  public void shutdown(boolean emit) {
    if (emit) {
      this.socket.emit(new Message("terminal:close", this.id, null));
    }
    if (Objects.nonNull(this.killScheduler) && !this.killScheduler.isTerminated()) {
      this.killScheduler.shutdownNow();
    }
    process.destroy();

  }

  public void write(String data) throws IOException {
    if (Objects.isNull(this.process) || Objects.isNull(data)) {
      return;
    }

    this.process.getOutputStream().write(data.getBytes());
  }

  public void orphaned() {
    this.killScheduler = Executors.newScheduledThreadPool(1);
    killScheduler.schedule(() -> {
      this.shutdown(false);
      killScheduler.shutdown();
    }, 60, TimeUnit.SECONDS);
  }
}
