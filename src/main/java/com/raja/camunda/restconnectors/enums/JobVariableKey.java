package com.raja.camunda.restconnectors.enums;

public enum JobVariableKey {
  URL("url"),
  METHOD("method"),
  HEADERS("headers"),
  BODY("body");

  private final String key;

  JobVariableKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
