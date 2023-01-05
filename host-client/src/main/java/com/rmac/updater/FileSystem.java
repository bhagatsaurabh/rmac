package com.rmac.updater;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.jar.JarFile;

public class FileSystem {

  public boolean exists(String path) {
    return Files.exists(Paths.get(path));
  }

  public void delete(String path) throws IOException {
    Files.delete(Paths.get(path));
  }

  public void createDirs(String location) throws IOException {
    Files.createDirectories(Paths.get(location));
  }

  public BufferedReader getReader(String path) throws FileNotFoundException {
    return new BufferedReader(new FileReader(path));
  }

  public JarFile getJarFile(String path) throws IOException {
    return new JarFile(path);
  }
}
