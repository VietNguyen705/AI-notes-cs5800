package com.notesapp.controllers;

import com.notesapp.entities.User;
import com.notesapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody Map<String, String> userData) {
        try {
            String username = userData.get("username");
            String email = userData.get("email");

            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);

            User savedUser = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/preferences")
    public ResponseEntity<User> updatePreferences(@PathVariable String id, @RequestBody Map<String, String> preferences) {
        return userRepository.findById(id)
            .map(user -> {
                user.updatePreferences(preferences);
                User updated = userRepository.save(user);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
