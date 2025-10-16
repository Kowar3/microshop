package com.example.ordersservice.service;

import com.example.ordersservice.DTO.UserDTO;
import com.example.ordersservice.feign.UserClient;
import com.example.ordersservice.model.Order;
import com.example.ordersservice.repository.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import je potreban

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserClient userClient;

    @Mock
    RabbitTemplate rabbitTemplate;

    @InjectMocks
    OrderService orderService;

    @Test
    void createOrder_shouldThrow_whenUserDoesNotExist() {
        Order req = new Order(null, 999L, "Mouse", BigDecimal.valueOf(19.99));

        FeignException notFound = mock(FeignException.class);
        when(notFound.status()).thenReturn(404);
        when(userClient.getUserById(999L)).thenThrow(notFound);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(req));

        assertTrue(ex.getMessage().contains("User not found with ID 999"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrow_whenServiceUnavailable() {
        Order req = new Order(null, 5L, "Tablet", BigDecimal.valueOf(299.99));

        FeignException serviceDown = mock(FeignException.class);
        when(serviceDown.status()).thenReturn(503);
        when(userClient.getUserById(5L)).thenThrow(serviceDown);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> orderService.createOrder(req));

        assertTrue(ex.getMessage().contains("Users-service unavailable"));
        verify(orderRepository, never()).save(any());
    }
}