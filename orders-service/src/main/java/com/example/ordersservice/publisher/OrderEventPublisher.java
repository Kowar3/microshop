package com.example.ordersservice.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderCreatedEvent(String message) {
        log.info("📨 Sending event to RabbitMQ queue 'orderCreatedQueue'...");
        log.info("📦 Message content → {}", message);
        try {
            rabbitTemplate.convertAndSend("orderCreatedQueue", message);
            log.info("✅ Message successfully sent to RabbitMQ 🎯");
        } catch (Exception e) {
            log.error("💥 Failed to send message to RabbitMQ: {}", e.getMessage());
        }
    }
}