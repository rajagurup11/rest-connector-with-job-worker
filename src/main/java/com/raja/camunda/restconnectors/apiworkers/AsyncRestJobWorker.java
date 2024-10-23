package com.raja.camunda.restconnectors.apiworkers;

import static com.raja.camunda.restconnectors.enums.JobResultKey.RESPONSE_BODY;
import static com.raja.camunda.restconnectors.enums.JobResultKey.STATUS_CODE;
import static com.raja.camunda.restconnectors.enums.JobVariableKey.*;
import static com.raja.camunda.restconnectors.utils.HttpUtils.*;

import com.raja.camunda.restconnectors.dto.HttpRequest;
import com.raja.camunda.restconnectors.enums.HttpMethodEnum;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AsyncRestJobWorker {

  private final WebClient.Builder webClientBuilder;

  public AsyncRestJobWorker(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  @JobWorker(type = "rest-invocation-async")
  public void handleRestCall(final JobClient client, final ActivatedJob job) {
    Map<String, Object> variables = job.getVariablesAsMap();

    var url = getVariable(variables, URL, String.class);
    var method = getVariable(variables, METHOD, String.class);
    var headersMap = getVariable(variables, HEADERS, Map.class);
    var body = getVariable(variables, BODY, String.class);

    // Validate input parameters
    validateRequestParameters(url, HttpMethodEnum.valueOf(method.toUpperCase()));

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

    // Make the HTTP request asynchronously
    Mono<ClientResponse> responseMono =
        makeHttpRequest(
            webClient,
            httpRequest.method(),
            httpRequest.url(),
            httpEntity.getHeaders().toSingleValueMap(),
            httpEntity.getBody());

    // Handle response asynchronously
    responseMono
        .flatMap(
            response ->
                response
                    .bodyToMono(String.class)
                    .map(
                        responseBody ->
                            Map.of(
                                STATUS_CODE.getKey(),
                                response.statusCode().value(),
                                RESPONSE_BODY.getKey(),
                                responseBody)))
        .flatMap(
            result ->
                Mono.create(
                    sink ->
                        client
                            .newCompleteCommand(job.getKey())
                            .variables(result)
                            .send()
                            .whenComplete(
                                (completeJobResponse, throwable) -> {
                                  if (throwable != null) {
                                    sink.error(throwable); // Handle error
                                  } else {
                                    sink.success(completeJobResponse); // Complete Mono successfully
                                  }
                                })))
        .subscribe();
  }

  private Mono<ClientResponse> makeHttpRequest(
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
            .exchange();
      case POST:
        return webClient
            .post()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .bodyValue(body)
            .exchange();
      case PUT:
        return webClient
            .put()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .bodyValue(body)
            .exchange();
      case DELETE:
        return webClient
            .delete()
            .uri(url)
            .headers(headers -> headersMap.forEach(headers::set))
            .exchange();
      default:
        return Mono.error(new IllegalArgumentException("Unsupported HTTP method: " + method));
    }
  }
}
