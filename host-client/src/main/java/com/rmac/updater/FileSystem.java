package com.rmac.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.file.CopyOption;
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

  public FileOutputStream getFOS(String path) throws FileNotFoundException {
    return new FileOutputStream(path);
  }

  public void create(String path) throws IOException {
    Files.createFile(Paths.get(path));
  }

  public void copy(String source, String target, CopyOption option) throws IOException {
    Files.copy(Paths.get(source), Paths.get(target), option);
  }

  public RandomAccessFile createRandomAccessFile(String path, String mode)
      throws FileNotFoundException {
    return new RandomAccessFile(path, mode);
  }

  public BufferedReader getReader(InputStream is) {
    return new BufferedReader(new InputStreamReader(is));
  }

  public BufferedWriter getWriter(OutputStream os) {
    return new BufferedWriter(new OutputStreamWriter(os));
  }
}
