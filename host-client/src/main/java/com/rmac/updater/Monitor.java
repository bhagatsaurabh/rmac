package com.rmac.updater;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

/**
 * Probes the health of running RMAC client process and restarts it if unhealthy for more than 1
 * minute.
 */
@Slf4j
public class Monitor {

  public Thread thread;
  public long healthCheckWindow = 60000L;
  public long healthCheckFailStart = Long.MAX_VALUE;
  public Clock clock;
  public Updater updater;

  public Monitor() {
    this.createThread();
    this.clock = new Clock();
  }

  public void createThread() {
    thread = new Thread(() -> {
      try {
        while (!Thread.interrupted()) {
          if (!this.healthCheck()) {
            log.warn("Health check failed");
            if (healthCheckFailStart == Long.MAX_VALUE) {
              healthCheckFailStart = clock.millis();
            }
            if (clock.millis() - healthCheckFailStart >= healthCheckWindow) {
              log.warn("Connection to RMACClient is down for a long time, trying to restart");
              Updater.client.shutdown();
              Updater.client = null;
              boolean success = this.updater.stopRMAC() && this.updater.startRMAC();
              if (success) {
                healthCheckFailStart = Long.MAX_VALUE;
                Updater.client = (SocketClient) this.updater.getInstance(SocketClient.class);
                Updater.client.start();
                log.error("RMAC client restarted successfully");
              } else {
                log.error("RMAC client re-start failed");
              }
            }
          } else {
            healthCheckFailStart = Long.MAX_VALUE;
          }
          synchronized (this.thread) {
            this.thread.wait(Updater.HEALTH_CHECK_INTERVAL);
          }
        }
      } catch (Exception e) {
        log.warn("Stopped", e);
      }
    }, "Monitor");
  }

  public void start() {
    this.thread.start();
  }

  /**
   * Probe the RMAC client's health by sending a message through socket.
   *
   * @return result (true = success | false = failed)
   */
  public boolean healthCheck() {
    if (Objects.nonNull(Updater.client)) {
      String response = Updater.client.sendMessage("Check");
      return Objects.nonNull(response) && response.equals("Up");
    }
    return false;
  }

  /**
   * Interrupt the current instance's thread
   */
  public void shutdown() {
    this.thread.interrupt();
  }
}
