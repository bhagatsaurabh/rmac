package com.rmac.comms;

import com.rmac.RMAC;
import com.rmac.core.Connectivity;
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.enums.ReadyState;

@Slf4j
public class BridgeClient {

  public Thread thread;
  public Socket socket;

  public static int MAX_RETRIES = 5;
  public static int CONNECT_COOLDOWN = 3000;
  public static int RECONNECT_COOLDOWN = 60000;
  public static Queue<Message> bufferedMessages = new LinkedList<>();

  public BridgeClient() {
    try {
      this.socket = new Socket(RMAC.config.getBridgeServerUrl());
    } catch (URISyntaxException e) {
      log.error("Malformed RMAC Bridge server URL, connection will not be established", e);
      return;
    }

    this.thread = new Thread(this::run, "BridgeClient");

    Connectivity.onChange(this::networkHandler);
    RMAC.config.onChange((key, value) -> {
      this.sendMessage(new Message("config", null, RMAC.config));
    });
  }

  public void networkHandler(boolean state) {
    if (state && Objects.nonNull(this.thread) && State.WAITING == this.thread.getState()) {
      synchronized (this.thread) {
        this.thread.notify();
      }
    }
  }

  public void start() {
    if (Objects.nonNull(this.thread)) {
      this.thread.start();
    }
  }

  public void run() {
    int attempt = 1;

    while (!this.connect()) {
      if (!Connectivity.checkNetworkState()) {
        this.waitIndefinite();
        attempt = 0;
      } else {
        this.waitDefinite(attempt >= MAX_RETRIES ? RECONNECT_COOLDOWN : CONNECT_COOLDOWN);
        if (attempt >= MAX_RETRIES) {
          attempt = 0;
        }
      }

      attempt += 1;
    }

    this.unloadBuffer();
    this.thread = null;
  }

  public boolean connect() {
    if (this.socket.isOpen()) {
      return true;
    }

    boolean result = false;
    ReadyState state = this.socket.getReadyState();
    try {
      if (ReadyState.NOT_YET_CONNECTED.equals(state)) {
        result = this.socket.connectBlocking();
      } else if (ReadyState.CLOSING.equals(state) || ReadyState.CLOSED.equals(state)) {
        result = this.socket.reconnectBlocking();
      }
    } catch (Exception ignored) {
    }

    if (!result) {
      log.warn("Could not connect to remote socket");
    }
    return result;
  }

  public void reconnect() {
    this.thread = new Thread(this::run);
    this.thread.start();
  }

  public void waitIndefinite() {
    try {
      synchronized (this.thread) {
        this.thread.wait();
      }
    } catch (InterruptedException e) {
      log.error("Could not wait indefinitely");
    }
  }

  public void waitDefinite(int cooldown) {
    try {
      synchronized (this.thread) {
        this.thread.wait(cooldown);
      }
    } catch (InterruptedException e) {
      log.error("Could not wait until cooldown");
    }
  }

  public void sendMessage(Message message) {
    if (!this.isReady()) {
      // Signal: 'hostid' should not be buffered
      if ("hostid".equals(message.type)) {
        return;
      }
      bufferedMessages.add(message);
    } else {
      this.socket.emit(message);
    }
  }

  public boolean isReady() {
    return this.socket.isOpen();
  }

  public void unloadBuffer() {
    if (this.isReady()) {
      while (bufferedMessages.peek() != null) {
        this.socket.emit(bufferedMessages.poll());
      }
    }
  }

  public void shutdown() {
    if (Objects.nonNull(this.socket)) {
      this.socket.terminals.forEach((id, terminal) -> {
        terminal.shutdown(true);
      });
    }
  }
}
