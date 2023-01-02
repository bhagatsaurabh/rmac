package com.rmac.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NoopOutputStreamTest {

  @Captor
  ArgumentCaptor<Character> byteCaptor;

  @Test
  @DisplayName("Noop OutputStream")
  public void write() throws IOException {
    OutputStream os = spy(new NoopOutputStream());

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
    writer.write('t');
    writer.write('e');
    writer.write('s');
    writer.write('t');
    writer.flush();

    verify(os, times(4)).write(byteCaptor.capture());

    List<Character> bytes = byteCaptor.getAllValues();

    assertEquals(Arrays.asList(116, 101, 115, 116), bytes);

    writer.close();
    os.close();
  }
}
