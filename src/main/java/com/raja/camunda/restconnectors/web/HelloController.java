package com.raja.camunda.restconnectors.web;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class HelloController {

  @GetMapping
  public String get() {
    return "Hello World!";
  }

  @PostMapping
  public void post() {
    System.out.println("Post method called");
  }

  @PutMapping
  public void put() {
    System.out.println("Put method called");
  }

  @DeleteMapping
  public void delete() {
    System.out.println("Delete method called");
  }
}
