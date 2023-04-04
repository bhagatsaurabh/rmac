package com.rmac.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rmac.RMAC;
import com.rmac.comms.BridgeClient;
import com.rmac.comms.Message;
import com.rmac.utils.FileSystem;
import java.io.IOException;
import java.util.Scanner;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ServiceTest {

  @Test
  @DisplayName("Get commands when network is down")
  public void getCommands_NetworkDown() {
    // Because of weird static class initialization issue: Class not found
    RMAC.config = null;

    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    Service service = new Service();

    connectivity.when(Connectivity::checkNetworkState).thenReturn(false);

    String[] result = service.getCommands();

    assertArrayEquals(new String[]{}, result);

    connectivity.close();
  }

  @Test
  @DisplayName("Get commands fails")
  public void getCommands_Failed() throws IOException {
    Service service = new Service();
    Config config = mock(Config.class);
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    when(httpClient.execute(any())).thenThrow(IOException.class);
    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);

    RMAC.config = config;
    String[] result = service.getCommands();

    assertArrayEquals(new String[]{}, result);

    connectivity.close();
    mockedClient.close();
  }

  @Test
  @DisplayName("Get commands fails while parsing json")
  public void getCommands_Parse_Failed() throws IOException {
    Service service = new Service();
    Config config = mock(Config.class);
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    Gson mockGson = mock(Gson.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("");
    when(httpClient.execute(any())).thenReturn(mockResponse);
    when(mockGson.fromJson(anyString(), any())).thenThrow(JsonSyntaxException.class);

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);

    RMAC.config = config;
    Service.GSON = mockGson;
    String[] result = service.getCommands();

    assertArrayEquals(new String[]{}, result);

    connectivity.close();
    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Get commands succeeds")
  public void getCommands_Success() throws IOException {
    Service service = new Service();
    Config config = mock(Config.class);
    MockedStatic<Connectivity> connectivity = mockStatic(Connectivity.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("['testcmd1', 'testcmd2']");
    when(httpClient.execute(any())).thenReturn(mockResponse);

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    connectivity.when(Connectivity::checkNetworkState).thenReturn(true);

    RMAC.config = config;
    String[] result = service.getCommands();

    assertArrayEquals(new String[]{"testcmd1", "testcmd2"}, result);

    connectivity.close();
    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Get register client when network is down")
  public void registerClient_NetworkDown() {
    Service service = new Service();
    RMAC.isClientRegistered = false;
    RMAC.NETWORK_STATE = false;

    service.registerClient();

    assertFalse(RMAC.isClientRegistered);
  }

  @Test
  @DisplayName("Get register client when already registered")
  public void registerClient_Already_Registered() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    Service service = new Service();
    Config config = mock(Config.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    HttpEntity mockEntity = mock(HttpEntity.class);
    Scanner mockScanner = mock(Scanner.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("['testcmd1', 'testcmd2']");
    when(httpClient.execute(any())).thenReturn(mockResponse);
    when(mockResponse.getEntity()).thenReturn(mockEntity);
    when(mockFs.scanner(any())).thenReturn(mockScanner);

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    doReturn("testclientname").when(config).getClientName();
    doReturn("testhostname").when(config).getHostName();
    doReturn(true, false).when(mockScanner).hasNext();
    doReturn("testid").when(mockScanner).nextLine();

    RMAC.config = config;
    RMAC.fs = mockFs;
    RMAC.NETWORK_STATE = true;
    service.registerClient();

    assertTrue(RMAC.isClientRegistered);
    verify(config, times(0)).setProperty(anyString(), anyString());

    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Get register client when not registered")
  public void registerClient_NotRegistered() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    Service service = new Service();
    Config config = mock(Config.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    HttpEntity mockEntity = mock(HttpEntity.class);
    Scanner mockScanner = mock(Scanner.class);
    BridgeClient mockBridge = mock(BridgeClient.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("['testcmd1', 'testcmd2']");
    when(httpClient.execute(any())).thenReturn(mockResponse);
    when(mockResponse.getEntity()).thenReturn(mockEntity);
    when(mockFs.scanner(any())).thenReturn(mockScanner);

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    doReturn("testclientname").when(config).getClientName();
    doReturn("testhostname").when(config).getHostName();
    doReturn(true, false).when(mockScanner).hasNext();
    doReturn("testid1").when(mockScanner).nextLine();

    RMAC.config = config;
    RMAC.fs = mockFs;
    RMAC.NETWORK_STATE = true;
    RMAC.bridgeClient = mockBridge;
    service.registerClient();

    assertTrue(RMAC.isClientRegistered);
    verify(config).setProperty(eq("Id"), eq("testid1"));
    verify(mockBridge).sendMessage(any(Message.class));

    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Get register client fails")
  public void registerClient_Failed() throws IOException {
    FileSystem mockFs = mock(FileSystem.class);
    Service service = new Service();
    Config config = mock(Config.class);
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("['testcmd1', 'testcmd2']");
    doThrow(IOException.class).when(httpClient).execute(any());

    doReturn("testurl").when(config).getApiServerUrl();
    doReturn("testid").when(config).getId();
    doReturn("testclientname").when(config).getClientName();
    doReturn("testhostname").when(config).getHostName();

    RMAC.config = config;
    RMAC.fs = mockFs;
    RMAC.NETWORK_STATE = true;
    RMAC.isClientRegistered = false;
    service.registerClient();

    assertFalse(RMAC.isClientRegistered);

    mockedClient.close();
    mockedUtils.close();
  }
}
