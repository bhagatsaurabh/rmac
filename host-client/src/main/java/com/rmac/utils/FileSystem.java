package com.rmac.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public long size(String path) throws IOException {
    return Files.size(Paths.get(path));
  }
}
