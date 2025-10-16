package com.example.ordersservice.service;

import com.example.ordersservice.DTO.OrderDTO;
import com.example.ordersservice.DTO.UserDTO;
import com.example.ordersservice.feign.UserClient;
import com.example.ordersservice.model.Order;
import com.example.ordersservice.model.OrderDetails;
import com.example.ordersservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import feign.FeignException;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.ordersservice.publisher.OrderEventPublisher;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserClient userClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final OrderEventPublisher publisher;

    public OrderService(OrderRepository orderRepository, UserClient userClient, OrderEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.userClient = userClient;
        this.publisher = publisher;
    }

    public OrderDTO toDTO(Order order) {
        return new OrderDTO(order.getId(), order.getUserId(), order.getProductName(), order.getPrice());
    }

    public Order fromDTO(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.id());
        order.setUserId(dto.userId());
        order.setProductName(dto.productName());
        order.setPrice(dto.price());
        return order;
    }

    public List<Order> getAllOrders() {
        log.info("📋 Fetching all orders from database...");
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        log.info("🔍 Searching for order with ID {}", id);
        return orderRepository.findById(id);
    }

    @Retry(name = "userService")
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackCreateOrder")
    public Order createOrder(Order order) {
        try {
            log.info("🧾 Checking user ID {} via users-service...", order.getUserId());
            userClient.getUserById(order.getUserId());
            log.info("✅ User exists — saving order to database...");
            Order savedOrder = orderRepository.save(order);

            String message = "📦 Novi order kreiran! ID: " + savedOrder.getId() +
                    ", UserID: " + savedOrder.getUserId() +
                    ", Proizvod: " + savedOrder.getProductName();
            rabbitTemplate.convertAndSend("orderCreatedQueue", message);
            log.info("📨 Sent event 'OrderCreated' to RabbitMQ for Order ID {}", savedOrder.getId());

            return savedOrder;

        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("⚠️ User not found in users-service (404) — cannot create order");
                throw new IllegalArgumentException("User not found with ID " + order.getUserId());
            }
            log.error("💥 Users-service unavailable or failed: {}", e.getMessage());
            throw new IllegalStateException("Users-service unavailable");
        }
    }

    public Order updateOrder(Long id, Order updated) {
        log.info("✏️ Updating order with ID {}", id);

        return orderRepository.findById(id)
                .map(existing -> {
                    existing.setProductName(updated.getProductName());
                    existing.setPrice(updated.getPrice());
                    existing.setUserId(updated.getUserId());
                    return orderRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID " + id));
    }

    public Order fallbackCreateOrder(Order order, Throwable t) {
        if (t instanceof IllegalArgumentException) {
            log.warn("🚫 Fallback bypassed — user not found: {}", t.getMessage());
            throw (IllegalArgumentException) t;
        }

        log.error("⚡ Fallback triggered! Reason: {}", t.getMessage());
        log.warn("🚧 CircuitBreaker OPEN — users-service unavailable");
        throw new IllegalStateException("Users-service unavailable — please try again later.");
    }

    public void deleteOrder(Long id) {
        log.info("🗑️ Deleting order with ID {}", id);
        if (!orderRepository.existsById(id)) {
            throw new IllegalArgumentException("Order not found with ID " + id);
        }
        orderRepository.deleteById(id);
        log.info("✅ Successfully deleted order with ID {}", id);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetOrderDetails")
    @Retry(name = "userService")
    public OrderDetails getOrderDetails(Long id) {
        log.info("🔗 Fetching detailed info for order ID {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID " + id));

        UserDTO user = userClient.getUserById(order.getUserId());
        return new OrderDetails(order, user);
    }

    public OrderDetails fallbackGetOrderDetails(Long id, Throwable t) {
        log.warn("⚠️ Users-service unavailable — returning partial order data for ID {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID " + id));

        UserDTO fallbackUser = new UserDTO(null, "Unknown user (service unavailable)", "-");
        return new OrderDetails(order, fallbackUser);
    }
}