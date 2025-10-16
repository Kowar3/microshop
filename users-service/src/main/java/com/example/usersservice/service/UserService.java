package com.example.usersservice.service;

import com.example.usersservice.DTO.UserDTO;
import com.example.usersservice.controller.UserController;
import com.example.usersservice.model.User;
import com.example.usersservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }

    public User fromDTO(UserDTO dto) {
        User user = new User();
        user.setId(dto.id());
        user.setName(dto.name());
        user.setEmail(dto.email());
        return user;
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    public User create(User user) {
        log.debug("🧩 Checking if email {} already exists...", user.getEmail());
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("🚫 User with email {} already exists!", user.getEmail());
            throw new IllegalArgumentException("User with this email already exists.");
        }
        log.info("💾 Saving new user {}", user.getName());
        return userRepository.save(user);
    }

    public User update(Long id, User updated) {
        return userRepository.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    existing.setEmail(updated.getEmail());
                    existing.setPassword(updated.getPassword());
                    return userRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found with id " + id));
    }

    public void delete(Long id) {

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID " + id);
        }
        userRepository.deleteById(id);
    }
}
