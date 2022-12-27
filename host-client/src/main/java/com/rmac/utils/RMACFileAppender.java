package com.rmac.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.rmac.Main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Custom logback FileAppender to integrate log output file with RMAC archiving/upload process.
 */
public class RMACFileAppender extends FileAppender<ILoggingEvent> {

  public int maxEventsBeforeRolloverCheck = 20;
  private int eventsPassed = 0;
  private long maxFileSize;

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(String size) {
    maxFileSize = Long.parseLong(size);
  }

  @Override
  protected void subAppend(ILoggingEvent event) {
    if (eventsPassed >= maxEventsBeforeRolloverCheck) {
      if (new File(getFile()).length() >= getMaxFileSize()) {
        this.rollover();
      }
      eventsPassed = 0;
    }

    super.subAppend(event);

    eventsPassed += 1;
  }

  @Override
  public void setFile(String file) {
    File logFile = new File(Constants.LOG_LOCATION);
    if (logFile.exists() && Main.archiver != null) {
      Main.archiver.moveToArchive(logFile, ArchiveFileType.OTHER);
    }
    if (logFile.exists()) {
      addError("Could not move old log file to archive");
      logFile.delete();
    }

    super.setFile(logFile.getAbsolutePath());
  }

  private void rollover() {
    lock.lock();
    try {
      this.closeOutputStream();
      attemptRollover();
      attemptOpenFile();
    } finally {
      lock.unlock();
    }
  }

  private void attemptRollover() {
    String destPath = Constants.TEMP_LOCATION + "\\Log-" + Utils.getTimestamp() + ".txt";
    File source = new File(Constants.LOG_LOCATION);
    try {
      Files.move(source.toPath(), Paths.get(destPath), StandardCopyOption.REPLACE_EXISTING);
      Main.archiver.moveToArchive(new File(destPath), ArchiveFileType.OTHER);
    } catch (IOException e) {
      addError("Could not move log file to temp", e);
      source.delete();
    }
  }

  private void attemptOpenFile() {
    try {
      this.openFile(Constants.LOG_LOCATION);
    } catch (IOException e) {
      addError("setFile(" + fileName + ", false) call failed.", e);
    }
  }
}
