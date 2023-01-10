package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.Commands;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import com.rmac.utils.Utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Probes the network connection state.
 * <br><br>
 * Triggers upload for all the archives stored in pending archives location (/archives/pending) when
 * network state changes from down to up.
 * <br><br>
 * Triggers RMAC host registration when network state changes from down to up and when this RMAC
 * client is not already registered.
 */
@Slf4j
public class Connectivity {

  /**
   * Probe the network connection state.
   * <br>
   * Trigger upload for all the archives stored in pending archives location when network state
   * changes from down to up.
   * <br>
   * Trigger RMAC host registration when network state changes from down to up and not already
   * registered.
   *
   * @return The network state (true=network-up | false=network-down).
   */
  public synchronized static boolean checkNetworkState() {
    try {
      ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-enc",
          Commands.C_CHECK_NETWORK);
      Process proc = RMAC.fs.startProcess(builder);
      BufferedReader out = RMAC.fs.getReader(proc.getInputStream());
      BufferedWriter in = RMAC.fs.getWriter(proc.getOutputStream());
      PipeStream err = PipeStream.make(proc.getErrorStream(), new NoopOutputStream());
      err.start();
      StringBuilder result = new StringBuilder();
      String curr;
      while ((curr = out.readLine()) != null) {
        result.append(curr.trim());
      }
      proc.waitFor();
      in.close();
      out.close();

      boolean newState = Boolean.parseBoolean(result.toString());
      boolean oldState = RMAC.NETWORK_STATE;
      RMAC.NETWORK_STATE = newState;
      if (newState != oldState) {
        log.warn("Network state changed to: " + newState);
        if (newState && Objects.nonNull(RMAC.archiver)) {
          RMAC.archiver.uploadArchiveAsync();
          if (!RMAC.isClientRegistered) {
            RMAC.service.registerClientAsync();
          }
        }
      }
      return RMAC.NETWORK_STATE;
    } catch (IOException e) {
      log.error("Could not test connection", e);
    } catch (InterruptedException e) {
      log.error("Test connection process interrupted", e);
    }
    return false;
  }
}
