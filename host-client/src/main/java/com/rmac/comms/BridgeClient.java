package com.rmac.comms;

import com.rmac.RMAC;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BridgeClient {

  public Thread thread;
  public Socket socket;
  public boolean isConnecting = false;

  public static int MAX_RETRIES = 5;
  public static int ATTEMPT = 0;
  public static int CONNECT_COOLDOWN = 3000;
  public static int RECONNECT_COOLDOWN = 90000;

  public BridgeClient() {
    this.thread = new Thread(this::run, "BridgeClient");

    // Connectivity.onChange(this::networkHandler);
  }

  /*public void networkHandler(boolean state) {
    if (state) {
      if (State.WAITING == this.thread.getState()) {
        synchronized (this.thread) {
          this.thread.notify();
        }
      }
    }
  }*/

  public void start() {
    this.thread.start();
  }

  public void run() {
    try {
      this.socket = new Socket(RMAC.config.getBridgeServerUrl());
      this.socket.connect();
    } catch (Exception e) {
      log.error("Error while connecting to bridging server", e);
    }
  }

  /*public void coolOffAndRetry() {
    try {
      synchronized (this.thread) {
        this.thread.wait(RECONNECT_COOLDOWN);
      }
    } catch (InterruptedException e) {
      log.error("Could not wait for reconnect cooldown");
    }
    this.socket = null;
    this.out = null;
    this.in = null;
    this.thread = new Thread(this::run);
    this.start();
  }*/

  /*public boolean openConnection(int cooldown) {
    if (Objects.nonNull(this.socket) && this.socket.isConnected()) {
      return true;
    }

    isConnecting = true;
    ATTEMPT = 0;
    for (int i = 0; i <= MAX_RETRIES; i++) {
      try {
        this.connect();
        isConnecting = false;
        return true;
      } catch (Exception e) {
        log.warn("Could not connect to remote socket");
        if (i >= MAX_RETRIES) {
          log.error("Max retries exceeded");
          isConnecting = false;
          return false;
        }
        try {
          synchronized (this.thread) {
            this.thread.wait(cooldown);
          }
        } catch (InterruptedException ex) {
          log.error("Could not wait for cooldown");
        }
      }
    }
    isConnecting = false;
    return false;
  }*/

  /*public void connect() throws IOException {
    ATTEMPT += 1;
    socket = this.getSocket();
    log.info("Connected to remote socket");
    ATTEMPT = 0;
  }*/

  /*public Socket getSocket() throws IOException {
    socket = new Socket(RMAC.config.getSocketServerUrlHost(), RMAC.config.getSocketServerUrlPort());
    socket.setKeepAlive(true);
    out = new PrintStream(socket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    return socket;
  }*/

  /*public void emit(String event, String data) {
    out.print(event + ";:" + data);
    out.flush();
  }*/

  /*public void emit(String event, String id, String data) {
    out.print(event + ";" + id + ":" + data);
    out.flush();
  }*/
}
