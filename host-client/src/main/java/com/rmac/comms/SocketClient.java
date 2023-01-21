package com.rmac.comms;

import com.google.gson.Gson;
import com.rmac.RMAC;
import com.rmac.core.Connectivity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.net.Socket;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketClient {

  public static Gson GSON = new Gson();

  public Thread thread;
  public Socket socket;
  PrintStream out;
  BufferedReader in;
  public boolean isConnecting = false;

  public static int MAX_RETRIES = 5;
  public static int ATTEMPT = 0;
  public static int CONNECT_COOLDOWN = 3000;
  public static int RECONNECT_COOLDOWN = 90000;

  public SocketClient() {
    this.thread = new Thread(this::run, "SocketClient");

    Connectivity.onChange(this::networkHandler);
  }

  public void networkHandler(boolean state) {
    if (state) {
      if (State.WAITING == this.thread.getState()) {
        synchronized (this.thread) {
          this.thread.notify();
        }
      }
    }/* else {
      try {
        in.close();
        out.close();
        socket.close();
      } catch (Exception e) {
        log.error("Could not close socket", e);
      }
      try {
        this.thread.join();
      } catch (InterruptedException e) {
        log.error("Could not wait for old socket to die", e);
      }
    }*/
  }

  public void start() {
    this.thread.start();
  }

  public void run() {
    boolean result = this.openConnection(CONNECT_COOLDOWN);

    if (!result && !Connectivity.checkNetworkState()) {
      while (true) {
        try {
          synchronized (this.thread) {
            this.thread.wait();
          }
          break;
        } catch (InterruptedException ignored) {
        }
      }
      this.coolOffAndRetry();
      return;
    } else if (!result) {
      this.coolOffAndRetry();
      return;
    }

    try {
      this.emit("ack", RMAC.config.getClientId());

      String data;
      while ((data = in.readLine()) != null) {
        this.processData(data);
      }

      in.close();
      out.close();
    } catch (Exception e) {
      log.error("Socket client error, disconnected");
    } finally {
      if (Objects.nonNull(socket)) {
        try {
          socket.close();
        } catch (Exception e) {
          log.error("Could not close socket");
        }
      }
    }

    this.coolOffAndRetry();
  }

  public void coolOffAndRetry() {
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
  }

  public boolean openConnection(int cooldown) {
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
  }

  public void connect() throws IOException {
    ATTEMPT += 1;
    socket = this.getSocket();
    log.info("Connected to remote socket");
    ATTEMPT = 0;
  }

  public Socket getSocket() throws IOException {
    socket = new Socket(RMAC.config.getSocketServerUrlHost(), RMAC.config.getSocketServerUrlPort());
    socket.setKeepAlive(true);
    out = new PrintStream(socket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    return socket;
  }

  public void emit(String event, String data) {
    out.print(event + ";:" + data);
    out.flush();
  }

  public void emit(String event, String id, String data) {
    out.print(event + ";" + id + ":" + data);
    out.flush();
  }

  public void processData(String data) {
    log.info("Received: " + data);
    String event = data.substring(0, data.indexOf(':'));
    String id = data.substring(data.indexOf(':') + 1);

    switch (event) {
      case "config": {
        this.emit("config", id, GSON.toJson(RMAC.config));
        break;
      }
      default:
        break;
    }
  }
}
