package org.christchurchmayfair.api.sermons.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GithubAccessTokenResponse {
  private final String accessToken;
  private final String scope;
  private final String tokenType;

  @JsonCreator
  public GithubAccessTokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("scope") String scope,
      @JsonProperty("token_type") String tokenType) {
    this.accessToken = accessToken;
    this.scope = scope;
    this.tokenType = tokenType;
  }

  @JsonProperty("access_token")
  public String getAccessToken() {
    return accessToken;
  }


  public String getScope() {
    return scope;
  }

  @JsonProperty("token_type")
  public String getTokenType() {
    return tokenType;
  }

  @Override
  public String toString() {
    return "GithubAccessTokenResponse{" +
        "accessToken='" + accessToken + '\'' +
        ", scope='" + scope + '\'' +
        ", tokenType='" + tokenType + '\'' +
        '}';
  }
}
