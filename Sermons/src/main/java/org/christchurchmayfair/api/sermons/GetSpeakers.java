package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.model.Speaker;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.util.List;

/**
 * Handler for requests to Lambda function.
 */
public class GetSpeakers extends SermonDataFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  public GetSpeakers() {
    super();
  }

  public GetSpeakers(SermonDataStore sermons) {
    super(sermons);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    try {
      final List<Speaker> speakerList = sermons.getSpeakers();
      final String sermonListAsString = objectMapper.writeValueAsString(speakerList);

      response.setBody(sermonListAsString);
      responseHeaders.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
      response.setStatusCode(200);
      response.setHeaders(responseHeaders);
      return response;
    } catch (IOException e) {
      e.printStackTrace();
      response.setBody("Problem listing sermons");
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setStatusCode(500);
      response.setHeaders(responseHeaders);
      return response;
    }
  }
}
