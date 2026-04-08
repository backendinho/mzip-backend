package com.pubgclan.controller;

import com.pubgclan.model.User;
import com.pubgclan.service.DiscordOAuthService;
import com.pubgclan.service.JwtService;
import com.pubgclan.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final DiscordOAuthService discordOAuthService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/discord")
    public ResponseEntity<Map<String, Object>> discordLogin(@RequestParam String code) {
        try {
            String accessToken = discordOAuthService.exchangeCodeForToken(code);
            if (accessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Failed to exchange authorization code"));
            }

            Map<String, Object> discordUser = discordOAuthService.getUserInfo(accessToken);
            if (discordUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Failed to fetch user info from Discord"));
            }

            String discordId = (String) discordUser.get("id");
            String username = (String) discordUser.get("username");
            String avatar = (String) discordUser.get("avatar");

            User user = userService.createOrUpdateUser(discordId, username, avatar);
            String jwt = jwtService.generateToken(user.getDiscordId(), user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "discordId", user.getDiscordId(),
                    "username", user.getUsername(),
                    "avatar", user.getAvatar(),
                    "role", user.getRole()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Discord auth error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }
}
