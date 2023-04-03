package com.rmac.comms;

import com.rmac.RMAC;
import lombok.Getter;
import lombok.Setter;

/**
 * The Message model between RMAC Host and Bridge Server interface (JSON).
 */
@Getter
@Setter
public class Message {

  /**
   * The message event, signifying what action needs to be taken when this message is received.
   */
  String event;
  /**
   * The type is the origin of the message, either 'host' (RMAC Host) or 'console' (RMAC Console).
   */
  String type;
  /**
   * A unique identifier for end-to-end trace of a messaging sequence between this RMAC Host, RMAC
   * Bridge Server and RMAC Console.
   */
  String rayId;
  /**
   * The id of this host.
   */
  String hId;
  /**
   * The data that needs to be sent.
   */
  Object data;

  public Message(String event, String rayId, Object data) {
    this.event = event;
    this.rayId = rayId;
    this.hId = RMAC.config.getId();
    this.data = data;
    this.type = "host";
  }
}
