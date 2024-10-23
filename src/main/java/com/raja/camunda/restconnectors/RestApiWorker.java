/*
package com.raja.camunda.restconnectors;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestApiWorker {

  private final RestTemplate restTemplate = new RestTemplate();

  @JobWorker(type = "io.camunda:http-json:1")
  public void handleRestApiCall(final JobClient client, final ActivatedJob job) {
    var url = job.getVariablesAsMap().get("url");
    var method = job.getVariablesAsMap().get("method");

    // Example REST API Call
    var result = restTemplate.getForObject("http://localhost:8901/hello", String.class);

    // Log the response or process it
    System.out.println("API response: " + result);

    // Complete the job
    client.newCompleteCommand(job.getKey()).send().join();
  }
}
*/
