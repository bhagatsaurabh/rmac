package com.rmac.core;

import com.rmac.Main;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.PipeStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
          "\"JABuAGUAdAByAGUAcwAgAD0AIABwAGkAbgBnACAAJwB3AHcAdwAuAGcAbwBvAGcAbABlAC4AYwBvAG0AJwAgAC0AbgAgADEAIAAtAGwAIAAxACAAfAAgAE8AdQB0AC0AUwB0AHIAaQBuAGcADQAKACEAJABuAGUAdAByAGUAcwAuAEMAbwBuAHQAYQBpAG4AcwAoACIAdQBuAHIAZQBhAGMAaABhAGIAbABlACIAKQAgAC0AYQBuAGQAIAAkAG4AZQB0AHIAZQBzAC4AQwBvAG4AdABhAGkAbgBzACgAIgBSAGUAcABsAHkAIABmAHIAbwBtACIAKQA=\"");
      Process proc = builder.start();
      BufferedReader out = new BufferedReader(new InputStreamReader(proc.getInputStream()));
      BufferedWriter in = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
      PipeStream err = new PipeStream(proc.getErrorStream(), new NoopOutputStream());
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
      boolean oldState = Main.NETWORK_STATE;
      Main.NETWORK_STATE = newState;
      if (newState != oldState) {
        log.warn("Network state changed to: " + newState);
        if (newState && Main.archiver != null) {
          new Thread(() -> Main.archiver.uploadArchives()).start();
          if (!Main.isClientRegistered) {
            new Thread(Service::registerClient).start();
          }
        }
      }
      return Main.NETWORK_STATE;
    } catch (IOException e) {
      log.error("Could not test connection", e);
    } catch (InterruptedException e) {
      log.error("Test connection process interrupted", e);
    }
    return false;
  }
}
