package com.rmac.core;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.rmac.comms.Message;
import com.rmac.comms.Socket;
import com.rmac.utils.PipeStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Terminal {

  public Thread thread;
  public PtyProcessBuilder builder;
  public PtyProcess process;
  public String id;
  public Socket socket;

  public Terminal(String id, Socket socket) {
    this.id = id;
    this.socket = socket;
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
      // process.setWinSize(new WinSize(255, 16));

      OutputStream os = process.getOutputStream();
      InputStream is = process.getInputStream();

      byte[] buffer = new byte[1024];
      int len;
      while ((len = is.read(buffer)) >= 0) {
        // os.write(buffer, 0, len);
        socket.emit(new Message(
            "terminal:data",
            this.id,
            new String(Arrays.copyOfRange(buffer, 0, len), StandardCharsets.US_ASCII)
        ));
      }

      /*PipeStream osPipe = PipeStream.make(System.in, os);
      PipeStream isPipe = PipeStream.make(is, System.out);

      osPipe.start();
      isPipe.start();*/

      process.waitFor();
    } catch (InterruptedException | IOException e) {
      log.warn("Terminal Stopped", e);
    }
  }

  public void shutdown() {
    process.destroy();
  }

  public void write(String data) throws IOException {
    if (Objects.isNull(this.process) || Objects.isNull(data)) {
      return;
    }

    this.process.getOutputStream().write(data.getBytes());
  }
}
