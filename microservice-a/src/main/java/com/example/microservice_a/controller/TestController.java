package com.example.microservice_a.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/test")
public class TestController {


    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/throw-error")
    public String throwError() {
        LOGGER.info("throwError controller");
        LOGGER.error("thrown error");
        throw new RuntimeException("throwError controller");
    }

    @GetMapping("/no-error")
    public String noError() {
        LOGGER.info("noError");

        return "hola";
    }

    @GetMapping("/call-microservice-b")
    public String throwErrorCallMicroServiceB() {
        String response = null;
        try {
             return response = restTemplate.getForObject("http://b-microservice-app:8096/b", String.class);
        }catch (Exception e) {
            return "error catched";
        }
    }
}
