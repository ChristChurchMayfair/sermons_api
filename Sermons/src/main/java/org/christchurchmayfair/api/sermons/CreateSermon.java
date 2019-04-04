package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.model.GithubUser;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handler for requests to Lambda function.
 */
public class CreateSermon extends SermonDataFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private WebTarget githubAPIRoot = ClientBuilder.newClient().target("https://api.github.com");

  public CreateSermon() {
    super();
  }

  public CreateSermon(SermonDataStore sermons) {
    super(sermons);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

    if (!input.getHeaders().containsKey("Authorization")) {
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setBody("Not authorized");
      response.setStatusCode(403);
      return response;
    }

    final String authorizationHeader = input.getHeaders().get("Authorization");
    final String[] parts = authorizationHeader.split(" ");
    final String type = parts[0];
    final String token = parts[1];

    if (!"github".equals(type)) {
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setBody("Not authorized");
      response.setStatusCode(403);
      return response;
    }

    final WebTarget currentUserTarget = githubAPIRoot.path("user");

    final GithubUser currentGithubUser = currentUserTarget.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization", "token " + token).get(GithubUser.class);

    final WebTarget memberOrgCheckTarget = githubAPIRoot.path("/orgs/{organisation}/members/{username}")
        .resolveTemplate("organisation","ChristChurchMayfair")
        .resolveTemplate("username",currentGithubUser.getLogin());

    final Response isOrgMemberResponse = memberOrgCheckTarget.request(MediaType.APPLICATION_JSON_TYPE).get();

    if (isOrgMemberResponse.getStatus() != 204) {
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setBody("Not authorized");
      response.setStatusCode(403);
      return response;
    }

    try {

      Sermon newSermon;
      if (input.getIsBase64Encoded()) {
        final byte[] bytes = Base64.getDecoder().decode(input.getBody());
        newSermon = objectMapper.readValue(bytes, Sermon.class);
      } else {
        newSermon = objectMapper.readValue(input.getBody(), Sermon.class);
      }

      Optional<String> sermonId = Optional.empty();
      try {
        sermonId = sermons.createSermon(newSermon);
      } catch (Exception e) {
        e.printStackTrace();
      }

      if (sermonId.isPresent()) {
        this.response.setBody(sermonId.get());
        responseHeaders.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        this.response.setStatusCode(200);
        this.response.setHeaders(responseHeaders);
        return this.response;
      } else {
        responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
        this.response.setBody("Graphcool did not respond with an ID");
        this.response.setStatusCode(500);
        this.response.setHeaders(responseHeaders);
        return this.response;
      }

    } catch (IOException e) {
      e.printStackTrace();
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      this.response.setBody("Problem listing sermons - " + e.getMessage());
      this.response.setStatusCode(500);
      this.response.setHeaders(responseHeaders);
      return this.response;
    }
  }
}
