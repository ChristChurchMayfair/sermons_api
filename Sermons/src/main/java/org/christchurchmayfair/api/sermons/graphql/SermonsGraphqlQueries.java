package org.christchurchmayfair.api.sermons.graphql;

public class SermonsGraphqlQueries {

  public static final String LAST_N_SERMONS =
      "query($number:Int!) {\n" +
      "        allSermons(last: $number, orderBy:preachedAt_ASC) {\n" +
      "            name,\n" +
      "            id,\n" +
      "            url,\n" +
      "            passage,\n" +
      "            series { id, name },\n" +
      "            speakers { id, name },\n" +
      "            event { id, name },\n" +
      "            duration,\n" +
      "            preachedAt\n" +
      "        }\n" +
      "    }";

  public static final String ALL_SERIES =
      "query($number:Int!) {\n" +
          "        allSeries(last: $number) {\n" +
          "            name,\n" +
          "            id,\n" +
          "            subtitle,\n" +
          "            image3x2Url\n" +
          "        }\n" +
          "    }";
  public static final String ALL_SPEAKERS =
      "query($number:Int!) {\n" +
      "        allSpeakers(last: $number) {\n" +
      "            name,\n" +
      "            id,\n" +
      "        }\n" +
      "    }";
  public static final String ALL_EVENTS =
      "query($number:Int!) {\n" +
      "        allEvents(last: $number) {\n" +
      "            name,\n" +
      "            id,\n" +
      "        }\n" +
      "    }";

  public static final String CREATE_SERMON =
      "    mutation(\n" +
          "  $name: String!,\n" +
          "  $url:String!,\n" +
          "  $duration:Int!,\n" +
          "  $preachedAt:DateTime!,\n" +
          "  $passage:String!\n" +
          "  $series_id:ID!,\n" +
          "  $speaker_ids:[ID!],\n" +
          "  $event_id:ID!\n" +
          ") {\n" +
          "    createSermon (\n" +
          "      name: $name,\n" +
          "      url: $url,\n" +
          "      duration: $duration,\n" +
          "      preachedAt: $preachedAt,\n" +
          "      passage: $passage,\n" +
          "      seriesId: $series_id,\n" +
          "      speakersIds: $speaker_ids,\n" +
          "      eventId: $event_id\n" +
          "    ) {\n" +
          "      id\n" +
          "    }\n" +
          "}";
}
