package org.christchurchmayfair.api.sermons.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class BasicGraphQLClient {

  private URI uri;
  private String token;

  public BasicGraphQLClient(String uri, String token) throws URISyntaxException {
    this.uri = new URI(uri);
    this.token = token;
  }

  public String request(GraphQLQuery query) throws JsonProcessingException {
    HttpClient httpClient = HttpClientBuilder.create().build();


    HttpPost httpRequest = new HttpPost(uri);

    httpRequest.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());
    httpRequest.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());

    if (token != null) {
      httpRequest.setHeader("Authorization", "Bearer " + token);
    }

    ObjectMapper objectMapper = new ObjectMapper();

    final String queryAsString = objectMapper.writeValueAsString(query);

    HttpEntity body = EntityBuilder.create().setText(queryAsString).setContentType(ContentType.APPLICATION_JSON).build();

    httpRequest.setEntity(body);

    final HttpResponse response;
    try {
      response = httpClient.execute(httpRequest);
    } catch (IOException e) {
      e.printStackTrace();
      return "";
    }

    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
      try {
        return EntityUtils.toString(response.getEntity(), "UTF8");
      } catch (IOException e) {
        e.printStackTrace();
        return "";
      }
    }
    return "";

  }


}
