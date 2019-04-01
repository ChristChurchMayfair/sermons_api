package org.christchurchmayfair.api.sermons.graphql;

import java.util.Map;

public class GraphQLQuery {

  private String query;
  private Map<String, Object> variables;

  public GraphQLQuery(String query, Map<String, Object> variables) {
    this.query = query;
    this.variables = variables;
  }

  public String getQuery() {
    return query;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
