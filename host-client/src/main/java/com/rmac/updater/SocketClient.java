package com.rmac.updater;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Socket client acting as a channel for inter-process comms with running RMAC client process.
 */
@Slf4j
public class SocketClient {

  public Socket socket = null;
  public DataOutputStream out = null;
  public BufferedReader in = null;
  public Thread client;
  public static String MESSAGE;
  public static volatile String RESPONSE;
  /**
   * No. of retries to connect to RMAC client's socket server.
   */
  public static int MAX_RETRIES_SOCKET_CLIENT = 5;
  /**
   * Count of connection attempts made.
   */
  public static int ATTEMPT = 0;
  /**
   * Cooldown duration between connection attempts.
   */
  public static int COOLDOWN = 3000;

  public SocketClient() {
    this.createThread();
  }

  public void createThread() {
    client = new Thread(this::run, "SocketClient");
  }

  public void start() {
    this.client.start();
  }

  /**
   * Try connecting to the RMAC client's socket server and wait until a message needs to be sent.
   */
  public void run() {
    // Try connecting to RMAC client's socket server
    for (int i = 0; i <= MAX_RETRIES_SOCKET_CLIENT; i++) {
      try {
        this.connect();
        break;
      } catch (Exception e) {
        log.error("Could not connect to local server", e);
        if (i >= MAX_RETRIES_SOCKET_CLIENT) {
          log.error("Max retries exceeded");
          return;
        }
        try {
          synchronized (this.client) {
            this.client.wait(COOLDOWN);
          }
        } catch (InterruptedException ex) {
          log.error("Could not wait for cooldown", ex);
        }
      }
    }

    // Wait indefinitely until a message needs to be sent to RMAC client
    try {
      while (!Thread.interrupted()) {
        synchronized (this.client) {
          this.client.wait();
        }
        if (!this.processMessage()) {
          break;
        }
      }
    } catch (InterruptedException e) {
      log.info("SocketClient interrupted, closing this process...");
    } finally {
      try {
        out.close();
        in.close();
        socket.close();
      } catch (IOException e) {
        log.error("Could not close connection", e);
      }
    }
  }

  /**
   * Attempt to connect to RMAC client's socket server.
   *
   * @throws IOException when connection fails.
   */
  public void connect() throws IOException {
    ATTEMPT += 1;
    socket = this.getSocket();
    out = new DataOutputStream(socket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    log.info("Connected to local server");
    ATTEMPT = 0;
  }

  public Socket getSocket() throws IOException {
    return new Socket("127.0.0.1", 12735);
  }

  /**
   * Process message before sending to RMAC client's socket server
   *
   * @return whether to close connection after sending this message (false=close, true=keep-alive).
   */
  public boolean processMessage() {
    try {
      out.writeBytes(MESSAGE + "\n");
      out.flush();
      // log.info("Message sent to IPC");
    } catch (IOException e) {
      log.error("Could not send message", e);
    }

    return !MESSAGE.equals("Exit");
  }

  /**
   * Send the message to RMAC client's socket server.
   *
   * @param message The message to be sent.
   */
  public String sendMessage(String message) {
    if (Objects.isNull(socket)) {
      log.error("Cannot send message, socket not connected");
      return null;
    }
    MESSAGE = message;
    synchronized (this.client) {
      this.client.notify();
    }

    try {
      RESPONSE = in.readLine();
      // log.info("Received message: " + RESPONSE);
    } catch (SocketException | EOFException e) {
      log.error("Connection abruptly closed", e);
    } catch (IOException e) {
      log.error("Could not read message response", e);
    }

    String response = RESPONSE;
    RESPONSE = null;
    if (Objects.nonNull(response) && response.equals("Exit")) {
      log.warn("RMAC client sent 'Exit'");
      // RMAC client wants to conclude current socket connection
      try {
        out.close();
        in.close();
        socket.close();
      } catch (IOException e) {
        log.error("Could not close connection", e);
      }
    }
    return response;
  }

  /**
   * Interrupt this process and as a result stop it.
   */
  public void shutdown() throws Exception {
    this.sendMessage("Exit");
  }
}
