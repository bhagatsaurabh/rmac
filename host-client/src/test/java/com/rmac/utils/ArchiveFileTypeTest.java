package com.rmac.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class ArchiveFileTypeTest {

  @Test
  @DisplayName("Get priority")
  public void getPriority() {
    ArchiveFileType type = ArchiveFileType.OTHER;

    assertEquals(1, type.getPriority());
  }
}
