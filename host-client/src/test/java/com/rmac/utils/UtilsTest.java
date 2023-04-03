package com.rmac.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class UtilsTest {

  @Test
  @DisplayName("Get timestamp util")
  public void getTimestamp() throws ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SS");
    Date date = formatter.parse("2022-01-05-13-34-03-00");

    Calendar calMock = spy(Calendar.getInstance());
    MockedStatic<Calendar> mockedCal = mockStatic(Calendar.class);
    mockedCal.when(Calendar::getInstance).thenReturn(calMock);
    mockedCal.when(() -> Calendar.getInstance(any(), any())).thenReturn(calMock);
    when(calMock.getTime()).thenReturn(date);

    assertEquals("2022-01-05-13-34-03-00", Utils.getTimestamp());

    mockedCal.close();
  }

  @Test
  @DisplayName("Get date util")
  public void getDate() throws ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy MM dd");
    Date date = formatter.parse("2022 12 04");

    Calendar calMock = spy(Calendar.getInstance());
    MockedStatic<Calendar> mockedCal = mockStatic(Calendar.class);
    mockedCal.when(Calendar::getInstance).thenReturn(calMock);
    mockedCal.when(() -> Calendar.getInstance(any(Locale.class))).thenReturn(calMock);
    mockedCal.when(() -> Calendar.getInstance(any(), any())).thenReturn(calMock);
    when(calMock.getTime()).thenReturn(date);

    assertEquals("2022 12 04", Utils.getDate());

    mockedCal.close();
  }

  @Test
  @DisplayName("Get image util")
  public void getImage() throws IOException {
    Commands.C_FFMPEG_GET_WEBCAM_SNAP = "\"whoami\"";
    Constants.RUNTIME_LOCATION = System.getProperty("user.dir");

    PipeStream mockErrPS = mock(PipeStream.class);
    PipeStream mockOutPS = mock(PipeStream.class);
    MockedStatic<PipeStream> mockedPS = mockStatic(PipeStream.class);
    mockedPS.when(() -> PipeStream.make(isA(InputStream.class), isA(NoopOutputStream.class)))
        .thenReturn(mockErrPS, mockOutPS);

    Process proc = Utils.getImage(Constants.RUNTIME_LOCATION);

    assertNotNull(proc);
    verify(mockErrPS).start();
    verify(mockOutPS).start();

    mockedPS.close();
  }
}
