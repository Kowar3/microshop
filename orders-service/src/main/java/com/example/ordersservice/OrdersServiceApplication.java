package com.example.ordersservice;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@SpringBootApplication
@EnableFeignClients

public class OrdersServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(OrdersServiceApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(OrdersServiceApplication.class, args);
        log.info("🚀 Orders Service started successfully on port 8082");
        log.info("📡 Registered with Eureka as ORDERS-SERVICE");
        log.info("🤝 Feign client enabled for USERS-SERVICE communication");
        log.info("🛡️ Resilience4j (CircuitBreaker + Retry) active and ready");
        log.info("🐇 RabbitMQ integration loaded for async events");
    }

    @Bean
    public WebMvcConfigurer gatewayHeaderCheck() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
                        String fromGateway = req.getHeader("X-From-Gateway");

                        if (!"true".equalsIgnoreCase(fromGateway)) {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.getWriter().write("401 Unauthorized – Access only through API Gateway");
                            log.warn("❌ Unauthorized access attempt: {}", req.getRequestURI());
                            return false;
                        }

                        return true;
                    }
                });
            }
        };
    }

    @Bean
    public RequestInterceptor gatewayHeaderForwarder() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-From-Gateway", "true");
            }
        };
    }
}
