package com.rmac.updater;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class ChecksumTest {

  @Test
  @DisplayName("Verify checksum")
  public void verifyChecksum() throws NoSuchAlgorithmException, IOException {
    assertTrue(Checksum.verifyChecksum("src/test/resources/test-file1.bin",
        "c036cbb7553a909f8b8877d4461924307f27ecb66cff928eeeafd569c3887e29"));
    assertFalse(Checksum.verifyChecksum("src/test/resources/test-file2.bin",
        "f9a7fe99a6bd458c91e9ca28c83jf84ueea938bbceb4dd9a7182e14502900753"));
    assertTrue(Checksum.verifyChecksum("src/test/resources/test-file2.bin",
        "f9a7fe99a6bd458c91e9ca28c1d0c66f4ea938bbceb4dd9a7182e14502900753"));
  }
}
