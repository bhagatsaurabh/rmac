package com.rmac.core;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.rmac.RMAC;
import java.io.IOException;
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
}
