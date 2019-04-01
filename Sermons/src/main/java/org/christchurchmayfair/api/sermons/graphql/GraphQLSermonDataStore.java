package org.christchurchmayfair.api.sermons.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.christchurchmayfair.api.sermons.model.Event;
import org.christchurchmayfair.api.sermons.model.Series;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.model.Speaker;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GraphQLSermonDataStore implements SermonDataStore {

  private BasicGraphQLClient client;
  private ObjectMapper objectMapper;

  public GraphQLSermonDataStore(String graphqlUrl, String token, ObjectMapper objectmapper) throws URISyntaxException {
    this.objectMapper = objectmapper;
    client = new BasicGraphQLClient(graphqlUrl, token);
  }

  @Override
  public List<Sermon> getSermons() throws IOException {

    Map<String, Object> variables = new HashMap<>();
    variables.put("number", 40);

    GraphQLQuery query = new GraphQLQuery(SermonsGraphqlQueries.LAST_N_SERMONS, variables);
    final String response;
    try {
      response = client.request(query);

      final JsonNode responseRoot = objectMapper.readTree(response);

      if (responseRoot.has("data") && responseRoot.get("data").has("allSermons")) {

        final JsonNode sermonsRoot = responseRoot.get("data").get("allSermons");

        final ObjectReader objectReader = objectMapper.readerFor(new TypeReference<List<Sermon>>() {
        });

        final List<Sermon> sermons = objectReader.readValue(sermonsRoot);

        return sermons;

      }


    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }

    return new ArrayList<>();
  }

  @Override
  public List<Series> getSerieses() throws IOException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("number", 50);

    GraphQLQuery query = new GraphQLQuery(SermonsGraphqlQueries.ALL_SERIES, variables);
    final String response;
    try {
      response = client.request(query);

      final JsonNode responseRoot = objectMapper.readTree(response);

      if (responseRoot.has("data") && responseRoot.get("data").has("allSeries")) {

        final JsonNode sermonsRoot = responseRoot.get("data").get("allSeries");

        final ObjectReader objectReader = objectMapper.readerFor(new TypeReference<List<Series>>() {
        });

        final List<Series> seriesList = objectReader.readValue(sermonsRoot);

        return seriesList;

      }


    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
    return new ArrayList<>();
  }

  @Override
  public List<Speaker> getSpeakers() throws IOException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("number", 50);

    GraphQLQuery query = new GraphQLQuery(SermonsGraphqlQueries.ALL_SPEAKERS, variables);
    final String response;
    try {
      response = client.request(query);

      final JsonNode responseRoot = objectMapper.readTree(response);

      if (responseRoot.has("data") && responseRoot.get("data").has("allSpeakers")) {

        final JsonNode sermonsRoot = responseRoot.get("data").get("allSpeakers");

        final ObjectReader objectReader = objectMapper.readerFor(new TypeReference<List<Series>>() {
        });

        final List<Speaker> speakerList = objectReader.readValue(sermonsRoot);

        return speakerList;

      }


    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
    return new ArrayList<>();
  }

  @Override
  public List<Event> getEvents() throws IOException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("number", 50);

    GraphQLQuery query = new GraphQLQuery(SermonsGraphqlQueries.ALL_EVENTS, variables);
    final String response;
    try {
      response = client.request(query);

      final JsonNode responseRoot = objectMapper.readTree(response);

      if (responseRoot.has("data") && responseRoot.get("data").has("allEvents")) {

        final JsonNode sermonsRoot = responseRoot.get("data").get("allEvents");

        final ObjectReader objectReader = objectMapper.readerFor(new TypeReference<List<Series>>() {
        });

        final List<Event> speakerList = objectReader.readValue(sermonsRoot);

        return speakerList;

      }


    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
    return new ArrayList<>();
  }

  @Override
  public Optional<String> createSermon(Sermon newSermon) throws Exception {
    Map<String, Object> variables = new HashMap<>();
    variables.put("name", newSermon.getName());
    variables.put("url", newSermon.getUrl().get());
    variables.put("duration", 10);
    variables.put("preachedAt", newSermon.getPreachedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    variables.put("passage", newSermon.getPassage());
    variables.put("series_id", newSermon.getSeries().getId());
    variables.put("event_id", newSermon.getEvent().getId());
    variables.put("speaker_ids", newSermon.getSpeakers().stream().map(Speaker::getId).collect(Collectors.toList()));
    //$name: String!,\n" +
    //          "  $url:String!,\n" +
    //          "  $duration:Int!,\n" +
    //          "  $preachedAt:DateTime!,\n" +
    //          "  $passage:String!\n" +
    //          "  $series_id:ID!,\n" +
    //          "  $speaker_ids:[ID!],\n" +
    //          "  $event_id:ID!\n" +


    GraphQLQuery query = new GraphQLQuery(SermonsGraphqlQueries.CREATE_SERMON, variables);
    final String response;
    try {
      response = client.request(query);

      System.out.println(response);

      final JsonNode responseRoot = objectMapper.readTree(response);

      if (responseRoot.has("data") &&
          responseRoot.get("data").has("Sermon") &&
          responseRoot.get("data").get("Sermon").has("id")
      ) {

        final String newSermonId = responseRoot.get("data").get("Sermon").get("id").asText();

        return Optional.of(newSermonId);

      }


    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new Exception("Unable to create sermon - " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      throw new Exception("Unable to create sermon - " + e.getMessage());
    }
    return Optional.empty();
  }
}
