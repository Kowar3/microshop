package com.example.ordersservice.model;

import com.example.ordersservice.DTO.UserDTO;

public record OrderDetails(Order order, UserDTO user) {}
