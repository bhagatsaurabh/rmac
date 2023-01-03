package com.rmac.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystem {

  public boolean exists(String location) {
    return Files.exists(Paths.get(location));
  }

  public void createDirs(String location) throws IOException {
    Files.createDirectories(Paths.get(location));
  }

  public void move(String source, String target, CopyOption option) throws IOException {
    Files.move(Paths.get(source), Paths.get(target), option);
  }

  public void move(String source, String target) throws IOException {
    Files.move(Paths.get(source), Paths.get(target));
  }

  public Stream<Path> list(String dir) throws IOException {
    return Files.list(Paths.get(dir));
  }

  public void delete(String path) throws IOException {
    Files.deleteIfExists(Paths.get(path));
  }

  public void create(String path) throws IOException {
    Files.createFile(Paths.get(path));
  }

  public RandomAccessFile createRandomAccessFile(String path, String mode)
      throws FileNotFoundException {
    return new RandomAccessFile(path, mode);
  }

  public InputStream getResourceAsStream(Class<?> T, String path) {
    return T.getResourceAsStream(path);
  }

  public void copy(InputStream is, String target, CopyOption option) throws IOException {
    Files.copy(is, Paths.get(target), option);
  }

  public void copy(String path, OutputStream os) throws IOException {
    Files.copy(Paths.get(path), os);
  }

  public long size(String path) throws IOException {
    return Files.size(Paths.get(path));
  }

  public void deleteAll(String path) throws IOException {
    list(path).forEach(filePath -> {
      try {
        Files.delete(filePath);
      } catch (IOException e) {
        System.err.println("Failed to delete file");
      }
    });
  }

  /**
   * Get the size of given <code>file</code> in bytes.
   *
   * @param path Path to the file.
   * @return Size of the file in bytes.
   */
  public long getFileSize(Path path) throws IOException {
    return Files.size(path);
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
  public long getDirectorySize(String dirPath) throws IOException {
    long size;
    Stream<Path> walk = Files.walk(Paths.get(dirPath));
    size = walk
        .filter(Files::isRegularFile)
        .mapToLong((path) -> {
          try {
            return getFileSize(path);
          } catch (IOException e) {
            System.err.println("Could not get file size");
            return 0L;
          }
        })
        .sum();

    walk.close();
    return size;
  }

  /**
   * Traverse all the root level files in a given directory and delete the one which is oldest.
   *
   * @param dirPath Location of the directory.
   */
  public void deleteOldestFile(String dirPath) throws IOException {
    long oldestModified = Long.MAX_VALUE;
    Path oldestFile = null;

    Path[] files = list(dirPath).toArray(Path[]::new);
    for (Path path : files) {
      long time = getLastModified(path.toAbsolutePath().toString());
      if (time < oldestModified) {
        oldestModified = time;
        oldestFile = path;
      }
    }

    if (Objects.nonNull(oldestFile)) {
      delete(oldestFile.toAbsolutePath().toString());
    }
  }

  public long getLastModified(String path) throws IOException {
    return Files.getLastModifiedTime(Paths.get(path)).toMillis();
  }
}
