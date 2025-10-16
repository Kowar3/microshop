package com.example.ordersservice.DTO;

import java.math.BigDecimal;

public record OrderDTO(Long id, Long userId, String productName, BigDecimal price) { }
