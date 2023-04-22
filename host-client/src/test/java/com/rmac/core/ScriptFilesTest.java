package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.Thread.State;
import java.lang.reflect.Field;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.text.StringSubstitutor;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ScriptFilesTest {

  @Test
  @DisplayName("ScriptFiles initialization")
  public void scriptFiles() {
    ScriptFiles sf = new ScriptFiles();

    assertEquals(State.NEW, sf.thread.getState());
  }

  @Test
  @DisplayName("Start")
  public void start() {
    ScriptFiles sf = new ScriptFiles();
    Thread t = mock(Thread.class);

    sf.thread = t;
    sf.start();

    verify(t).start();
  }

  @Test
  @DisplayName("Run fails")
  public void run_Failed() throws IllegalAccessException {
    MockedStatic<Utils> mockUtils = mockStatic(Utils.class);
    Field mockField = mock(Field.class);

    doThrow(IllegalAccessException.class).when(mockField).get(eq(null));
    mockUtils.when(() -> Utils.getFields(any(Class.class))).thenReturn(new Field[]{mockField});

    ScriptFiles scriptFiles = spy(ScriptFiles.class);
    scriptFiles.run();

    verify(scriptFiles, times(0)).copyScript(anyString(), anyString());
    verify(scriptFiles, times(0)).copyScript(anyString(), any(StringSubstitutor.class), anyString());

    mockUtils.close();
  }

  @Test
  @DisplayName("Run succeeds")
  public void run_Success() {
    ScriptFiles scriptFiles = spy(ScriptFiles.class);

    doNothing().when(scriptFiles).copyScript(anyString(), anyString());
    doNothing().when(scriptFiles).copyScript(anyString(), any(StringSubstitutor.class), anyString());

    scriptFiles.run();

    verify(scriptFiles).copyScript(eq("kill_ffmpeg.bat"), anyString());
    verify(scriptFiles).copyScript(eq("background.vbs"), anyString());
    verify(scriptFiles).copyScript(eq("start_rmac.bat"), any(StringSubstitutor.class), anyString());
    verify(scriptFiles).copyScript(eq("restart_rmac.bat"), any(StringSubstitutor.class), anyString());
    verify(scriptFiles).copyScript(eq("rmac.vbs"), any(StringSubstitutor.class), anyString());
    verify(scriptFiles).copyScript(eq("compromised.bat"), any(StringSubstitutor.class), anyString());
  }

  @Test
  @DisplayName("Copy script no file")
  public void copyScript_Failed_NoFile() throws IOException {
    FileSystem fs = mock(FileSystem.class);

    doReturn(null).when(fs).getResourceAsStream(eq(RMAC.class), anyString());

    RMAC.fs = fs;
    ScriptFiles scriptFiles = new ScriptFiles();
    scriptFiles.copyScript("test.script", "");

    verify(fs, times(0)).copy(any(InputStream.class), anyString(),
        eq(StandardCopyOption.REPLACE_EXISTING));
  }

  @Test
  @DisplayName("Copy script failed")
  public void copyScript_Failed() throws IOException {
    Constants.SCRIPTS_LOCATION = "X:\\Runtime\\scripts";
    FileSystem fs = mock(FileSystem.class);
    InputStream mockIS = mock(InputStream.class);

    doReturn(mockIS).when(fs).getResourceAsStream(eq(RMAC.class), eq("/scripts/test.script"));
    doThrow(IOException.class).when(fs)
        .copy(any(InputStream.class), eq("X:\\Runtime\\scripts\\test.script"),
            eq(StandardCopyOption.REPLACE_EXISTING));

    RMAC.fs = fs;
    ScriptFiles scriptFiles = new ScriptFiles();
    scriptFiles.copyScript("test.script", "");
  }

  @Test
  @DisplayName("Copy script with substitution no file")
  public void copyScript_Substitution_NoFile() throws IOException {
    FileSystem fs = mock(FileSystem.class);

    doReturn(null).when(fs).getResourceAsStream(eq(RMAC.class), anyString());

    RMAC.fs = fs;
    ScriptFiles scriptFiles = new ScriptFiles();
    scriptFiles.copyScript("test.script", null);

    verify(fs, times(0)).copy(any(InputStream.class), anyString(),
        eq(StandardCopyOption.REPLACE_EXISTING));
  }

  @Test
  @DisplayName("Copy script with substitution fails")
  public void copyScript_Substitution_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    InputStream mockIS = mock(InputStream.class);
    BufferedReader mockReader = mock(BufferedReader.class);
    StringSubstitutor mockSubstitutor = mock(StringSubstitutor.class);

    doReturn(mockReader).when(fs).getReader(any(InputStream.class));
    doAnswer((invc) -> Stream.of("test line 1")).when(mockReader).lines();
    doReturn(mockIS).when(fs).getResourceAsStream(eq(RMAC.class), eq("/scripts/test.script"));
    doThrow(FileNotFoundException.class).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    ScriptFiles scriptFiles = new ScriptFiles();
    scriptFiles.copyScript("test.script", mockSubstitutor, "");
  }

  @Test
  @DisplayName("Copy script with substitution succeeds")
  public void copyScript_Substitution_Success() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    InputStream mockIS = mock(InputStream.class);
    BufferedReader mockReader = mock(BufferedReader.class);
    PrintStream mockPS = mock(PrintStream.class);

    doReturn(mockReader).when(fs).getReader(any(InputStream.class));
    doAnswer((invc) -> Stream.of("test ${var1} 1")).when(mockReader).lines();
    doReturn(mockIS).when(fs).getResourceAsStream(eq(RMAC.class), eq("/scripts/test.script"));
    doThrow(FileNotFoundException.class).when(fs).getPrintStream(anyString());
    doReturn(mockPS).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    ScriptFiles scriptFiles = new ScriptFiles();
    Map<String, String> varMap = new HashMap<>();
    varMap.put("var1", "line");
    StringSubstitutor substitutor = new StringSubstitutor(varMap);
    scriptFiles.copyScript("test.script", substitutor, "");

    verify(mockPS).println(eq("test line 1"));
  }
}
