package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

/**
 * Handler for requests to Lambda function.
 */
public class CreateSermon extends SermonDataFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  public CreateSermon() {
    super();
  }

  public CreateSermon(SermonDataStore sermons) {
    super(sermons);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

    if (!SermonDataFunction.isAuthorized(input)) {
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
        response.setBody(sermonId.get());
        responseHeaders.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        response.setStatusCode(200);
        response.setHeaders(responseHeaders);
        return response;
      } else {
        responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
        response.setBody("Graphcool did not respond with an ID");
        response.setStatusCode(500);
        response.setHeaders(responseHeaders);
        return response;
      }

    } catch (IOException e) {
      e.printStackTrace();
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setBody("Problem listing sermons - " + e.getMessage());
      response.setStatusCode(500);
      response.setHeaders(responseHeaders);
      return response;
    }
  }
}
