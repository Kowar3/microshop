package com.example.discoveryservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryServiceApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(DiscoveryServiceApplication.class, args);
        log.info("✅ Eureka Discovery Server started at http://localhost:8761");
    }

}
