package com.raja.camunda.restconnectors.dto;

import com.raja.camunda.restconnectors.enums.HttpMethodEnum;
import java.util.Map;

public record HttpRequest(
    String url, HttpMethodEnum method, Map<String, String> headers, String body) {}
