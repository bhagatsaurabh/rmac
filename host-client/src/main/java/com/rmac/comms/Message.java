package com.rmac.comms;

import com.rmac.RMAC;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

  String event;
  String type;
  String cId;
  String hId;
  Object data;

  public Message(String event, String cId, Object data) {
    this.event = event;
    this.cId = cId;
    this.hId = RMAC.config.getClientId();
    this.data = data;
    this.type = "host";
  }
}
