package com.example.usersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.example.usersservice")
@EnableDiscoveryClient
public class UsersServiceApplication {
    private static final Logger log = LoggerFactory.getLogger(UsersServiceApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(UsersServiceApplication.class, args);
        log.info("🚀 Users Service started successfully on port 8081");
        log.info("📡 Registered with Eureka under name: USERS-SERVICE");
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
                            return false;
                        }
                        return true;
                    }
                });
            }
        };
    }

}
