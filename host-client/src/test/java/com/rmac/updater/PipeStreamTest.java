package com.rmac.updater;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class PipeStreamTest {

  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;

  @Before
  public void setUpStreams() {
    System.setErr(new PrintStream(errContent));
  }

  @After
  public void restoreStreams() {
    System.setErr(originalErr);
  }

  @Test
  @DisplayName("Pipe stream fails")
  public void run_Failed() throws IOException, InterruptedException {
    InputStream mockIS = mock(InputStream.class);
    when(mockIS.read(any())).thenThrow(IOException.class);
    OutputStream mockOS = mock(OutputStream.class);

    PipeStream ps = new PipeStream(mockIS, mockOS);
    ps.start();
    ps.join();

    assertEquals("Failed to pipe stream\r\n", errContent.toString());
  }

  @Test
  @DisplayName("Pipe stream succeeds")
  public void run_Success() throws InterruptedException {
    String data = "Test data";

    PipeStream ps = new PipeStream(new ByteArrayInputStream(data.getBytes()),
        System.err);
    ps.start();
    ps.join();

    assertEquals("Test data", errContent.toString());
  }
}

