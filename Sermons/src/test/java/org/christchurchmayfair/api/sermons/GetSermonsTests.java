package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.christchurchmayfair.api.sermons.graphql.GraphQLSermonDataStore;
import org.christchurchmayfair.api.sermons.model.Event;
import org.christchurchmayfair.api.sermons.model.Series;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.model.Speaker;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public class GetSermonsTests {

  @Test
  public void successfulResponse() throws URISyntaxException, IOException {

    // Given
    SermonDataStore mockSermonDataStore = Mockito.mock(GraphQLSermonDataStore.class);
    GetSermons getSermons = new GetSermons(mockSermonDataStore);

    when(mockSermonDataStore.getSermons()).thenReturn(asList(new Sermon("id", "name", ZonedDateTime.now(), Optional.of("url"),123123, "passage", new Series("series_id", "series name", "subtitle", "imageurl"),new Event("event id","event name"), asList(new Speaker("speaker id","speaker name")))));

    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();

    // When
    final APIGatewayProxyResponseEvent responseEvent = getSermons.handleRequest(input, null);

    // Then
    assertThat(responseEvent.getStatusCode(), is(200));
    assertThat(responseEvent.getHeaders().keySet(), hasItem(is("Content-Type")));
    assertThat(responseEvent.getHeaders().get("Content-Type"), is("application/json"));
  }
}
