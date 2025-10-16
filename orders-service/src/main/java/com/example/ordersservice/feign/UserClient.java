package com.example.ordersservice.feign;

import com.example.ordersservice.DTO.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-service")
public interface UserClient {

    @GetMapping("/users/{id}")
    UserDTO getUserById(@PathVariable Long id);
}