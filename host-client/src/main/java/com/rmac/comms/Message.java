package com.rmac.comms;

import com.rmac.RMAC;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

  String event;
  String type;
  String rayId;
  String hId;
  Object data;

  public Message(String event, String rayId, Object data) {
    this.event = event;
    this.rayId = rayId;
    this.hId = RMAC.config.getClientId();
    this.data = data;
    this.type = "host";
  }
}
