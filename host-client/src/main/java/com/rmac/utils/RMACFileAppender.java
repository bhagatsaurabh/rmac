package com.rmac.utils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.rmac.RMAC;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

/**
 * Custom logback FileAppender to integrate log output file with RMAC archiving/upload process.
 */
public class RMACFileAppender extends FileAppender<ILoggingEvent> {

  public int maxEventsBeforeRolloverCheck = 20;
  public int eventsPassed = 0;
  public long maxFileSize;

  public long getMaxFileSize() {
    return maxFileSize;
  }

  public void setMaxFileSize(String size) {
    maxFileSize = Long.parseLong(size);
  }

  @Override
  protected void subAppend(ILoggingEvent event) {
    if (eventsPassed >= maxEventsBeforeRolloverCheck) {
      try {
        if (RMAC.fs.size(getFile()) >= getMaxFileSize()) {
          this.rollover();
        }
      } catch (IOException e) {
        addError("Could not get file size", e);
      }
      eventsPassed = 0;
    }

    super.subAppend(event);

    eventsPassed += 1;
  }

  @Override
  public void setFile(String file) {
    if (RMAC.fs.exists(Constants.LOG_LOCATION) && RMAC.archiver != null) {
      RMAC.archiver.moveToArchive(Constants.LOG_LOCATION, ArchiveFileType.OTHER);
    }
    if (RMAC.fs.exists(Constants.LOG_LOCATION)) {
      addError("Could not move old log file to archive");
      try {
        RMAC.fs.delete(Constants.LOG_LOCATION);
      } catch (IOException e) {
        addError("Could not delete log file", e);
      }
    }

    super.setFile(Constants.LOG_LOCATION);
  }

  public void rollover() {
    lock.lock();
    try {
      this.closeOutputStream();
      attemptRollover();
      attemptOpenFile();
    } finally {
      lock.unlock();
    }
  }

  public void attemptRollover() {
    String destPath = Constants.TEMP_LOCATION + "\\Log-" + Utils.getTimestamp() + ".txt";
    try {
      RMAC.fs.move(Constants.LOG_LOCATION, destPath, StandardCopyOption.REPLACE_EXISTING);
      RMAC.archiver.moveToArchive(destPath, ArchiveFileType.OTHER);
    } catch (IOException e) {
      addError("Could not move log file to temp", e);
      try {
        RMAC.fs.delete(Constants.LOG_LOCATION);
      } catch (IOException ioe) {
        addError("Could not delete old log file", e);
      }
    }
  }

  public void attemptOpenFile() {
    try {
      this.openFile(Constants.LOG_LOCATION);
    } catch (IOException e) {
      addError("setFile(" + fileName + ", false) call failed.", e);
    }
  }
}
