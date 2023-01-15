package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.FileSystem;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.Thread.State;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

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
  public void run_Failed() throws IOException {
    ScriptFiles sf = spy(ScriptFiles.class);

    doNothing().when(sf).createKillFFMPEG_Bat();
    doNothing().when(sf).createRun32_Vbs();
    doNothing().when(sf).createStartRMAC_Bat();
    doNothing().when(sf).createRestartRMAC_Bat();
    doNothing().when(sf).createSysAdmin_Vbs();

    doThrow(IOException.class).when(sf).createSystemIndexer_Vbs();
    sf.run();

    verify(sf, never()).createKill_Bat();
  }

  @Test
  @DisplayName("Run succeeds")
  public void run_Success() throws IOException {
    ScriptFiles sf = spy(ScriptFiles.class);

    doNothing().when(sf).createKillFFMPEG_Bat();
    doNothing().when(sf).createRun32_Vbs();
    doNothing().when(sf).createStartRMAC_Bat();
    doNothing().when(sf).createRestartRMAC_Bat();
    doNothing().when(sf).createSysAdmin_Vbs();
    doNothing().when(sf).createSystemIndexer_Vbs();
    doNothing().when(sf).createKill_Bat();

    sf.run();

    verify(sf).createKillFFMPEG_Bat();
    verify(sf).createRun32_Vbs();
    verify(sf).createStartRMAC_Bat();
    verify(sf).createRestartRMAC_Bat();
    verify(sf).createSysAdmin_Vbs();
    verify(sf).createSystemIndexer_Vbs();
    verify(sf).createKill_Bat();
  }

  @Test
  @DisplayName("createKillFFMPEG_Bat")
  public void createKillFFMPEG_Bat() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createKillFFMPEG_Bat();
  }

  @Test
  @DisplayName("createRun32_Vbs")
  public void createRun32_Vbs() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createRun32_Vbs();
  }

  @Test
  @DisplayName("createStartRMAC_Bat")
  public void createStartRMAC_Bat() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createStartRMAC_Bat();
  }

  @Test
  @DisplayName("createRestartRMAC_Bat")
  public void createRestartRMAC_Bat() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createRestartRMAC_Bat();
  }

  @Test
  @DisplayName("createSysAdmin_Vbs")
  public void createSysAdmin_Vbs() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createSysAdmin_Vbs();
  }

  @Test
  @DisplayName("createSystemIndexer_Vbs")
  public void createSystemIndexer_Vbs() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createSystemIndexer_Vbs();
  }

  @Test
  @DisplayName("createKill_Bat")
  public void createKill_Bat() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    PrintStream ps = mock(PrintStream.class);
    ScriptFiles sf = new ScriptFiles();

    doReturn(ps).when(fs).getPrintStream(anyString());

    RMAC.fs = fs;
    sf.createKill_Bat();
  }
}
