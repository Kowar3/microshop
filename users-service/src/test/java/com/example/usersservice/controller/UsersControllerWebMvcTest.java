package com.example.usersservice.controller;

import com.example.usersservice.model.User;
import com.example.usersservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UsersControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // ✅ Header koji simulira da zahtev dolazi kroz API Gateway
    private final HttpHeaders gatewayHeaders = new HttpHeaders();
    {
        gatewayHeaders.add("X-From-Gateway", "true");
    }

    @Test
    void getById_shouldReturn404_whenUserNotFound() throws Exception {
        Mockito.when(userService.getById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get("/users/999")
                                .headers(gatewayHeaders) // 🔥 Ključno: dodaj header
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found with ID 999")));
    }

    @Test
    void getById_shouldReturn200_whenUserExists() throws Exception {
        var user = new User("Pera", "pera@example.com", "sekret1");
        user.setId(1L);
        Mockito.when(userService.getById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(
                        get("/users/1")
                                .headers(gatewayHeaders) // 🔥 Ključno: dodaj header
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("pera@example.com"));
    }
}