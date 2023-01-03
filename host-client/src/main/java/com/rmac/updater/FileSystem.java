package com.rmac.updater;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSystem {

  public boolean exists(String path) {
    return Files.exists(Paths.get(path));
  }

  public void delete(String path) throws IOException {
    Files.delete(Paths.get(path));
  }
}
