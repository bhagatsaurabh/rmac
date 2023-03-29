package com.rmac.comms;

import com.google.gson.Gson;
import com.rmac.RMAC;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {

  public static Gson gson = new Gson();

  String event;
  String type;
  String rayId;
  String hId;
  Object data;

  public Message(String event, String rayId, Object data) {
    this.event = event;
    this.rayId = rayId;
    this.hId = RMAC.config.getId();
    this.data = data;
    this.type = "host";
  }

  @Override
  public String toString() {
    return gson.toJson(this);
  }
}
