package com.pubgclan.controller;

import com.pubgclan.model.User;
import com.pubgclan.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String discordId = (String) authentication.getPrincipal();
        return userService.findByDiscordId(discordId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userUpdate) {
        return userService.findById(id)
                .map(existing -> {
                    if (userUpdate.getUsername() != null) {
                        existing.setUsername(userUpdate.getUsername());
                    }
                    if (userUpdate.getAvatar() != null) {
                        existing.setAvatar(userUpdate.getAvatar());
                    }
                    return ResponseEntity.ok(userService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUserRole(@PathVariable String id,
                                               @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            User updated = userService.updateRole(id, role);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
