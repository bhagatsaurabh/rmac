package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.process.KeyLog;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class KeyRecorderTest {

  @Test
  @DisplayName("KeyRecorder initialization when native look fails")
  public void keyRecorder_NativeHookRegistration_Failed() {
    MockedStatic<GlobalScreen> globalScreen = mockStatic(GlobalScreen.class);

    globalScreen.when(GlobalScreen::registerNativeHook).thenThrow(NativeHookException.class);

    KeyRecorder key = new KeyRecorder();

    assertEquals(KeyRecorder.nativeKeyListener, key);
    globalScreen.verify(() -> GlobalScreen.addNativeKeyListener(eq(key)));

    globalScreen.close();
  }

  @Test
  @DisplayName("KeyRecorder initialization when native look succeeds")
  public void keyRecorder_NativeHookRegistration_Success() {
    MockedStatic<GlobalScreen> globalScreen = mockStatic(GlobalScreen.class);

    globalScreen.when(GlobalScreen::registerNativeHook).thenAnswer(invc -> null);

    KeyRecorder key = new KeyRecorder();

    assertEquals(KeyRecorder.nativeKeyListener, key);
    globalScreen.verify(() -> GlobalScreen.addNativeKeyListener(eq(key)));

    globalScreen.close();
  }

  @Test
  @DisplayName("Get CapsLock State when process execution fails")
  public void getCapsState_Failed() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    KeyRecorder key = spy(KeyRecorder.class);

    doThrow(IOException.class).when(key).startProcess(any());

    boolean result = key.getCapsState();

    assertFalse(result);
  }

  @Test
  @DisplayName("Get CapsLock State when process execution succeeds")
  public void getCapsState_Success() throws IOException, InterruptedException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    KeyRecorder key = spy(KeyRecorder.class);
    BufferedReader reader = mock(BufferedReader.class);
    MockedStatic<PipeStream> pipe = mockStatic(PipeStream.class);
    PipeStream mockPipe = mock(PipeStream.class);
    Process proc = mock(Process.class);
    FileSystem fs = mock(FileSystem.class);
    InputStream is = mock(InputStream.class);

    doReturn(0).when(proc).waitFor();
    doReturn(is).when(proc).getInputStream();
    doReturn(proc).when(key).startProcess(any());
    doReturn(reader).when(fs).getReader(any(InputStream.class));
    doReturn("True").doReturn(null).when(reader).readLine();
    pipe.when(() -> PipeStream.make(any(), any())).thenReturn(mockPipe);

    RMAC.fs = fs;
    boolean result = key.getCapsState();

    assertTrue(result);

    pipe.close();
  }

  @Test
  @DisplayName("Native key pressed when key is Unknown and code is 'a1'")
  public void nativeKeyPressed_Unknown() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Unknown");
    doReturn(0).when(nke).getKeyCode();
    doReturn(161).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();

    RMAC.keyLog = keyLog;
    key.nativeKeyPressed(nke);

    verify(keyLog).println(eq("[Shift|2]"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed when key is Unknown and code is unrecognizable")
  public void nativeKeyPressed_Code_Unrecognizable() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Unknown");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();

    RMAC.keyLog = keyLog;
    key.nativeKeyPressed(nke);

    verify(keyLog).println(eq("[0x4d2]"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed when key unrecognizable, caps and shift state matches")
  public void nativeKeyPressed_Key_Unrecognizable_State_Sync() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("XYZ");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();

    RMAC.keyLog = keyLog;
    KeyRecorder.isCaps = true;
    KeyRecorder.isShift = true;
    key.nativeKeyPressed(nke);

    verify(keyLog).print(eq("xyz"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed when key unrecognizable, caps and shift state doesn't match")
  public void nativeKeyPressed_Key_Unrecognizable_State_NoSync() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("XYZ");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();

    RMAC.keyLog = keyLog;
    KeyRecorder.isCaps = true;
    KeyRecorder.isShift = false;
    key.nativeKeyPressed(nke);

    verify(keyLog).print(eq("XYZ"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed CapsLock")
  public void nativeKeyPressed_CapsLock() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Caps Lock");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();

    RMAC.keyLog = keyLog;
    key.nativeKeyPressed(nke);

    verify(key).getCapsState();
    assertFalse(KeyRecorder.isCaps);

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed Shift")
  public void nativeKeyPressed_Shift() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Shift");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();

    RMAC.keyLog = keyLog;
    key.nativeKeyPressed(nke);

    assertTrue(KeyRecorder.isShift);
    verify(keyLog).println(eq("[Shift|2]"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed Delete, Enter, Space, Tab")
  public void nativeKeyPressed_Delete_Enter_Space() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt()))
        .thenReturn("Delete", "Enter", "Space", "Tab");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();
    doNothing().when(keyLog).print(anyString());
    doNothing().when(keyLog).println(anyString());
    doNothing().when(keyLog).println();

    RMAC.keyLog = keyLog;
    key.nativeKeyPressed(nke);
    key.nativeKeyPressed(nke);
    key.nativeKeyPressed(nke);
    key.nativeKeyPressed(nke);

    verify(keyLog, times(2)).println(anyString());
    verify(keyLog).println();
    verify(keyLog).print(" ");

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key pressed Comma")
  public void nativeKeyPressed_Comma() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt()))
        .thenReturn("Comma");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();
    doNothing().when(keyLog).print(anyString());

    RMAC.keyLog = keyLog;
    KeyRecorder.isCaps = false;
    KeyRecorder.isShift = true;
    key.nativeKeyPressed(nke);

    verify(keyLog).print("<");
    verify(key).getDual(eq("Comma"));

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key released when key is not 'Shift' and code is not 'a1'")
  public void nativeKeyReleased_NotShift_NotA1() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Tab");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();
    doNothing().when(keyLog).print(anyString());

    RMAC.keyLog = keyLog;
    KeyRecorder.isCaps = false;
    KeyRecorder.isShift = true;
    key.nativeKeyReleased(nke);

    assertTrue(KeyRecorder.isShift);

    nkeMock.close();
  }

  @Test
  @DisplayName("Native key released when key is 'Shift' and code is not 'a1'")
  public void nativeKeyReleased_Shift_NotA1() {
    KeyRecorder key = spy(KeyRecorder.class);
    MockedStatic<NativeKeyEvent> nkeMock = mockStatic(NativeKeyEvent.class);
    NativeKeyEvent nke = mock(NativeKeyEvent.class);
    KeyLog keyLog = mock(KeyLog.class);

    nkeMock.when(() -> NativeKeyEvent.getKeyText(anyInt())).thenReturn("Shift");
    doReturn(0).when(nke).getKeyCode();
    doReturn(1234).when(nke).getRawCode();
    doReturn(2).when(nke).getKeyLocation();
    doReturn(false).when(key).getCapsState();
    doNothing().when(keyLog).print(anyString());

    RMAC.keyLog = keyLog;
    KeyRecorder.isCaps = false;
    KeyRecorder.isShift = true;
    key.nativeKeyReleased(nke);

    assertFalse(KeyRecorder.isShift);

    nkeMock.close();
  }

  @Test
  @DisplayName("Start process")
  public void startProcess() throws IOException {
    KeyRecorder key = spy(KeyRecorder.class);
    ProcessBuilder builder = mock(ProcessBuilder.class);

    key.nativeKeyTyped(null);
    key.startProcess(builder);

    verify(builder).start();
  }
}
