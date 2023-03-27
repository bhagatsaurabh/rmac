package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import java.util.function.BiConsumer;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class ConfigTest {

  @Test
  @DisplayName("Config initialization")
  public void config_Default() {
    Config config = new Config();

    assertEquals(config.getServerUrl(), "");
    assertEquals(config.getMegaUser(), "");
    assertEquals(config.getMegaPass(), "");
    assertEquals(config.getVideoDuration(), 600000);
    assertEquals(config.getFPS(), 20);
    assertEquals(config.getKeyLogUploadInterval(), 600000);
    assertEquals(config.getHostName(), "");
    assertEquals(config.getClientName(), "");
    assertEquals(config.getClientId(), "");
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
    doReturn("TestField1=TestValue1")
        .doReturn("/#TestMultilineField")
        .doReturn("TestMultiline1")
        .doReturn("TestMultiline2")
        .doReturn("#/")
        .doReturn("TestField2=TestValue2")
        .doReturn(null)
        .when(reader).readLine();
    doReturn(reader).when(fs).getReader(eq(Constants.CONFIG_LOCATION));
    doNothing().when(config).setConfig(anyString(), anyString());

    RMAC.fs = fs;
    config.loadConfig();

    verify(fs).getReader(eq(Constants.CONFIG_LOCATION));
    verify(config).setConfig(eq("TestField1"), eq("TestValue1"));
    verify(config).setConfig(eq("TestMultilineField"), eq("TestMultiline1 TestMultiline2"));
    verify(config).setConfig(eq("TestField2"), eq("TestValue2"));
    verify(reader).close();
  }

  @Test
  @DisplayName("Set property with persistence")
  public void setProperty_Persist() throws NoSuchFieldException, IllegalAccessException {
    Config config = spy(Config.class);

    doNothing().when(config).updateConfig();

    config.setConfig("ServerUrl", "testurl");

    assertEquals(config.getServerUrl(), "testurl");
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
