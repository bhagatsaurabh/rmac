package com.rmac.updater;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;

/**
 * Socket client acting as a channel for inter-process comms with running RMAC client process.
 */
@Slf4j
public class SocketClient {

  private Socket socket = null;
  private DataOutputStream out = null;
  private BufferedReader in = null;
  public final Thread client;
  private static String MESSAGE;
  private static volatile String RESPONSE;
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
    client = new Thread(this::connectToServer, "SocketClient");
    client.start();
  }

  /**
   * Try connecting to the RMAC client's socket server and wait until a message needs to be sent.
   */
  private void connectToServer() {
    // Try connecting to RMAC client's socket server
    for (int i = 0; i <= MAX_RETRIES_SOCKET_CLIENT; i++) {
      try {
        connect();
        break;
      } catch (Exception e) {
        log.error("Could not connect to local server", e);
        if (i == MAX_RETRIES_SOCKET_CLIENT) {
          log.error("Max retries exceeded");
          return;
        }
        try {
          Thread.sleep(COOLDOWN);
        } catch (InterruptedException ex) {
          log.error("Could not wait for cooldown", ex);
        }
      }
    }

    // Wait indefinitely until a message needs to be sent to RMAC client
    try {
      while (!Thread.interrupted()) {
        synchronized (client) {
          client.wait();
        }
        if (!processMessage()) {
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
  private void connect() throws IOException {
    ATTEMPT += 1;
    socket = new Socket("127.0.0.1", 12735);
    out = new DataOutputStream(socket.getOutputStream());
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    log.info("Connected to local server");
    ATTEMPT = 0;
  }

  /**
   * Process message before sending to RMAC client's socket server
   *
   * @return whether to close connection after sending this message (false=close, true=keep-alive).
   */
  private boolean processMessage() {
    try {
      out.writeBytes(MESSAGE + "\n");
      out.flush();
      // log.info("Message sent to SocketServer");
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
    if (socket == null) {
      log.error("Cannot send message, socket not connected");
      return null;
    }
    MESSAGE = message;
    synchronized (client) {
      client.notify();
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
    if (response.equals("Exit")) {
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
  public void shutdown() {
    this.sendMessage("Exit");
  }
}
