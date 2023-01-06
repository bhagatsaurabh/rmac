package com.rmac.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ConstantsTest {

  @Test
  @DisplayName("Set runtime location")
  public void setRuntimeLocation() {
    Constants.setRuntimeLocation("X:\\test\\RMAC");

    assertEquals("X:\\test\\RMAC", Constants.RUNTIME_LOCATION);
    assertEquals("X:\\test\\RMAC\\update.lock", Constants.UPDATE_LOCK_LOCATION);
    assertEquals("X:\\test\\RMAC\\updater.lock", Constants.INSTANCE_LOCK_LOCATION);
    assertEquals("X:\\test\\RMAC\\update\\", Constants.UPDATE_LOCATION);
    assertEquals("X:\\test\\RMAC\\scripts\\startrmac.bat", Constants.START_RMAC_LOCATION);
  }

  @Test
  @DisplayName("Set current location fails")
  public void setCurrentLocation_Failed() {
    MockedStatic<URLDecoder> mockedDecoder = mockStatic(URLDecoder.class);
    mockedDecoder.when(() -> URLDecoder.decode(anyString(), anyString()))
        .thenThrow(UnsupportedEncodingException.class);

    Constants.RMAC_LOCATION = null;
    Constants.CURRENT_LOCATION = null;
    boolean result = Constants.setCurrentLocation();

    assertNull(Constants.CURRENT_LOCATION);
    assertNull(Constants.RMAC_LOCATION);
    assertFalse(result);

    mockedDecoder.close();
  }

  @Test
  @DisplayName("Set current location succeeded")
  public void setCurrentLocation_Success() {
    MockedStatic<URLDecoder> mockedDecoder = mockStatic(URLDecoder.class);
    mockedDecoder.when(() -> URLDecoder.decode(any(), any()))
        .thenReturn("X:\\test\\Live\\");

    boolean result = Constants.setCurrentLocation();

    assertEquals("X:\\test\\Live", Constants.CURRENT_LOCATION);
    assertEquals("X:\\test\\Live\\RMACClient.jar", Constants.RMAC_LOCATION);
    assertTrue(result);

    mockedDecoder.close();
  }
}
