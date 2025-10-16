package com.example.usersservice.controller;

import com.example.usersservice.DTO.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.usersservice.model.User;
import com.example.usersservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        log.info("📋 Fetching all users...");
        List<UserDTO> users = userService.getAll()
                .stream()
                .map(userService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        log.info("🔍 Fetching user with ID {}", id);
        return userService.getById(id)
                .<ResponseEntity<Object>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("⚠️ User not found with ID {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("User not found with ID " + id);
                });
    }

    @PostMapping
    public ResponseEntity<?> create(@Validated @RequestBody User user) {
        log.info("🆕 Creating new user with email {}", user.getEmail());
        try {
            User saved = userService.create(user);
            log.info("✅ Successfully created user with ID {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.toDTO(saved));
        } catch (Exception e) {
            log.error("💥 Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Validated @RequestBody User updated) {
        log.info("✏️ Updating user with ID {}", id);
        try {
            User saved = userService.update(id, updated);
            log.info("✅ Successfully updated user with ID {}", id);
            return ResponseEntity.ok(userService.toDTO(saved));
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("💥 Error updating user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating user.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("🗑️ Deleting user with ID {}", id);
        try {
            userService.delete(id);
            log.info("✅ Successfully deleted user {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("💥 Error deleting user {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with ID " + id);
        }
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationError(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("🚫 Validation failed: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("⚠️ {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}