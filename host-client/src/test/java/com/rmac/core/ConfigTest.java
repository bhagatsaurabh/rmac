package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.function.BiConsumer;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ConfigTest {

  @Test
  @DisplayName("Config initialization")
  public void config_Default() {
    Config config = new Config();

    InetAddress mockedLocalHost = mock(InetAddress.class);
    MockedStatic<InetAddress> mockedInetAddr = mockStatic(InetAddress.class);
    mockedInetAddr.when(InetAddress::getLocalHost).thenReturn(mockedLocalHost);
    doReturn("XYZABC").when(mockedLocalHost).getHostName();

    assertEquals(config.getApiServerUrl(), "");
    assertEquals(config.getMegaUser(), "");
    assertEquals(config.getMegaPass(), "");
    assertEquals(config.getVideoDuration(), 600000);
    assertEquals(config.getFPS(), 20);
    assertEquals(config.getKeyLogUploadInterval(), 600000);
    assertEquals(config.getHostName(), "XYZABC");
    assertEquals(config.getClientName(), "");
    assertEquals(config.getId(), "");
    assertTrue(config.getLogFileUpload());
    assertTrue(config.getVideoUpload());
    assertEquals(config.getMaxStagingSize(), 157286400L);
    assertEquals(config.getFetchCommandPollInterval(), 5000);
    assertEquals(config.getMaxParallelUploads(), 3);
    assertTrue(config.getScreenRecording());
    assertTrue(config.getKeyLog());
    assertTrue(config.getAudioRecording());
    assertFalse(config.getActiveAudioRecording());
    assertEquals(config.getClientHealthCheckInterval(), 3000);

    mockedInetAddr.close();
  }

  @Test
  @DisplayName("Load config when config file doesn't exist")
  public void loadConfig_NoFile() throws IOException, NoSuchFieldException, IllegalAccessException {
    Config config = new Config();
    FileSystem fs = mock(FileSystem.class);

    doReturn(false).when(fs).exists(eq(Constants.CONFIG_LOCATION));

    RMAC.fs = fs;
    config.loadConfig();

    verify(fs, never()).getReader(eq(Constants.CONFIG_LOCATION));
  }

  @Test
  @DisplayName("Load config succeeds")
  public void loadConfig_Success()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    Constants.CONFIG_LOCATION = "X:\\test\\RMAC\\config.rmac";
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);
    BufferedReader reader = mock(BufferedReader.class);

    doReturn(true).when(fs).exists(eq(Constants.CONFIG_LOCATION));
    doReturn("VideoDuration=60000")
        .doReturn("/#ApiServerUrl")
        .doReturn("TestMultiline1")
        .doReturn("TestMultiline2")
        .doReturn("#/")
        .doReturn("VideoUpload=false")
        .doReturn("MaxStagingSize=123456789")
        .doReturn(null)
        .when(reader).readLine();
    doReturn(reader).when(fs).getReader(eq(Constants.CONFIG_LOCATION));
    doNothing().when(config).setConfig(anyString(), anyString());

    RMAC.fs = fs;
    config.loadConfig();

    verify(fs).getReader(eq(Constants.CONFIG_LOCATION));
    assertEquals(config.getVideoDuration(), 60000);
    assertEquals(config.getApiServerUrl(), "TestMultiline1 TestMultiline2");
    assertFalse(config.getVideoUpload());
    assertEquals(config.getMaxStagingSize(), 123456789L);
    verify(reader).close();
  }

  @Test
  @DisplayName("Set property with persistence")
  public void setProperty_Persist() throws NoSuchFieldException, IllegalAccessException {
    Config config = spy(Config.class);

    doNothing().when(config).updateConfig();

    config.setConfig("ApiServerUrl", "testurl");

    assertEquals(config.getApiServerUrl(), "testurl");
    verify(config).updateConfig();
  }

  @Test
  @DisplayName("Get formatted video duration")
  public void getVideoDurationFormatted() {
    Config config = spy(Config.class);

    doNothing().when(config).updateConfig();

    config.setProperty("VideoDuration", 295785628);
    assertEquals("82:09:45", config.getVideoDurationFormatted());

    config.setProperty("VideoDuration", 39538);
    assertEquals("00:00:39", config.getVideoDurationFormatted());

    config.setProperty("VideoDuration", 7028374);

    assertEquals("01:57:08", config.getVideoDurationFormatted());
  }

  @Test
  @DisplayName("Set property failed")
  public void setProperty_Failed() {
    Config config = spy(Config.class);

    boolean result = config.setProperty("boguskey", (Object) "bogusvalue");

    assertFalse(result);
  }

  @Test
  @DisplayName("Set property succeeds")
  public void setProperty_Success() {
    Config config = spy(Config.class);

    doNothing().when(config).updateConfig();

    config.setProperty("ClientName", "testname");
    assertEquals("testname", config.getClientName());

    config.setProperty("KeyLog", false);
    assertFalse(config.getKeyLog());

    config.setProperty("MaxStorageSize", 34837546L);
    assertEquals(34837546L, config.getMaxStorageSize());
  }

  @Test
  @DisplayName("On change")
  public void onChange() {
    BiConsumer<String, String> callback = (x, y) -> {
    };
    Config config = new Config();

    config.onChange(callback);

    assertTrue(config.listeners.contains(callback));
  }

  @Test
  @DisplayName("Update Config failed")
  public void updateConfig_Failed() throws FileNotFoundException {
    Constants.CONFIG_LOCATION = "X:\\test\\RMAC\\config.rmac";
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(FileNotFoundException.class).when(fs).getWriter(anyString());

    RMAC.fs = fs;
    config.updateConfig();
  }

  @Test
  @DisplayName("Update Config succeeds")
  public void updateConfig_Success() throws FileNotFoundException {
    Constants.CONFIG_LOCATION = "X:\\test\\RMAC\\config.rmac";
    Config config = spy(Config.class);
    FileSystem fs = mock(FileSystem.class);
    PrintWriter writer = mock(PrintWriter.class);

    doReturn(writer).when(fs).getWriter(anyString());

    RMAC.fs = fs;
    config.updateConfig();

    verify(writer).flush();
    verify(writer).close();
  }
}
