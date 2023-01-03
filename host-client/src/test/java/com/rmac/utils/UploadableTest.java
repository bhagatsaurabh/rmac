package com.rmac.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rmac.RMAC;
import com.rmac.core.Archiver;
import com.rmac.core.MegaClient;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UploadableTest {

  @Test
  @DisplayName("Create an Uploadable")
  public void createUploadable() {
    Consumer<Uploadable> consumer = (uploadable -> {
    });
    Uploadable uploadable = new Uploadable("X:\\test\\RMAC\\test.txt", ArchiveFileType.OTHER,
        consumer);

    assertEquals(ArchiveFileType.OTHER, uploadable.getType());
    assertEquals("X:\\test\\RMAC\\test.txt", uploadable.getFile());
    assertEquals(consumer, uploadable.getCallback());
    assertEquals(State.NEW, uploadable.getThread().getState());
  }

  @Test
  @DisplayName("Run Uploadable thread, upload fails")
  public void runUploadableThread_Upload_Fails() throws InterruptedException, IOException {
    @SuppressWarnings("unchecked")
    Consumer<Uploadable> consumer = (Consumer<Uploadable>) mock(Consumer.class);

    Archiver mockArchiver = mock(Archiver.class);
    RMAC.archiver = mockArchiver;
    doNothing().when(mockArchiver).moveToArchive(any(), any());
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    MegaClient mockMega = mock(MegaClient.class);
    RMAC.mega = mockMega;
    when(mockMega.uploadFile(any(), any())).thenReturn(false);

    Uploadable uploadable = new Uploadable("X:\\test\\RMAC\\test.txt", ArchiveFileType.OTHER,
        consumer);
    uploadable.getThread().start();
    uploadable.getThread().join();

    verify(mockArchiver).moveToArchive(eq("X:\\test\\RMAC\\test.txt"), eq(ArchiveFileType.OTHER));
    verify(mockFs, never()).delete(eq("X:\\test\\RMAC\\test.txt"));
    verify(consumer).accept(eq(uploadable));
  }

  @Test
  @DisplayName("Run Uploadable thread, file deletion fails")
  public void runUploadableThread_Delete_Fails() throws InterruptedException, IOException {
    @SuppressWarnings("unchecked")
    Consumer<Uploadable> consumer = (Consumer<Uploadable>) mock(Consumer.class);

    Archiver mockArchiver = mock(Archiver.class);
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    doThrow(IOException.class).when(mockFs).delete(any());
    MegaClient mockMega = mock(MegaClient.class);
    RMAC.mega = mockMega;
    when(mockMega.uploadFile(any(), any())).thenReturn(true);

    Uploadable uploadable = new Uploadable("X:\\test\\RMAC\\test.txt", ArchiveFileType.OTHER,
        consumer);
    uploadable.getThread().start();
    uploadable.getThread().join();

    verify(mockArchiver, never()).moveToArchive(eq("X:\\test\\RMAC\\test.txt"),
        eq(ArchiveFileType.OTHER));
    verify(mockFs).delete(eq("X:\\test\\RMAC\\test.txt"));
    verify(consumer).accept(eq(uploadable));
  }

  @Test
  @DisplayName("Run Uploadable thread, file deletion succeeds")
  public void runUploadableThread_Delete_Success() throws InterruptedException, IOException {
    @SuppressWarnings("unchecked")
    Consumer<Uploadable> consumer = (Consumer<Uploadable>) mock(Consumer.class);

    Archiver mockArchiver = mock(Archiver.class);
    FileSystem mockFs = mock(FileSystem.class);
    RMAC.fs = mockFs;
    MegaClient mockMega = mock(MegaClient.class);
    RMAC.mega = mockMega;
    when(mockMega.uploadFile(any(), any())).thenReturn(true);

    Uploadable uploadable = new Uploadable("X:\\test\\RMAC\\test.txt", ArchiveFileType.OTHER,
        consumer);
    uploadable.getThread().start();
    uploadable.getThread().join();

    verify(mockArchiver, never()).moveToArchive(eq("X:\\test\\RMAC\\test.txt"),
        eq(ArchiveFileType.OTHER));
    verify(mockFs).delete(eq("X:\\test\\RMAC\\test.txt"));
    verify(consumer).accept(eq(uploadable));
  }

  @Test
  @DisplayName("Execute Uploadable thread")
  public void execute() {
    Thread mockThread = mock(Thread.class);
    Uploadable uploadable = new Uploadable("X:\\test\\RMAC\\test.txt", ArchiveFileType.OTHER,
        (u) -> {
        });
    uploadable.setThread(mockThread);
    uploadable.execute();

    verify(mockThread).start();
  }

  @Test
  @DisplayName("Compare Uploadable priorities")
  public void comparePriority() {
    Uploadable uploadable1 = new Uploadable("X:\\test\\RMAC\\test1.txt", ArchiveFileType.OTHER,
        (u) -> {
        });
    Uploadable uploadable2 = new Uploadable("X:\\test\\RMAC\\test2.txt", ArchiveFileType.SCREEN,
        (u) -> {
        });

    assertEquals(uploadable2.compareTo(uploadable1), 1);
  }
}
