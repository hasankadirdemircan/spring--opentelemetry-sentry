package com.example.microservice_b.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/b")
public class BController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BController.class);

    @GetMapping
    public String throwErrorFromBMicroservice() {
        LOGGER.info("B microservice api called throwErrorFromBMicroservice");
        LOGGER.error("B microservice thrown an error in throwErrorFromBMicroservice ");
        throw new RuntimeException("error in throwErrorFromBMicroservice");
    }


    @GetMapping("/ok")
    public String noErrorFromBMicroservice() {
        LOGGER.info("B microservice api called throwErrorFromBMicroservice");
        return "ok";
    }
}
