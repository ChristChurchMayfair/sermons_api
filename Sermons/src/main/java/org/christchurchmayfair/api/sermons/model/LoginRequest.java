package org.christchurchmayfair.api.sermons.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginRequest {
  private final String code;
  private final String state;

  @JsonCreator
  public LoginRequest(
      @JsonProperty("code") String code,
      @JsonProperty("state") String state) {
    this.code = code;
    this.state = state;
  }

  public String getCode() {
    return code;
  }

  public String getState() {
    return state;
  }

  @Override
  public String toString() {
    return "LoginRequest{" +
        "code='" + code + '\'' +
        ", state='" + state + '\'' +
        '}';
  }
}
