package com.rmac.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class PairTest {

  @Test
  @DisplayName("Pair")
  public void createPair() {
    Pair<String, Integer> pair = new Pair<>("Test", 635);

    assertEquals("Test", pair.getFirst());
    assertEquals(635L, (long) pair.getSecond());
  }
}
