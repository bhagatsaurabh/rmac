package com.rmac.ipc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Local socket server acting as a channel for inter-process comms with RMACUpdater
 */
@Slf4j
public class IPC {

  public Thread server;
  public ServerSocket serverSocket;
  public Socket socket;
  public BufferedReader in;
  public PrintStream out;
  public int port = 12735;

  /**
   * Create a local socket server.
   */
  public IPC() {
    try {
      this.serverSocket = IPC.getInstance(this.port);
      log.info("Socket server started on port: " + this.port);
    } catch (IOException e) {
      log.error("Could not create socket server on port: " + this.port, e);
    }

    if (Objects.nonNull(this.serverSocket)) {
      this.server = new Thread(this::serve, "IPC");
    }
  }

  public void start() {
    if (Objects.nonNull(this.serverSocket)) {
      this.server.start();
    }
  }

  /**
   * Start listening for socket client connection request, once connected, start reading and
   * processing messages coming from connected process.
   */
  public void serve() {
    try {
      this.socket = this.serverSocket.accept();
      this.out = new PrintStream(this.socket.getOutputStream());
      this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      log.info("RMACUpdater connected");
    } catch (IOException e) {
      log.error("Could not accept client connection", e);
    }

    if (Objects.nonNull(this.serverSocket) && Objects.nonNull(this.socket)) {
      String message = "";
      while (Objects.nonNull(message) && !message.equals("Exit") && !this.socket.isClosed()) {
        try {
          message = this.in.readLine();
          // log.info("Received message: " + message);
          this.processMessage(message);
        } catch (SocketException | EOFException | RuntimeException e) {
          log.error("Connection abruptly closed by client", e);
          break;
        } catch (IOException e) {
          log.error("Could not read client message", e);
        }
      }
      log.warn("Closing current socket connection");
      try {
        this.socket.close();
        this.in.close();
        this.out.close();
      } catch (IOException e) {
        log.error("Could not close socket server", e);
      }

      if (Objects.nonNull(message) && message.equals("Exit")) {
        // If the message was Exit (graceful connection closure from client),
        // continue serving and listen for new connection, otherwise natural shutdown of this threaded process will follow
        this.serve();
      }
    }
  }

  /**
   * Process message received from connected process, valid messages include:
   * <br>
   * <ul>
   *   <li><b>Stop</b>: Signals this RMAC client to shutdown gracefully.</li>
   *   <li><b>Check</b>: Health check for this RMAC client process.</li>
   *   <li><b>Exit</b>: Close the current socket connection and start serving a new socket.</li>
   * </ul>
   *
   * @param message The message
   */
  public void processMessage(String message) throws IOException {
    if (Objects.isNull(message)) {
      return;
    }
    if (message.equals("Stop")) {
      log.warn("Received message 'Stop'");
      this.out.println("Exit");
      this.out.flush();
      if (Objects.nonNull(this.in)) {
        try {
          this.in.close();
        } catch (IOException e) {
          log.error("Could not close Socket input stream", e);
        }
      }
      if (Objects.nonNull(this.out)) {
        this.out.close();
      }
      if (Objects.nonNull(this.socket)) {
        try {
          this.socket.close();
        } catch (IOException e) {
          log.error("Could not close socket", e);
        }
      }
      // Shutdown
      System.exit(0);
    } else if (message.equals("Check")) {
      // Maybe sending some diagnostics here can be more helpful instead of just sending a thumbs up
      this.out.println("Up");
      this.out.flush();
      // log.info("Response sent to SocketClient");
    }
  }

  public static ServerSocket getInstance(int port) throws IOException {
    return new ServerSocket(port);
  }
}
