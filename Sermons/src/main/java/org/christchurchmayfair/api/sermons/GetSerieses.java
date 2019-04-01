package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.model.Series;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.util.List;

/**
 * Handler for requests to Lambda function.
 */
public class GetSerieses extends SermonDataFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  public GetSerieses() {
    super();
  }

  public GetSerieses(SermonDataStore sermons) {
    super(sermons);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
    try {
      final List<Series> sermonList = sermons.getSerieses();
      final String sermonListAsString = objectMapper.writeValueAsString(sermonList);

      response.setBody(sermonListAsString);
      responseHeaders.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
      response.setStatusCode(200);
      response.setHeaders(responseHeaders);
      return response;

    } catch (IOException e) {
      e.printStackTrace();
      response.setBody("Problem listing series");
      responseHeaders.put("Content-Type", ContentType.TEXT_PLAIN.getMimeType());
      response.setStatusCode(500);
      response.setHeaders(responseHeaders);
      return response;
    }
  }
}
