package com.rmac.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ConstantsTest {

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
  @DisplayName("Set runtime location")
  public void setRuntimeLocation() {
    Constants.setRuntimeLocation("X:\\test\\RMAC");

    assertEquals("X:\\test\\RMAC\\config.rmac", Constants.CONFIG_LOCATION);
    assertEquals("X:\\test\\RMAC\\nircmd.exe", Constants.NIRCMD_LOCATION);
    assertEquals("X:\\test\\RMAC\\ffmpeg.exe", Constants.FFMPEG_LOCATION);
    assertEquals("X:\\test\\RMAC\\megacmd", Constants.MEGACMD_LOCATION);
    assertEquals("X:\\test\\RMAC\\megacmd\\MEGAclient.exe", Constants.MEGACLIENT_LOCATION);
    assertEquals("X:\\test\\RMAC\\megacmd\\MEGAcmdServer.exe", Constants.MEGASERVER_LOCATION);
    assertEquals("X:\\test\\RMAC\\jre", Constants.JRE_LOCATION);
    assertEquals("X:\\test\\RMAC\\scripts", Constants.SCRIPTS_LOCATION);
    assertEquals("X:\\test\\RMAC\\svcl.exe", Constants.SVCL_LOCATION);
    assertEquals("X:\\test\\RMAC\\client.lock", Constants.INSTANCE_LOCK_LOCATION);
    assertEquals("X:\\test\\RMAC\\rmac-native.dll", Constants.RMAC_DLL_LOCATION);
  }

  @Test
  @DisplayName("Set current location fails")
  public void setCurrentLocation_Failed() {
    MockedStatic<URLDecoder> mockedDecoder = mockStatic(URLDecoder.class);
    mockedDecoder.when(() -> URLDecoder.decode(anyString(), anyString())).thenThrow(
        UnsupportedEncodingException.class);

    boolean result = Constants.setCurrentLocation();

    assertFalse(result);
    assertEquals("Could not set current location of RMAC executable\r\n", errContent.toString());

    mockedDecoder.close();
  }

  @Test
  @DisplayName("Set current location succeeds")
  public void setCurrentLocation_Success() {
    MockedStatic<URLDecoder> mockedDecoder = mockStatic(URLDecoder.class);
    mockedDecoder.when(() -> URLDecoder.decode(anyString(), anyString()))
        .thenReturn("X:\\test\\Live\\test.jar");

    boolean result = Constants.setCurrentLocation();

    assertTrue(result);
    assertEquals("X:\\test\\Live", Constants.CURRENT_LOCATION);
    assertEquals("X:\\test\\Live\\key.txt", Constants.KEYLOG_LOCATION);
    assertEquals("X:\\test\\Live\\log.txt", Constants.LOG_LOCATION);
    assertEquals("X:\\test\\Live\\archives", Constants.ARCHIVES_LOCATION);
    assertEquals("X:\\test\\Live\\archives\\screen", Constants.SCREEN_ARCHIVE_LOCATION);
    assertEquals("X:\\test\\Live\\archives\\key", Constants.LOG_ARCHIVE_LOCATION);
    assertEquals("X:\\test\\Live\\archives\\other", Constants.OTHER_ARCHIVE_LOCATION);
    assertEquals("X:\\test\\Live\\archives\\pending", Constants.PENDING_ARCHIVES_LOCATION);

    mockedDecoder.close();
  }
}
