package com.rmac.updater;

import lombok.extern.slf4j.Slf4j;

/**
 * Probes the health of running RMAC client process and restarts it if unhealthy for 1 minute.
 */
@Slf4j
public class Monitor implements Runnable {

  public final Thread monitor;
  public long healthCheckWindow = 60000L;
  public long healthCheckFailStart = Long.MAX_VALUE;

  public Monitor() {
    monitor = new Thread(this, "Monitor");
    monitor.start();
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        if (!healthCheck()) {
          log.warn("Health check failed");
          if (healthCheckFailStart == Long.MAX_VALUE) {
            healthCheckFailStart = System.currentTimeMillis();
          }
          if (System.currentTimeMillis() - healthCheckFailStart >= healthCheckWindow) {
            log.warn("Connection to RMACClient is down for a long time, trying to restart");
            Updater.client.shutdown();
            Updater.client = null;
            boolean success = Updater.stopRMAC() && Updater.startRMAC();
            if (success) {
              healthCheckFailStart = Long.MAX_VALUE;
              Updater.client = new SocketClient();
              log.error("RMAC client restarted successfully");
            } else {
              log.error("RMAC client re-start failed");
            }
          }
        } else {
          // log.info("Health check succeeded");
          healthCheckFailStart = Long.MAX_VALUE;
        }
        Thread.sleep(Updater.HEALTH_CHECK_INTERVAL);
      }
    } catch (InterruptedException e) {
      log.warn("Stopped", e);
    }
  }

  /**
   * Probe the RMAC client's health by sending a message through socket.
   *
   * @return result (true = success | false = failed)
   */
  private boolean healthCheck() {
    if (Updater.client != null) {
      String response = Updater.client.sendMessage("Check");
      return response != null && response.equals("Up");
    }
    return false;
  }

  /**
   * Interrupt the current instance's thread
   */
  public void shutdown() {
    this.monitor.interrupt();
  }
}
