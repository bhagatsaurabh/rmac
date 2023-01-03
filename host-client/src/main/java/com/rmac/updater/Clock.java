package com.rmac.updater;

import java.time.Instant;
public class Clock {

  public long millis() {
    return Instant.now().toEpochMilli();
  }
}
