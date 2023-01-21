package com.rmac.process;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
import com.rmac.process.CommandHandler;
import com.rmac.utils.ArchiveFileType;
import com.rmac.utils.Constants;
import com.rmac.utils.FileSystem;
import com.rmac.utils.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    Thread thread = spy(new Thread(ch::run));

    doThrow(IOException.class).when(ch).execute();

    RMAC.config = config;
    ch.thread = thread;
    ch.thread.start();

    verify(ch, never()).execute();
  }

  @Test
  @DisplayName("Run thread, wait interrupted")
  public void run_Interrupted() throws IOException, InterruptedException {
    CommandHandler ch = spy(CommandHandler.class);
    Config config = mock(Config.class);
    Thread thread = spy(new Thread(ch::run));

    doReturn(-1).when(config).getFetchCommandPollInterval();
    doNothing().when(ch).execute();

    RMAC.config = config;
    ch.thread = thread;
    ch.thread.start();
    ch.thread.join();

    verify(ch).execute();
  }

  @Test
  @DisplayName("Run thread succeeds")
  public void run_Success() throws IOException {
    CommandHandler ch = spy(CommandHandler.class);
    Config config = mock(Config.class);

    doReturn(1).doReturn(-1).when(config).getFetchCommandPollInterval();
    doNothing().when(ch).execute();

    RMAC.config = config;
    ch.run();

    verify(ch, times(2)).execute();
  }

  @Test
  @DisplayName("Execute when no commands")
  public void execute_NoCommands() throws IOException {
    CommandHandler ch = spy(CommandHandler.class);
    Service service = mock(Service.class);

    doReturn(new String[]{}).when(service).getCommands();

    RMAC.service = service;
    ch.execute();
  }

  @Test
  @DisplayName("Execute when command is 'panic'")
  public void execute_Command_Panic() throws Exception {
    Constants.SCRIPTS_LOCATION = "X:\\test\\RMAC\\scripts";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);

    doReturn(null).when(runtime).exec(anyString());
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    doReturn(new String[]{"panic"}).when(service).getCommands();

    RMAC.service = service;
    ch.execute();

    verify(runtime).exec(anyString());

    mockedRuntime.close();
  }

  @Test
  @DisplayName("Execute when command is 'drive' with no sub-command")
  public void execute_Command_Drive_NoCommand() throws Exception {
    Constants.SCRIPTS_LOCATION = "X:\\test\\RMAC\\scripts";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);

    doReturn(new String[]{"drive"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    ch.execute();

    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'drive list'")
  public void execute_Command_Drive_List() throws Exception {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    Constants.SCRIPTS_LOCATION = "X:\\test\\RMAC\\scripts";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);

    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00");
    doReturn(ps).when(fs).getPrintStream(anyString());
    doReturn(new String[]{"X:", "Y:", "Z:"}).when(fs).getRoots();
    doReturn(new String[]{"drive list"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.OTHER));
    assertEquals("X:\r\nY:\r\nZ:\r\n", out.toString());

    utils.close();
  }

  @Test
  @DisplayName("Execute when command is 'drive tree' without argument")
  public void execute_Command_Drive_Tree_NoArg() throws Exception {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);

    doReturn(new String[]{"drive tree"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'drive tree X'")
  public void execute_Command_Drive_Tree() throws Exception {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);
    Process proc = mock(Process.class);

    doReturn(0).when(proc).waitFor();
    doReturn(proc).when(runtime).exec(anyString());
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00");
    doReturn(new String[]{"drive tree X"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.OTHER));

    utils.close();
    mockedRuntime.close();
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
    ch.execute();

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
    ch.execute();

    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'fetch X'")
  public void execute_Command_Fetch_Success() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"fetch X:\\test.txt"}).when(service).getCommands();
    doReturn(true).when(fs).exists(eq("X:\\test.txt"));

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

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
    ch.execute();
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
    ch.execute();
  }

  @Test
  @DisplayName("Execute when command is 'system shutdown'")
  public void execute_Command_System_Shutdown() throws Exception {
    CommandHandler ch = new CommandHandler();
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);

    doReturn(new String[]{"system shutdown"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.fs = fs;
    int statusCode = catchSystemExit(ch::execute);

    assertEquals(0, statusCode);
  }

  @Test
  @DisplayName("Execute when command is 'process' without argument")
  public void execute_Command_Process_NoArg() throws IOException {
    CommandHandler ch = new CommandHandler();
    FileSystem fs = mock(FileSystem.class);
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);

    doReturn(new String[]{"process"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.fs = fs;
    ch.execute();

    verify(uploader, never()).uploadFile(anyString(), any());
  }

  @Test
  @DisplayName("Execute when command is 'process list'")
  public void execute_Command_Process_List() throws IOException, InterruptedException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);
    Process proc = mock(Process.class);

    doReturn(0).when(proc).waitFor();
    doReturn(proc).when(runtime).exec(anyString());
    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    utils.when(Utils::getTimestamp).thenReturn("0000-00-00-00-00-00");
    doReturn(new String[]{"process list"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    verify(uploader).uploadFile(anyString(), eq(ArchiveFileType.OTHER));

    utils.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Execute when command is 'process kill X'")
  public void execute_Command_Process_Kill_X() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);

    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    doReturn(new String[]{"process kill"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    mockedRuntime.verify(Runtime::getRuntime, never());

    utils.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Execute when command is 'process kill' without argument")
  public void execute_Command_Process_Kill_NoArg() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);

    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    doReturn(new String[]{"process kill test"}).when(service).getCommands();

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

    mockedRuntime.verify(Runtime::getRuntime);
    verify(runtime).exec(anyString());

    utils.close();
    mockedRuntime.close();
  }

  @Test
  @DisplayName("Execute when command is 'nircmd' without argument")
  public void execute_Command_Nircmd_NoArg() throws IOException {
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);

    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    doReturn(new String[]{"nircmd"}).when(service).getCommands();

    RMAC.service = service;
    ch.execute();

    mockedRuntime.verify(Runtime::getRuntime, never());
    verify(runtime, never()).exec(anyString());

    mockedRuntime.close();
  }

  @Test
  @DisplayName("Execute when command is 'nircmd X'")
  public void execute_Command_Nircmd_X() throws IOException {
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Runtime> mockedRuntime = mockStatic(Runtime.class);
    Runtime runtime = mock(Runtime.class);

    mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);
    doReturn(new String[]{"nircmd test"}).when(service).getCommands();

    RMAC.service = service;
    ch.execute();

    mockedRuntime.verify(Runtime::getRuntime);
    verify(runtime).exec(anyString());

    mockedRuntime.close();
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
    ch.execute();

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
    ch.execute();

    verify(uploader, never()).uploadFile(anyString(), any());

    utils.close();
  }

  @Test
  @DisplayName("Execute when command is 'cam'")
  public void execute_Command_Cam() throws IOException {
    Constants.RUNTIME_LOCATION = "X:\\test\\RMAC";
    CommandHandler ch = new CommandHandler();
    Service service = mock(Service.class);
    MockedStatic<Utils> utils = mockStatic(Utils.class);
    Process proc = mock(Process.class);
    FileUploader uploader = mock(FileUploader.class);
    FileSystem fs = mock(FileSystem.class);

    utils.when(() -> Utils.getImage(anyString())).thenReturn(proc);
    doReturn(new String[]{"cam"}).when(service).getCommands();
    doReturn(true).when(fs).exists(anyString());

    RMAC.service = service;
    RMAC.uploader = uploader;
    RMAC.fs = fs;
    ch.execute();

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
    ch.execute();
  }
}
