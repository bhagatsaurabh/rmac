package com.rmac.updater;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

public class ServiceTest {

  @Test
  @DisplayName("Call to update service fails")
  public void getUpdate_Failed() throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    when(httpClient.execute(any())).thenThrow(IOException.class);

    String[] result = new Service().getUpdate("1.0.0");

    Assert.assertArrayEquals(new String[]{}, result);

    mockedClient.close();
  }

  @Test
  @DisplayName("Call to update service fails while parsing string response")
  public void getUpdate_Parse_Failed() throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenThrow(ParseException.class);
    when(httpClient.execute(any())).thenReturn(mockResponse);

    String[] result = new Service().getUpdate("1.0.0");

    Assert.assertArrayEquals(new String[]{}, result);

    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Call to update service fails while parsing json response")
  public void getUpdate_ParseJSON_Failed() throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
    Gson mockGson = mock(Gson.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("");
    when(httpClient.execute(any())).thenReturn(mockResponse);
    when(mockGson.fromJson(anyString(), any())).thenThrow(JsonSyntaxException.class);

    Service service = new Service();
    Service.GSON = mockGson;
    String[] result = service.getUpdate("1.0.0");

    Assert.assertArrayEquals(new String[]{}, result);

    mockedClient.close();
    mockedUtils.close();
  }

  @Test
  @DisplayName("Call to update service succeeds")
  public void getUpdate_Success() throws IOException {
    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    MockedStatic<HttpClients> mockedClient = mockStatic(HttpClients.class);
    MockedStatic<EntityUtils> mockedUtils = mockStatic(EntityUtils.class);
    CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);

    mockedClient.when(HttpClients::createDefault).thenReturn(httpClient);
    mockedUtils.when(() -> EntityUtils.toString(any())).thenReturn("['testurl', 'testchecksum']");
    when(httpClient.execute(any())).thenReturn(mockResponse);

    Service service = new Service();
    String[] result = service.getUpdate("1.0.0");

    Assert.assertArrayEquals(new String[]{"testurl", "testchecksum"}, result);

    mockedClient.close();
    mockedUtils.close();
  }
}
