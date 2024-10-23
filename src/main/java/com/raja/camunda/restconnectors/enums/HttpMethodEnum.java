package com.raja.camunda.restconnectors.enums;

public enum HttpMethodEnum {
  GET("GET"),
  POST("POST"),
  PUT("PUT"),
  DELETE("DELETE");

  private final String method;

  HttpMethodEnum(String method) {
    this.method = method;
  }

  public static boolean isValid(String method) {
    for (HttpMethodEnum httpMethod : values()) {
      if (httpMethod.method.equalsIgnoreCase(method)) {
        return true;
      }
    }
    return false;
  }

  public String getMethod() {
    return method;
  }
}
