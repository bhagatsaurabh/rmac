package com.rmac.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Reads the input stream data and writes to output stream
 */
public class PipeStream extends Thread {

  InputStream is;
  OutputStream os;

  public PipeStream(InputStream is, OutputStream os) {
    this.is = is;
    this.os = os;
  }

  public void run() {
    byte[] buffer = new byte[1024];
    int len;
    try {
      while ((len = is.read(buffer)) >= 0) {
        os.write(buffer, 0, len);
      }
    } catch (IOException e) {
      System.err.println("Failed to pipe stream");
    }
  }
}