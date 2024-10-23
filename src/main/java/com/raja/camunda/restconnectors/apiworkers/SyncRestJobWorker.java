package com.raja.camunda.restconnectors.apiworkers;

import static com.raja.camunda.restconnectors.enums.JobResultKey.RESPONSE_BODY;
import static com.raja.camunda.restconnectors.enums.JobResultKey.STATUS_CODE;
import static com.raja.camunda.restconnectors.utils.HttpUtils.*;

import com.raja.camunda.restconnectors.dto.HttpRequest;
import com.raja.camunda.restconnectors.dto.HttpResponse;
import com.raja.camunda.restconnectors.enums.HttpMethodEnum;
import com.raja.camunda.restconnectors.enums.JobVariableKey;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SyncRestJobWorker {

  private final WebClient.Builder webClientBuilder;

  public SyncRestJobWorker(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  @JobWorker(type = "io.camunda:http-json:1")
  public void handleRestCall(final JobClient client, final ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();

    var url = getVariable(variables, JobVariableKey.URL, String.class);
    var method = getVariable(variables, JobVariableKey.METHOD, String.class);
    var headersMap = getVariable(variables, JobVariableKey.HEADERS, Map.class);
    var body = getVariable(variables, JobVariableKey.BODY, String.class);

    // Validate input parameters
    //validateRequestParameters(url, HttpMethodEnum.valueOf(method.toUpperCase()));

    // Implement custom security logic
    String authToken = getOrRefreshToken();

    // Build WebClient with security
    WebClient webClient =
        webClientBuilder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + authToken).build();

    // Create HttpRequest record
    HttpRequest httpRequest =
        new HttpRequest(url, HttpMethodEnum.valueOf(method.toUpperCase()), headersMap, body);

    // Create HttpEntity
    HttpEntity<String> httpEntity = createHttpEntity(httpRequest.headers(), httpRequest.body());

    // Make the HTTP request
    ClientResponse response =
        makeHttpRequest(
            webClient,
            httpRequest.method(),
            httpRequest.url(),
            httpEntity.getHeaders().toSingleValueMap(),
            httpEntity.getBody());

    // Handle response
    HttpResponse httpResponse =
        new HttpResponse(response.statusCode().value(), response.bodyToMono(String.class).block());

    // Complete the job with the response
    Map<String, Object> result =
        Map.of(
            STATUS_CODE.getKey(), httpResponse.statusCode(),
            RESPONSE_BODY.getKey(), httpResponse.responseBody());

    client.newCompleteCommand(job.getKey()).variables(result).send().join();
  }

  private ClientResponse makeHttpRequest(
      WebClient webClient,
      HttpMethodEnum method,
      String url,
      Map<String, String> headersMap,
      String body) {

    switch (method) {
      case GET:
        return webClient
            .get()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .exchange()
            .block();
      case POST:
        return webClient
            .post()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .bodyValue(body)
            .exchange()
            .block();
      case PUT:
        return webClient
            .put()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .bodyValue(body)
            .exchange()
            .block();
      case DELETE:
        return webClient
            .delete()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .exchange()
            .block();
      default:
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }
  }
}
