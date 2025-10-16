package com.example.ordersservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("orderCreatedQueue", false);
    }
}
