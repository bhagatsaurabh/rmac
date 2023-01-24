package com.rmac.comms;

import com.google.gson.Gson;
import com.rmac.RMAC;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class Socket extends WebSocketClient {

  public static Gson GSON = new Gson();

  public Socket(URI serverUri, Draft draft) {
    super(serverUri, draft);
  }

  public Socket(String url) throws URISyntaxException {
    super(new URI(url));
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    log.info("Connected to RMAC bridging server");

    this.emit(new Message("identity", null, null));
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
        this.emit(new Message("config", message.getCId(), RMAC.config));
        break;
      }
      default:
        break;
    }
  }
}
