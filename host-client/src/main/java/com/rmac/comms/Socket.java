package com.rmac.comms;

import com.google.gson.Gson;
import com.rmac.RMAC;
import com.rmac.core.Terminal;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class Socket extends WebSocketClient {

  public static Gson GSON = new Gson();
  public Map<String, Terminal> terminals = new HashMap<>();

  public Socket(URI serverUri, Draft draft) {
    super(serverUri, draft);
  }

  public Socket(String url) throws URISyntaxException {
    super(new URI(url));
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    log.info("Connected to RMAC bridging server");

    this.emit(new Message("identity", null, RMAC.config));
  }

  @Override
  public void onMessage(String raw) {
    this.parse(raw);
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    log.warn("Connection closed to RMAC bridging server");

    if (Objects.isNull(RMAC.bridgeClient.thread)) {
      RMAC.bridgeClient.reconnect();
    }
  }

  @Override
  public void onError(Exception e) {
    log.warn("BridgeClient error", log.isDebugEnabled() ? e : null);
  }

  public void emit(Message message) {
    this.send(GSON.toJson(message));
  }

  public void parse(String data) {
    Message message = GSON.fromJson(data, Message.class);

    switch (message.getEvent()) {
      case "config": {
        this.emit(new Message("config", message.getRayId(), RMAC.config));
        break;
      }
      case "command": {
        String command = (String) message.data;
        String[] parts = command.split(" ");

        if (parts.length > 0 && "rmac".equals(parts[0])) {
          try {
            RMAC.commandHandler.execute(new String[]{(String) message.data});
            this.emit(new Message("command", message.getRayId(), "Success"));
          } catch (IOException e) {
            this.emit(new Message("command", message.getRayId(), "Failed to run command"));
          }
        } else {
          // Run native command and return result in output OR figure out interactive shell between host <-> bridge <-> console (pty ?)
        }
      }
      case "terminal:new": {
        Terminal terminal = new Terminal(message.rayId, this);
        terminals.put(message.rayId, terminal);
        terminal.start();
      }
      case "terminal:data": {
        Terminal term = this.terminals.get(message.rayId);
        try {
          term.write((String) message.data);
        } catch (IOException e) {
          log.error("Terminal Error", e);
        }
      }
      default:
        break;
    }
  }
}
