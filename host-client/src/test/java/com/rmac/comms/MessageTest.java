package com.rmac.comms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.rmac.RMAC;
import com.rmac.core.Config;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class MessageTest {

  @Test
  @DisplayName("Message")
  public void message() {
    Config config = mock(Config.class);
    RMAC.config = config;

    doReturn("test-host-id").when(config).getId();

    Message message = new Message("test-event", "test-ray-id", "Test data");

    message.setEvent("test-event-1");
    message.setRayId("test-ray-id-1");
    message.setData("Test data 1");

    assertEquals("test-event-1", message.getEvent());
    assertEquals("test-ray-id-1", message.getRayId());
    assertEquals("Test data 1", message.getData());
    assertEquals("test-host-id", message.getHId());
    assertEquals("host", message.getType());
  }
}
