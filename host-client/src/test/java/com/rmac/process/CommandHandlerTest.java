package com.rmac.process;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.rmac.core.Config;
import com.rmac.core.FileUploader;
import com.rmac.core.Service;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.IOException;
import java.lang.Thread.State;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class CommandHandlerTest {

  @Test
  @DisplayName("Create thread")
  public void createThread() {
    CommandHandler ch = spy(CommandHandler.class);

    assertEquals(State.NEW, ch.thread.getState());
  }

  @Test
  @DisplayName("Start")
  public void start() {
    CommandHandler ch = spy(CommandHandler.class);
    Thread thread = mock(Thread.class);

    ch.thread = thread;
    ch.start();

    verify(thread).start();
  }

  @Test
  @DisplayName("Shutdown")
  public void shutdown() {
    CommandHandler ch = spy(CommandHandler.class);
    Thread thread = mock(Thread.class);

    ch.thread = thread;
    ch.shutdown();

    verify(thread).interrupt();
  }

  @Test
  @DisplayName("Run thread, command execution fails")
  public void run_Failed() throws IOException {
    CommandHandler ch = spy(CommandHandler.class);
    Config config = mock(Config.class);

    doThrow(IOException.class).when(ch).execute(new String[]{});

    RMAC.config = config;
    ch.thread = new Thread(ch::run);
    ch.thread.start();

    verify(ch, never()).execute(new String[]{});
  }

  @Test
  @DisplayName("Run thread, wait interrupted")
  public void run_Interrupted() throws IOException, InterruptedException {
    CommandHandler ch = spy(CommandHandler.class);
    Config config = mock(Config.class);
    Service service = mock(Service.class);

    doReturn(-1).when(config).getFetchCommandPollInterval();
    doNothing().when(ch).execute(any(String[].class));
    doReturn(new String[]{}).when(service).getCommands();

    RMAC.config = config;
    RMAC.service = service;
    ch.thread = new Thread(ch::run);
    ch.thread.start();
    ch.thread.join();

    verify(ch).execute(eq(new String[]{}));
  }

  @Test
  @DisplayName("Run thread succeeds")
  public void run_Success() throws IOException {
    CommandHandler ch = spy(CommandHandler.class);
    Config config = mock(Config.class);
    Service service = mock(Service.class);

    doReturn(1).doReturn(-1).when(config).getFetchCommandPollInterval();
    doNothing().when(ch).execute(new String[]{});
    doReturn(new String[]{}).when(service).getCommands();

    RMAC.config = config;
    RMAC.service = service;
    ch.run();

    verify(ch, times(2)).execute(new String[]{});
  }

  @Test
  @DisplayName("Execute when no commands")
  public void execute_NoCommands() throws IOException {
    CommandHandler ch = spy(CommandHandler.class);
    Service service = mock(Service.class);

    doReturn(new String[]{}).when(service).getCommands();

    RMAC.service = service;
    ch.execute(new String[]{});
  }

  @Test
  @DisplayName("Execute when command is 'fetch' without argument")
  public void execute_Command_Fetch_NoArg() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"fetch"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"fetch"});

    verify(fs, never()).exists(anyString());
    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'fetch' with invalid argument")
  public void execute_Command_Fetch_InvalidArg() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"fetch "}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"fetch "});

    verify(fs, never()).exists(anyString());
    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'fetch X' and file doesn't exist")
  public void execute_Command_Fetch_NoFile() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"fetch X:\\test.txt"}).when(service).getCommands();
    doReturn(false).when(fs).exists(eq("X:\\test.txt"));

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"fetch X:\\test.txt"});

    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'fetch X'")
  public void execute_Command_Fetch_Success() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(true).when(fs).exists(eq("X:\\test.txt"));

    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"fetch X:\\test.txt"});

    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.OTHER));
  }

  @Test
  @DisplayName("Execute when command is 'system' without argument")
  public void execute_Command_System_NoArg() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"system"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.fs = fs;
    ch.execute(new String[]{"system"});
  }

  @Test
  @DisplayName("Execute when command is 'system' with unrecognizable argument")
  public void execute_Command_System_Invalid_Arg() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"system xyz"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.fs = fs;
    ch.execute(new String[]{"system xyz"});
  }

  @Test
  @DisplayName("Execute when command is 'system shutdown'")
  public void execute_Command_System_Shutdown() throws Exception {
    CommandHandler ch = new CommandHandler();

    RMAC.fs = mock(FileSystem.class);
    int statusCode = catchSystemExit(() -> ch.execute(new String[]{"system shutdown"}));

    assertEquals(0, statusCode);
  }

  @Test
  @DisplayName("Execute when command is 'cam' and process is interrupted")
  public void execute_Command_Cam_Interrupted() throws IOException, InterruptedException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    Process proc = mock(Process.class);
    FileUploader uploader = mock(FileUploader.class);

    utils.when(() -> Utils.getImage(anyString())).thenReturn(proc);
    doThrow(InterruptedException.class).when(proc).waitFor();
    doReturn(new String[]{"cam"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    ch.execute(new String[]{"cam"});

    verify(uploader, never()).uploadFile(anyString(), any());

    utils.close();
  }

  @Test
  @DisplayName("Execute when command is 'cam' and file doesn't exist")
  public void execute_Command_Cam_NoFile() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    Process proc = mock(Process.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);

    utils.when(() -> Utils.getImage(anyString())).thenReturn(proc);
    doReturn(new String[]{"cam"}).when(service).getCommands();
    doReturn(false).when(fs).exists(anyString());

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"cam"});

    verify(uploader, never()).uploadFile(anyString(), any());

    utils.close();
  }

  @Test
  @DisplayName("Execute when command is 'cam'")
  public void execute_Command_Cam() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    Process proc = mock(Process.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);

    utils.when(() -> Utils.getImage(anyString())).thenReturn(proc);
    doReturn(true).when(fs).exists(anyString());

    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute(new String[]{"cam"});

    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.OTHER));

    utils.close();
  }

  @Test
  @DisplayName("Execute when command is unrecognized")
  public void execute_Command_Unrecognized() throws IOException {
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);

    doReturn(new String[]{"test"}).when(service).getCommands();

    RMAC.service = service;
    ch.execute(new String[]{"test"});
  }

  @Test
  @DisplayName("Execute when command is 'config' and invalid arguments")
  public void execute_Config_InvalidArgs()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    CommandHandler ch = new CommandHandler();
    Config config = mock(Config.class);

    RMAC.config = config;
    ch.execute(new String[]{"config VideoUpload"});

    verify(config, times(0)).setConfig(anyString(), anyString(), anyBoolean());
  }

  @Test
  @DisplayName("Execute when command is 'config' and set config fails")
  public void execute_Config_Failed()
      throws IOException, NoSuchFieldException, IllegalAccessException {
    CommandHandler ch = new CommandHandler();
    Config config = mock(Config.class);

    doThrow(IllegalAccessException.class).when(config).setConfig("VideoUpload", "false", true);

    RMAC.config = config;
    ch.execute(new String[]{"config VideoUpload false"});
  }
}
