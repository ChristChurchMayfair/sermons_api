package org.christchurchmayfair.api.sermons.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Series {
  private final String id;
  private final String name;
  private final String subtitle;
  private final String image3x2Url;

  @JsonCreator
  public Series(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("subtitle") String subtitle,
      @JsonProperty("image3x2Url") String image3x2Url)
  {
    this.id = id;
    this.name = name;
    this.subtitle = subtitle;
    this.image3x2Url = image3x2Url;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getImage3x2Url() {
    return image3x2Url;
  }
}
