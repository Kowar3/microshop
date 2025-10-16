package com.example.ordersservice.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @RabbitListener(queues = "orderCreatedQueue")
    public void handleOrderCreated(String message) {
        log.info("📬 [RabbitMQ] Received new event from 'orderCreatedQueue'");
        log.info("💡 Event content: {}", message);
        log.info("✅ Event successfully processed and acknowledged 🟢");
    }
}
