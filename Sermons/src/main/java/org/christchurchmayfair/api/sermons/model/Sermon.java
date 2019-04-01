package org.christchurchmayfair.api.sermons.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public class Sermon {
  private final String id;
  private final String name;
  private final ZonedDateTime preachedAt;
  private final Optional<String> url;
  private final Integer duration;
  private final String passage;
  private final Series series;
  private final Event event;

  private final List<Speaker> speakers;

  @JsonCreator
  public Sermon(
      @JsonProperty("id") String id,
      @JsonProperty(value = "name", required = true) String name,
      @JsonProperty(value = "preachedAt", required = true) ZonedDateTime preachedAt,
      @JsonProperty("url") Optional<String> url,
      @JsonProperty("duration") Integer duration,
      @JsonProperty(value = "passage", required = true) String passage,
      @JsonProperty(value = "series", required = true) Series series,
      @JsonProperty(value = "event", required = true) Event event,
      @JsonProperty(value = "speakers", required = true) List<Speaker> speakers) {
    this.id = id;
    this.name = name;
    this.preachedAt = preachedAt;
    this.url = url;
    this.duration = duration;
    this.passage = passage;
    this.series = series;
    this.event = event;
    this.speakers = speakers;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ZonedDateTime getPreachedAt() {
    return preachedAt;
  }

  public Optional<String> getUrl() {
    return url;
  }

  public Integer getDuration() {
    return duration;
  }

  public String getPassage() {
    return passage;
  }

  public Series getSeries() {
    return series;
  }

  public Event getEvent() {
    return event;
  }

  public List<Speaker> getSpeakers() {
    return speakers;
  }

  //  id: ID! @isUnique
//  name: String!
//  preachedAt: DateTime!
//  url: String! @isUnique
//  duration: Int!
//  series: Series @relation(name: "SermonOnSeries")
//  speakers: [Speaker!]! @relation(name: "SermonOnSpeaker")
//  event: Event @relation(name: "SermonOnEvent")
//  passage: String
}
