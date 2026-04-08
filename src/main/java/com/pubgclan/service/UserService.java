package com.pubgclan.service;

import com.pubgclan.model.User;
import com.pubgclan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByDiscordId(String discordId) {
        return userRepository.findByDiscordId(discordId);
    }

    public User createOrUpdateUser(String discordId, String username, String avatar) {
        Optional<User> existing = userRepository.findByDiscordId(discordId);

        if (existing.isPresent()) {
            User user = existing.get();
            user.setUsername(username);
            user.setAvatar(avatar);
            return userRepository.save(user);
        }

        User newUser = User.builder()
                .discordId(discordId)
                .username(username)
                .avatar(avatar)
                .role("USER")
                .createdAt(LocalDate.now())
                .build();
        return userRepository.save(newUser);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User updateRole(String id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteById(String id) {
        userRepository.deleteById(id);
    }
}
