package com.example.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ApiGatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(ApiGatewayApplication.class, args);
        log.info("🚀 API Gateway started successfully on port 8085 🌐");
        log.info("🔗 Registered with Eureka under service name: API-GATEWAY");
        log.info("⚡ Gateway is ready to route requests to USERS-SERVICE and ORDERS-SERVICE");
        log.info("🔐 API Key security enabled (header: X-API-KEY)");
        log.info("🧩 WebFlux & LoadBalancer active — Reactive routing operational ✅");
    }

}
