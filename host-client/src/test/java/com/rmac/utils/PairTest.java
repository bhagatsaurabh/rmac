package com.rmac.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class PairTest {

  @Test
  @DisplayName("Pair")
  public void createPair() {
    Pair<String, Integer> pair = new Pair<>("Test", 635);
    pair.setFirst("Test1");
    pair.setSecond(729);

    assertEquals("Test1", pair.getFirst());
    assertEquals(729L, (long) pair.getSecond());
  }

}
