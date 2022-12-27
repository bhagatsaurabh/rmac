package com.rmac.updater;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Service to call RMAC Server's Rest APIs.
 */
@Slf4j
public class Service {

  public static Gson GSON = new Gson();

  /**
   * Check if a new RMAC client update is available.
   *
   * @param version Version of currently active RMAC client application on this host machine.
   * @return Temporary download url for the new RMAC client jar and its SHA checksum, empty String
   * array if no update is available.
   */
  public static String[] getUpdate(String version) {
    HttpResponse response;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URIBuilder builder = new URIBuilder(Updater.SERVER_URL + "/update");
      builder.setParameter("version", version);
      HttpGet httpget = new HttpGet(builder.build());
      response = httpclient.execute(httpget);
    } catch (IOException | URISyntaxException e) {
      log.error("Could not fetch updates", e);
      return new String[]{};
    }

    String jsonString;
    try {
      jsonString = EntityUtils.toString(response.getEntity());
    } catch (ParseException | IOException e) {
      log.error("Could not get data from response", e);
      return new String[]{};
    }

    try {
      return GSON.fromJson(jsonString, String[].class);
    } catch (JsonSyntaxException e) {
      log.error("Could not parse json response", e);
      return new String[]{};
    }
  }
}
