package com.rmac.comms;

import com.rmac.RMAC;
import com.rmac.core.Connectivity;
import java.lang.Thread.State;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.enums.ReadyState;

/**
 * A client to handle communications between this RMAC Host and the RMAC Bridge Server, this socket
 * connection is always-up.
 */
@Slf4j
public class BridgeClient {

  public Thread thread;
  public Socket socket;

  /**
   * Maximum no. of re-tries to attempt for connection to RMAC Bridge Server
   */
  public static int MAX_RETRIES = 5;
  /**
   * Cooldown in milliseconds if a connection attempt fails
   */
  public static int CONNECT_COOLDOWN = 3000;
  /**
   * Cooldown in milliseconds if maximum no. of re-tries has been exhausted
   */
  public static int RECONNECT_COOLDOWN = 60000;
  /**
   * Queued messages as a result of network down or connection not being ready
   */
  public static Queue<Message> bufferedMessages = new LinkedList<>();

  /**
   * Initializes a BridgeClient instance in a separate thread but does not start immediately.
   */
  public BridgeClient() {
    try {
      this.socket = new Socket(RMAC.config.getBridgeServerUrl());
    } catch (URISyntaxException e) {
      log.error("Malformed RMAC Bridge server URL, connection will not be established", e);
      return;
    }

    this.thread = new Thread(this::run, "BridgeClient");

    Connectivity.onChange(this::networkHandler);
    RMAC.config.onChange(
        (key, value) -> this.sendMessage(Message.create("config", null, RMAC.config)));
  }

  /**
   * Resumes the thread if network is back up again (if the thread is waiting in-definitely as a
   * result of network down).
   *
   * @param state The current network state <br/> (true = Network up | false = Network down)
   */
  public void networkHandler(boolean state) {
    if (state && Objects.nonNull(this.thread) && State.WAITING == this.thread.getState()) {
      synchronized (this.thread) {
        this.thread.notify();
      }
    }
  }

  /**
   * Starts the BridgeClient's instance thread, as a result starts attempting connection to RMAC
   * Bridge Server
   */
  public void start() {
    if (Objects.nonNull(this.thread)) {
      this.thread.start();
    }
  }

  /**
   * <p>Attempts forever to connect to RMAC Bridge Server until the connection is successful, waits
   * in-definitely if network is down, and waits definitely if in cooldown.</p> <br/>
   * <p>Sends any buffered messages to the Bridge Server as soon as the connection is up.</p>
   */
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

  /**
   * Attempts to establish a connection to RMAC Bridge Server.
   *
   * @return result <br/> (true = succeeded | false = failed)
   */
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

  /**
   * Creates a new thread for this instance for re-connection to Bridge Server.
   */
  public void reconnect() {
    this.thread = new Thread(this::run);
    this.thread.start();
  }

  /**
   * Suspends the BridgeClient connection thread in-definitely.
   */
  public void waitIndefinite() {
    try {
      synchronized (this.thread) {
        this.thread.wait();
      }
    } catch (InterruptedException e) {
      log.error("Could not wait indefinitely");
    }
  }

  /**
   * Suspends the BridgeClient connection thread definitely.
   *
   * @param cooldown Cooldown period in milliseconds.
   */
  public void waitDefinite(int cooldown) {
    try {
      synchronized (this.thread) {
        this.thread.wait(cooldown);
      }
    } catch (InterruptedException e) {
      log.error("Could not wait until cooldown");
    }
  }

  /**
   * Sends a message to the RMAC Bridge Server, messages are buffered if connection is not
   * established or is not up.
   *
   * @param message The Message to send.
   */
  public void sendMessage(Message message) {
    if (!this.isReady()) {
      // Signal: 'hostid' should not be buffered.
      if ("hostid".equals(message.type)) {
        return;
      }
      bufferedMessages.add(message);
    } else {
      this.socket.emit(message);
    }
  }

  /**
   * Gets the RMAC Bridge Server connection status.
   *
   * @return status <br/> (true = connected | false = not connected)
   */
  public boolean isReady() {
    return this.socket.isOpen();
  }

  /**
   * Process all buffered messages in queue and send to Bridge Server.
   */
  public void unloadBuffer() {
    if (this.isReady()) {
      while (bufferedMessages.peek() != null) {
        this.socket.emit(bufferedMessages.poll());
      }
    }
  }

  /**
   * Close all open terminal connections to this RMAC Host
   */
  public void shutdown() {
    if (Objects.nonNull(this.socket)) {
      this.socket.terminals.forEach((id, terminal) -> {
        terminal.shutdown(true);
      });
    }
  }
}
