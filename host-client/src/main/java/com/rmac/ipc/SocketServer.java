package com.rmac.ipc;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;

/**
 * Local socket server acting as a channel for inter-process comms with RMACUpdater
 */
@Slf4j
public class SocketServer {

  public static Thread server;
  public static ServerSocket serverSocket;
  public static Socket socket;
  public static BufferedReader in;
  public static PrintStream out;
  public static int port = 12735;

  /**
   * Create a local socket server.
   */
  public SocketServer() {
    try {
      serverSocket = new ServerSocket(port);
      log.info("Socket server started on port: " + port);
    } catch (IOException e) {
      log.error("Could not create socket server on port: " + port, e);
    }

    if (serverSocket != null) {
      server = new Thread(this::serve, "SocketServer");
      server.start();
    }
  }

  /**
   * Start listening for socket client connection request, once connected, start reading and
   * processing messages coming from connected process.
   */
  private void serve() {
    try {
      socket = serverSocket.accept();
      out = new PrintStream(socket.getOutputStream());
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      log.info("RMACUpdater connected");
    } catch (IOException e) {
      log.error("Could not accept client connection", e);
    }

    if (serverSocket != null && socket != null) {
      String message = "";
      while (!message.equals("Exit") && !socket.isClosed()) {
        try {
          message = in.readLine();
          // log.info("Received message: " + message);
          processMessage(message);
        } catch (SocketException | EOFException e) {
          log.error("Connection abruptly closed by client", e);
          break;
        } catch (IOException e) {
          log.error("Could not read client message", e);
        }
      }
      log.warn("Closing current socket connection");
      try {
        socket.close();
        in.close();
        out.close();
      } catch (IOException e) {
        log.error("Could not close socket server", e);
      }

      if (message.equals("Exit")) {
        // If the message was Exit (graceful connection closure from client),
        // continue serving and listen for new connection, otherwise natural shutdown of this threaded process
        serve();
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
  public void processMessage(String message) {
    if (message == null) {
      return;
    }
    if (message.equals("Stop")) {
      log.warn("Received message 'Stop'");
      out.println("Exit");
      out.flush();
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          log.error("Could not close Socket input stream", e);
        }
      }
      if (out != null) {
        out.close();
      }
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          log.error("Could not close socket", e);
        }
      }
      // Shutdown
      System.exit(0);
    } else if (message.equals("Check")) {
      // Maybe sending some diagnostics here can be more helpful instead of just sending a thumbs up
      out.println("Up");
      out.flush();
      // log.info("Response sent to SocketClient");
    }
  }
}
