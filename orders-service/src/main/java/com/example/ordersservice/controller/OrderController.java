package com.example.ordersservice.controller;

import com.example.ordersservice.feign.UserClient;
import com.example.ordersservice.DTO.UserDTO;
import com.example.ordersservice.model.Order;
import com.example.ordersservice.model.OrderDetails;
import com.example.ordersservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final UserClient userClient;

    public OrderController(OrderService orderService, UserClient userClient) {
        this.orderService = orderService;
        this.userClient = userClient;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("📋 Fetching all orders...");
        List<Order> orders = orderService.getAllOrders();
        log.info("✅ {} orders retrieved successfully", orders.size());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        log.info("🔍 Getting order with ID {}", id);
        return orderService.getOrderById(id)
                .<ResponseEntity<?>>map(o -> {
                    log.info("✅ Found order with ID {}", id);
                    return ResponseEntity.ok(o);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Order not found with ID {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Order not found with ID " + id);
                });
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        log.info("🔗 Fetching details for order ID {}", id);
        return orderService.getOrderById(id)
                .<ResponseEntity<?>>map(o -> {
                    UserDTO u = userClient.getUserById(o.getUserId());
                    log.info("✅ Combined Order + User info for ID {}", id);
                    return ResponseEntity.ok(new OrderDetails(o, u));
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Order not found with ID {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Order not found with ID " + id);
                });
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
        log.info("🧾 Request to create new order for user ID {}", order.getUserId());
        try {
            Order saved = orderService.createOrder(order);
            log.info("✅ Order created successfully with ID {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(orderService.toDTO(saved));
        } catch (IllegalArgumentException e) {
            log.warn("🚫 Validation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("💥 Service unavailable: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @Valid @RequestBody Order updatedOrder) {
        log.info("✏️ Request to update order with ID {}", id);
        try {
            Order saved = orderService.updateOrder(id, updatedOrder);
            log.info("✅ Order {} updated successfully", id);
            return ResponseEntity.ok(orderService.toDTO(saved));
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("💥 Error updating order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating order.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        log.info("🗑️ Request to delete order with ID {}", id);
        try {
            orderService.deleteOrder(id);
            log.info("✅ Successfully deleted order {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("💥 Error deleting order {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found with ID " + id);
        }
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationError(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("🚫 Validation error: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeError(RuntimeException ex) {
        log.error("💥 Runtime error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}