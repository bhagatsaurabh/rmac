package com.rmac.core;

import com.google.gson.Gson;
import com.rmac.RMAC;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Service to call RMAC Server REST APIs
 */
@Slf4j
public class Service {

  public static Gson GSON = new Gson();

  /**
   * Call GET /commands API to fetch commands requested to be executed on this host machine.
   *
   * @return List of commands.
   */
  public String[] getCommands() {
    Connectivity.checkNetworkState();

    if (!RMAC.NETWORK_STATE) {
      log.warn("Network down, not fetching commands");
      return new String[]{};
    }

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URIBuilder builder = new URIBuilder(RMAC.config.getServerUrl() + "/command");
      builder.setParameter("id", RMAC.config.getClientId());
      HttpGet httpget = new HttpGet(builder.build());
      HttpResponse response = httpclient.execute(httpget);
      String jsonString = EntityUtils.toString(response.getEntity());
      return GSON.fromJson(jsonString, String[].class);
    } catch (IOException | URISyntaxException e) {
      log.error("Could not fetch commands", e);
    } catch (ParseException e) {
      log.error("Could not parse json response from /commands", e);
    }
    return new String[]{};
  }

  /**
   * Call GET /register API to register this RMAC client.
   */
  public void registerClient() {
    Connectivity.checkNetworkState();

    if (!RMAC.NETWORK_STATE) {
      log.warn("Network down, client registration skipped");
      return;
    }

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      URIBuilder builder = new URIBuilder(RMAC.config.getServerUrl() + "/register");
      builder
          .setParameter("clientName", RMAC.config.getClientName())
          .setParameter("hostName", RMAC.config.getHostName())
          .setParameter("id", RMAC.config.getClientId());
      HttpGet httpget = new HttpGet(builder.build());
      HttpResponse response = httpclient.execute(httpget);
      Scanner sc = new Scanner(response.getEntity().getContent());
      while (sc.hasNext()) {
        String id = sc.nextLine();
        if (!id.equals(RMAC.config.getClientId())) {
          RMAC.config.setClientId(id);
        }
      }
      RMAC.isClientRegistered = true;
    } catch (URISyntaxException | IOException | IllegalStateException e) {
      log.error("Client registration failed", e);
    }
  }
}
