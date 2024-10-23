package com.raja.camunda.restconnectors.utils;

import com.raja.camunda.restconnectors.enums.HttpMethodEnum;
import com.raja.camunda.restconnectors.enums.JobVariableKey;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
  public static HttpEntity<String> createHttpEntity(Map<String, String> headersMap, String body) {
    HttpHeaders headers = new HttpHeaders();
    Optional.ofNullable(headersMap).ifPresent(map -> map.forEach(headers::set));
    return new HttpEntity<>(body, headers);
  }

  public static <T> T getVariable(
      Map<String, Object> variables, JobVariableKey key, Class<T> type) {
    return type.cast(variables.get(key.getKey()));
  }

  public static void validateRequestParameters(String url, HttpMethodEnum method) {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("URL must not be null or empty");
    }
  }

  public static String getOrRefreshToken() {
    // Custom logic for retrieving or refreshing a security token
    return "some-auth-token";
  }
}
