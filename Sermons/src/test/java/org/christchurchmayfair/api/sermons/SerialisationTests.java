package org.christchurchmayfair.api.sermons;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SerialisationTests {
  private static ObjectMapper objectMapper = new ObjectMapper();

  @BeforeClass
  public static void setup() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
  }

  @Test
  public void testCanDeserilisePartialSermon() throws IOException {

    // Given
    final String partialSermonAsString =
        "{" +
            "\"name\" : \"name\"," +
            "\"preachedAt\" : \"2019-01-01T12:00:00.000Z\"," +
            "\"passage\" : \"Genesis 1:1-2\"," +
            "\"url\" : \"a url\"," +
            "\"speakers\" : [ { \"name\":\"Speaker Name\"}]," +
            "\"series\" : { \"name\":\"Series Name\"}," +
            "\"event\" : { \"name\":\"Event Name\"}" +
        "}";


    // When
    final Sermon sermon = objectMapper.readValue(partialSermonAsString, Sermon.class);


    // Then
    assertThat(sermon.getName(), is("name"));
  }
}
