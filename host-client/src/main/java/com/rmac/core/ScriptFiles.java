package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Constants;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

/**
 * Verifies if all the script files exist and creates them otherwise.
 * <br><br>
 * Scripts are executable shell scripts that are all generated under runtime directory except
 * <i>rmac.vbs</i>.
 * <br><br>
 * These scripts perform tasks that are otherwise not possible from within the running RMAC client
 * process.
 * <br><br>
 * <code>kill_ffmpeg.bat</code>
 * <br>
 * This script forcefully kills the ffmpeg.exe process responsible for screen recording.
 * <br><br>
 * <code>background.vbs</code>
 * <br>
 * This script executes other scripts/cli-programs as a background process.
 * <br><br>
 * <code>startrmac.bat</code>
 * <br>
 * This script starts the RMAC client executable as a background process.
 * <br><br>
 * <code>restartrmac.bat</code>
 * <br>
 * This script re-starts the RMAC client executable as a background process.
 * <br><br>
 * <code>rmac.vbs</code>
 * <br>
 * This script is generated under the startup location and is responsible to start RMAC everytime
 * the host machine boots.
 * <br>
 */
@Slf4j
public class ScriptFiles {

  public Thread thread;

  public ScriptFiles() {
    thread = new Thread(this::run, "ScriptFiles");
  }

  /**
   * Starts the script creation thread.
   */
  public void start() {
    this.thread.start();
  }

  /**
   * Creates all the script files.
   */
  public void run() {
    try {
      Map<String, String> values = new HashMap<>();
      for (Field field : Utils.getFields(Constants.class)) {
        values.put(field.getName(), (String) field.get(null));
      }
      StringSubstitutor substitutor = new StringSubstitutor(values);

      copyScript("kill_ffmpeg.bat", Constants.SCRIPTS_LOCATION);
      copyScript("background.vbs", Constants.SCRIPTS_LOCATION);
      copyScript("start_rmac.bat", substitutor, Constants.SCRIPTS_LOCATION);
      copyScript("restart_rmac.bat", substitutor, Constants.SCRIPTS_LOCATION);
      copyScript("rmac.vbs", substitutor, Constants.STARTUP_LOCATION);

      log.info("ScriptFiles successfully created");
    } catch (IllegalAccessException e) {
      log.error("ScriptFiles generation failed", e);
    }
  }

  /**
   * Copy the script from resources to runtime location.
   *
   * @param script The script name.
   */
  public void copyScript(String script, String destination) {
    try (InputStream is = RMAC.fs.getResourceAsStream(RMAC.class, "/scripts/" + script)) {
      if (Objects.nonNull(is)) {
        RMAC.fs.copy(
            is, destination + "\\" + script, StandardCopyOption.REPLACE_EXISTING
        );
      } else {
        log.error("Could not copy script: {}", script);
      }
    } catch (IOException e) {
      log.warn("Could not read script resource: {}", script, e);
    }
  }

  /**
   * Copy the script from resources to runtime location with variable substitution.
   *
   * @param script      The script name.
   * @param substitutor The variable substitutor.
   */
  public void copyScript(String script, StringSubstitutor substitutor, String destination) {
    try (InputStream is = RMAC.fs.getResourceAsStream(RMAC.class, "/scripts/" + script)) {
      if (Objects.nonNull(is)) {
        BufferedReader reader = RMAC.fs.getReader(is);
        String scriptContent = substitutor.replace(
            reader.lines().collect(Collectors.joining("\n"))
        );

        RMAC.fs
            .getPrintStream(destination + "\\" + script)
            .println(scriptContent);

        reader.close();
      } else {
        log.error("Could not copy script: {}", script);
      }
    } catch (IOException e) {
      log.warn("Could not read script resource: {}", script, e);
    }
  }
}
