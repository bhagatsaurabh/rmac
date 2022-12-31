package com.rmac.utils;

import java.io.IOException;
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
}
