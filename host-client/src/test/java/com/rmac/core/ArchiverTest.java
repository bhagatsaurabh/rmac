package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ArchiverTest {

  @Test
  @DisplayName("Verify folders")
  public void verifyFolders() throws IOException {
    Constants.ARCHIVES_LOCATION = "X:\\test\\Live\\archives";
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    Constants.OTHER_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\other";

    FileSystem fs = mock(FileSystem.class);

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.verifyFolders();

    verify(fs, times(2)).createDirs(eq(Constants.ARCHIVES_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.SCREEN_ARCHIVE_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.LOG_ARCHIVE_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.OTHER_ARCHIVE_LOCATION));
  }

  @Test
  @DisplayName("Verify folders fails")
  public void verifyFolders_Failed() throws IOException {
    Constants.ARCHIVES_LOCATION = "X:\\test\\Live\\archives";
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";

    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).createDirs(eq(Constants.SCREEN_ARCHIVE_LOCATION));

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.verifyFolders();

    verify(fs, times(2)).createDirs(eq(Constants.ARCHIVES_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.LOG_ARCHIVE_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(fs, times(2)).createDirs(eq(Constants.SCREEN_ARCHIVE_LOCATION));
    verify(fs, never()).createDirs(eq(Constants.OTHER_ARCHIVE_LOCATION));
  }

  @Test
  @DisplayName("Move to archive, move fails and max staging size not reached")
  public void moveToArchive_Move_Failed_StagingThreshold_NotReached() throws IOException {
    Archiver archiver = spy(Archiver.class);
    FileSystem fs = mock(FileSystem.class);
    Config config = mock(Config.class);

    doThrow(IOException.class).when(fs).move(eq("X:\\test\\Live\\test.txt"), anyString());
    doReturn(100L).when(fs).getDirectorySize(anyString());
    doReturn(1000L).when(config).getMaxStagingSize();

    RMAC.fs = fs;
    RMAC.config = config;
    archiver.moveToArchive("X:\\test\\Live\\test.txt", ArchiveFileType.OTHER);

    verify(archiver, never()).createNewArchive(anyString(), anyString());
  }

  @Test
  @DisplayName("Move to archive, move succeeds and max staging size errors")
  public void moveToArchive_Move_Success_StagingThreshold_Error() throws IOException {
    Archiver archiver = spy(Archiver.class);
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).getDirectorySize(anyString());

    RMAC.fs = fs;
    archiver.moveToArchive("X:\\test\\Live\\test.txt", ArchiveFileType.OTHER);

    verify(archiver, never()).createNewArchive(anyString(), anyString());
  }

  @Test
  @DisplayName("Move to archive, move succeeds and max storage size errors")
  public void moveToArchive_Move_Success_StorageThreshold_Error() throws IOException {
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    Archiver archiver = spy(Archiver.class);
    FileSystem fs = mock(FileSystem.class);
    Config config = mock(Config.class);

    doReturn(1000L).when(fs).getDirectorySize(anyString());
    doReturn(100L).when(config).getMaxStagingSize();
    doThrow(IOException.class).when(fs).getDirectorySize(eq(Constants.PENDING_ARCHIVES_LOCATION));
    doNothing().when(archiver).createNewArchive(anyString(), anyString());

    RMAC.fs = fs;
    RMAC.config = config;
    archiver.moveToArchive("X:\\test\\Live\\archives\\other\\test.txt", ArchiveFileType.OTHER);

    verify(archiver).createNewArchive(eq("X:\\test\\Live\\archives\\other"),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
  }

  @Test
  @DisplayName("Move to archive, move succeeds and max storage size not reached")
  public void moveToArchive_Move_Success_StorageThreshold_NotReached() throws IOException {
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    FileSystem fs = mock(FileSystem.class);
    Config config = mock(Config.class);

    doReturn(1000L).when(fs).getDirectorySize(anyString());
    doReturn(100L).when(config).getMaxStagingSize();
    doReturn(100L).when(fs).getDirectorySize(eq(Constants.PENDING_ARCHIVES_LOCATION));
    doReturn(1000L).when(config).getMaxStorageSize();
    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    doReturn("X:\\test\\Live\\archives\\other").when(archiver)
        .getArchiveLocation(eq(ArchiveFileType.OTHER));
    doNothing().when(archiver).createNewArchive(anyString(), anyString());

    RMAC.config = config;
    archiver.moveToArchive("X:\\test\\Live\\archives\\other\\test.txt", ArchiveFileType.OTHER);

    verify(archiver).createNewArchive(eq("X:\\test\\Live\\archives\\other"),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(fs, never()).deleteOldestFile(eq(Constants.PENDING_ARCHIVES_LOCATION));
  }

  @Test
  @DisplayName("Move to archive, move succeeds and max storage size reached")
  public void moveToArchive_Move_Success_StorageThreshold_Reached() throws IOException {
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    FileSystem fs = mock(FileSystem.class);
    Config config = mock(Config.class);

    doReturn(1000L).when(fs).getDirectorySize(anyString());
    doReturn(100L).when(config).getMaxStagingSize();
    doReturn(1000L).when(fs).getDirectorySize(eq(Constants.PENDING_ARCHIVES_LOCATION));
    doReturn(100L).when(config).getMaxStorageSize();
    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    doReturn("X:\\test\\Live\\archives\\other").when(archiver)
        .getArchiveLocation(eq(ArchiveFileType.OTHER));
    doNothing().when(archiver).createNewArchive(anyString(), anyString());

    RMAC.config = config;
    archiver.moveToArchive("X:\\test\\Live\\archives\\other\\test.txt", ArchiveFileType.OTHER);

    verify(archiver).createNewArchive(eq("X:\\test\\Live\\archives\\other"),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(fs).deleteOldestFile(eq(Constants.PENDING_ARCHIVES_LOCATION));
  }

  @Test
  @DisplayName("Upload archives when listing files in archive directories fails")
  public void uploadArchive_Listing_Failed() throws IOException {
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.OTHER_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\other";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).list(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.uploadArchive();

    verify(archiver, never()).createNewArchive(anyString(), anyString());
  }

  @Test
  @DisplayName("Upload archives when archive directories are empty")
  public void uploadArchive_Listing_Empty() throws IOException {
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.OTHER_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\other";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    FileSystem fs = mock(FileSystem.class);

    doAnswer(invc -> Stream.of()).when(fs).list(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.uploadArchive();

    verify(archiver, never()).createNewArchive(anyString(), anyString());
  }

  @Test
  @DisplayName("Upload archives succeeds")
  public void uploadArchive_Success() throws IOException {
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.OTHER_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\other";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.PENDING_ARCHIVES_LOCATION = "X:\\test\\Live\\archives\\pending";
    FileSystem fs = mock(FileSystem.class);
    FileUploader uploader = mock(FileUploader.class);

    doAnswer(invc -> Stream.of(Paths.get("X:\\test"))).when(fs).list(anyString());

    RMAC.fs = fs;
    RMAC.uploader = uploader;
    Archiver archiver = spy(Archiver.class);
    doNothing().when(archiver).createNewArchive(anyString(), anyString());
    archiver.uploadArchive();

    verify(archiver).createNewArchive(eq(Constants.SCREEN_ARCHIVE_LOCATION),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(archiver).createNewArchive(eq(Constants.OTHER_ARCHIVE_LOCATION),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(archiver).createNewArchive(eq(Constants.LOG_ARCHIVE_LOCATION),
        eq(Constants.PENDING_ARCHIVES_LOCATION));
    verify(uploader).uploadFile(eq("X:\\test"), eq(ArchiveFileType.ARCHIVE));

  }

  @Test
  @DisplayName("Cleanup when moving files failed")
  public void cleanUp_Move_Failed() throws IOException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Constants.CURRENT_LOCATION = "X:\\test\\Live";
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).list(anyString());
    doThrow(IOException.class).when(fs).move(anyString(), anyString(), any());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    doNothing().when(archiver).moveToArchive(anyString(), any());
    RMAC.archiver = archiver;
    archiver.cleanUp();

    verify(archiver, never()).moveToArchive(anyString(), any());
  }

  @Test
  @DisplayName("Cleanup succeeds")
  public void cleanUp_Success() throws IOException {
    Constants.KEYLOG_LOCATION = "X:\\test\\Live\\key.txt";
    Constants.CURRENT_LOCATION = "X:\\test\\Live";
    Constants.TEMP_LOCATION = "X:\\temp";
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);

    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00");
    doAnswer(invc -> Stream.of(Paths.get("X:\\test\\Live\\test.mkv"))).when(fs)
        .list(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    doNothing().when(archiver).moveToArchive(anyString(), any());
    RMAC.archiver = archiver;
    archiver.cleanUp();

    verify(archiver).moveToArchive(eq(Constants.TEMP_LOCATION + "\\Key-0000-00-00-00-00-00.txt"),
        eq(ArchiveFileType.KEY));

    utils.close();
  }

  @Test
  @DisplayName("Get archive location")
  public void getArchiveLocation() {
    Constants.SCREEN_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\screen";
    Constants.LOG_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\log";
    Constants.OTHER_ARCHIVE_LOCATION = "X:\\test\\Live\\archives\\other";

    FileSystem fs = mock(FileSystem.class);

    RMAC.fs = fs;
    Archiver archiver = new Archiver();

    assertEquals(archiver.getArchiveLocation(ArchiveFileType.SCREEN),
        Constants.SCREEN_ARCHIVE_LOCATION);
    assertEquals(archiver.getArchiveLocation(ArchiveFileType.KEY), Constants.LOG_ARCHIVE_LOCATION);
    assertEquals(archiver.getArchiveLocation(ArchiveFileType.OTHER),
        Constants.OTHER_ARCHIVE_LOCATION);
    assertNull(archiver.getArchiveLocation(ArchiveFileType.ARCHIVE));
  }

  @Test
  @DisplayName("Create new archive when listing files fails")
  public void createNewArchive_Listing_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);

    doThrow(IOException.class).when(fs).list(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.createNewArchive("X:\\test\\Live\\archives\\screen",
        "X:\\test\\Live\\archives\\pending");

    verify(fs, never()).getZipOutStream(anyString());
  }

  @Test
  @DisplayName("Create new archive when opening zip fails")
  public void createNewArchive_ZipOpen_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);

    doAnswer(invc -> Stream.of(Paths.get("X:\\test\\Live\\archives\\screen\\test.mkv"))).when(fs)
        .list(anyString());
    doThrow(IOException.class).when(fs).getZipOutStream(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.createNewArchive("X:\\test\\Live\\archives\\screen",
        "X:\\test\\Live\\archives\\pending");

    verify(fs, never()).copy(anyString(), any());
    verify(fs, never()).deleteAll(anyString());
  }

  @Test
  @DisplayName("Create new archive partial success, delete zipped files fails")
  public void createNewArchive_Partial_Success_Delete_Failed() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    ZipOutputStream zos = mock(ZipOutputStream.class);

    doAnswer(invc -> Stream.of(
        Paths.get("X:\\test\\Live\\archives\\screen\\test1.mkv"),
        Paths.get("X:\\test\\Live\\archives\\screen\\test2.mkv")
    )).when(fs).list(anyString());
    doReturn(zos).when(fs).getZipOutStream(anyString());
    doThrow(IOException.class).doNothing().when(zos).putNextEntry(any());
    doThrow(IOException.class).when(fs).deleteAll(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.createNewArchive("X:\\test\\Live\\archives\\screen",
        "X:\\test\\Live\\archives\\pending");

    verify(fs).copy(eq("X:\\test\\Live\\archives\\screen\\test2.mkv"), eq(zos));
  }

  @Test
  @DisplayName("Create new archive succeeds")
  public void createNewArchive_Success() throws IOException {
    FileSystem fs = mock(FileSystem.class);
    ZipOutputStream zos = mock(ZipOutputStream.class);

    doAnswer(invc -> Stream.of(
        Paths.get("X:\\test\\Live\\archives\\screen\\test1.mkv"),
        Paths.get("X:\\test\\Live\\archives\\screen\\test2.mkv")
    )).when(fs).list(anyString());
    doReturn(zos).when(fs).getZipOutStream(anyString());

    RMAC.fs = fs;
    Archiver archiver = spy(Archiver.class);
    archiver.createNewArchive("X:\\test\\Live\\archives\\screen",
        "X:\\test\\Live\\archives\\pending");

    verify(fs, times(2)).copy(anyString(), eq(zos));
    verify(fs).deleteAll(anyString());
  }
}
