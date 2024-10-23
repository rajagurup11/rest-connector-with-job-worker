package com.raja.camunda.restconnectors.enums;

public enum JobResultKey {
  STATUS_CODE("statusCode"),
  RESPONSE_BODY("responseBody");

  private final String key;

  JobResultKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
