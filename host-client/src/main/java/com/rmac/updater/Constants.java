package com.rmac.updater;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Constants {

  private Constants() {
    /**/
  }

  public static String CURRENT_LOCATION;
  public static String RMAC_LOCATION;

  public static String RUNTIME_LOCATION;
  public static String UPDATE_LOCK_LOCATION;
  public static String UPDATE_LOCATION;
  public static String START_RMAC_LOCATION;
  public static String INSTANCE_LOCK_LOCATION;

  public static String LITERAL_RMAC_JAR_NAME = "RMACClient.jar";

  public static void setRuntimeLocation(String runtimeLocation) {
    RUNTIME_LOCATION = runtimeLocation;
    UPDATE_LOCK_LOCATION = Constants.RUNTIME_LOCATION + "\\update.lock";
    INSTANCE_LOCK_LOCATION = Constants.RUNTIME_LOCATION + "\\updater.lock";
    UPDATE_LOCATION = Constants.RUNTIME_LOCATION + "\\update\\";
    START_RMAC_LOCATION = Constants.RUNTIME_LOCATION + "\\scripts\\startrmac.bat";
  }

  public static boolean setCurrentLocation() {
    try {
      CURRENT_LOCATION = URLDecoder.decode(
          Updater.class
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
      RMAC_LOCATION = Constants.CURRENT_LOCATION + "\\RMACClient.jar";
    } catch (UnsupportedEncodingException e) {
      System.err.println("Could not set current location of RMACUpdater executable");
      return false;
    }
    return true;
  }
}
