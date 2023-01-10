package com.rmac.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.rmac.RMAC;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Uploadable;
import java.util.Queue;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class FileUploaderTest {

  @Test
  @DisplayName("File uploader initialization when MEGA server failed to start")
  public void fileUploader_NoMegaServer() {
    Config config = mock(Config.class);
    MegaClient mega = mock(MegaClient.class);

    doReturn(false).when(mega).startServer();

    RMAC.config = config;
    RMAC.mega = mega;

    new FileUploader();

    verify(mega).startServer();
    verify(mega, never()).login(anyString(), anyString(), anyBoolean());
  }

  @Test
  @DisplayName("File uploader initialization when MEGA server started successfully")
  public void fileUploader_Success() {
    Config config = mock(Config.class);
    MegaClient mega = mock(MegaClient.class);

    doReturn(true).when(mega).startServer();
    doReturn("testuser").when(config).getMegaUser();
    doReturn("testpass").when(config).getMegaPass();

    RMAC.config = config;
    RMAC.mega = mega;

    new FileUploader();

    verify(mega).startServer();
    verify(mega).login(eq("testuser"), eq("testpass"), eq(false));
    verify(config).getMegaUser();
    verify(config).getMegaPass();
  }

  @Test
  @DisplayName("Upload complete")
  public void uploadComplete() {
    MegaClient mega = mock(MegaClient.class);

    RMAC.mega = mega;
    FileUploader uploader = spy(FileUploader.class);
    doNothing().when(uploader).doUploads();

    uploader.runningUploads = 5;
    uploader.uploadComplete();

    assertEquals(4, uploader.runningUploads);
    verify(uploader).doUploads();
  }

  @Test
  @DisplayName("Upload started")
  public void uploadStarted() {
    MegaClient mega = mock(MegaClient.class);

    RMAC.mega = mega;
    FileUploader uploader = spy(FileUploader.class);
    doNothing().when(uploader).doUploads();

    uploader.runningUploads = 3;
    uploader.uploadStarted();

    assertEquals(4, uploader.runningUploads);
  }

  @Test
  @DisplayName("Do uploads when running uploads is less than max configured")
  public void doUploads_Threshold_Reached() {
    Config config = mock(Config.class);

    doReturn(3).when(config).getMaxParallelUploads();

    FileUploader uploader = spy(FileUploader.class);
    Queue<Uploadable> queue = spy(uploader.queue);

    RMAC.config = config;
    uploader.runningUploads = 3;
    uploader.doUploads();

    verify(queue, never()).poll();
  }

  @Test
  @DisplayName("Do uploads when queue is empty")
  public void doUploads_Empty_Queue() {
    Config config = mock(Config.class);

    doReturn(3).when(config).getMaxParallelUploads();

    FileUploader uploader = spy(FileUploader.class);
    Queue<Uploadable> queue = spy(uploader.queue);
    doReturn(null).when(queue).poll();

    RMAC.config = config;
    uploader.runningUploads = 1;
    uploader.doUploads();
  }

  @Test
  @DisplayName("Do uploads succeeds when uploadables are in queue")
  public void doUploads_Success() {
    MegaClient mega = mock(MegaClient.class);
    Config config = mock(Config.class);
    Uploadable uploadable1 = mock(Uploadable.class);
    Uploadable uploadable2 = mock(Uploadable.class);

    doReturn(3).when(config).getMaxParallelUploads();

    RMAC.mega = mega;
    RMAC.config = config;
    FileUploader uploader = spy(FileUploader.class);
    uploader.queue.add(uploadable1);
    uploader.queue.add(uploadable2);

    uploader.runningUploads = 1;
    uploader.doUploads();

    verify(uploadable1).execute();
    verify(uploadable2).execute();
    verify(uploader, times(2)).uploadStarted();
  }

  @Test
  @DisplayName("Upload file when file type is ARCHIVE and network is down")
  public void uploadFile_ARCHIVE_NetDown() {
    MegaClient mega = mock(MegaClient.class);
    RMAC.mega = mega;
    FileUploader uploader = spy(FileUploader.class);

    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(false);

    uploader.uploadFile("X:\\test.zip", ArchiveFileType.ARCHIVE);

    verify(uploader, never()).doUploads();

    connectivity.close();
  }

  @Test
  @DisplayName("Upload file when file type is not ARCHIVE and network is down")
  public void uploadFile_Not_ARCHIVE_NetDown() {
    MegaClient mega = mock(MegaClient.class);
    Config config = mock(Config.class);
    Archiver archiver = mock(Archiver.class);
    RMAC.mega = mega;
    RMAC.config = config;
    RMAC.archiver = archiver;
    FileUploader uploader = spy(FileUploader.class);

    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(false);
    doReturn(true).when(config).getVideoUpload();

    uploader.uploadFile("X:\\test.mkv", ArchiveFileType.SCREEN);

    verify(uploader, never()).doUploads();
    verify(archiver).moveToArchive(eq("X:\\test.mkv"), eq(ArchiveFileType.SCREEN));

    connectivity.close();
  }

  @Test
  @DisplayName("Upload file when filetype is not ARCHIVE and network is up and filetype upload is disabled")
  public void uploadFile_Not_ARCHIVE_NetUp_ConfigDisabled() {
    MegaClient mega = mock(MegaClient.class);
    Config config = mock(Config.class);
    Archiver archiver = mock(Archiver.class);
    RMAC.mega = mega;
    RMAC.config = config;
    RMAC.archiver = archiver;
    FileUploader uploader = spy(FileUploader.class);

    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);
    doReturn(false).when(config).getLogFileUpload();

    uploader.uploadFile("X:\\test.txt", ArchiveFileType.KEY);

    verify(uploader, never()).doUploads();
    verify(archiver).moveToArchive(eq("X:\\test.txt"), eq(ArchiveFileType.KEY));

    connectivity.close();
  }

  @Test
  @DisplayName("Upload file succeeds")
  public void uploadFile_Success() {
    MegaClient mega = mock(MegaClient.class);
    Config config = mock(Config.class);
    Archiver archiver = mock(Archiver.class);
    RMAC.mega = mega;
    RMAC.config = config;
    RMAC.archiver = archiver;
    FileUploader uploader = spy(FileUploader.class);

    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);

    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);
    doReturn(false).when(config).getLogFileUpload();

    uploader.uploadFile("X:\\test.zip", ArchiveFileType.ARCHIVE);

    verify(uploader).doUploads();
    verify(archiver, never()).moveToArchive(anyString(), any());
    assertEquals(1, uploader.queue.size());

    connectivity.close();
  }
}
