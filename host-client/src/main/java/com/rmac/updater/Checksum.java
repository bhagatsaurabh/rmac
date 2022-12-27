package com.rmac.updater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies given file's integrity by calculating its SHA-256 checksum and comparing against expected
 * value.
 */
@Slf4j
public class Checksum {

  /**
   * Calculate SHA-256 checksum of the given file and compare against given expected value.
   *
   * @param file The file for which to perform integrity check.
   * @param expected The expected value for SHA-256 checksum.
   * @return result (true = success | false = failed)
   * @throws NoSuchAlgorithmException when algorithm used to calculate checksum is un-recognizable.
   * @throws IOException when file read fails.
   */
  public static boolean verifyChecksum(File file, String expected)
      throws NoSuchAlgorithmException, IOException {

    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    FileInputStream fis = new FileInputStream(file);

    byte[] chunk = new byte[1024];
    int bytesCount;
    while ((bytesCount = fis.read(chunk)) != -1) {
      digest.update(chunk, 0, bytesCount);
    }
    fis.close();

    byte[] bytes = digest.digest();
    StringBuilder sb = new StringBuilder();
    for (byte aByte : bytes) {
      sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
    }

    String actual = sb.toString();
    log.info(expected + " ; " + actual);
    return expected.equals(actual);
  }
}
