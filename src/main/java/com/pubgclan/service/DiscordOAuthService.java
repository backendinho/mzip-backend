package com.pubgclan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordOAuthService {

    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";
    private static final String TOKEN_URL = DISCORD_API_BASE + "/oauth2/token";
    private static final String USER_URL = DISCORD_API_BASE + "/users/@me";

    @Value("${discord.client.id}")
    private String clientId;

    @Value("${discord.client.secret}")
    private String clientSecret;

    @Value("${discord.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;

    public String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("access_token")) {
                return (String) responseBody.get("access_token");
            }
        } catch (Exception e) {
            log.error("Failed to exchange code for token: {}", e.getMessage());
        }
        return null;
    }

    public Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(USER_URL, HttpMethod.GET, request, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get user info from Discord: {}", e.getMessage());
        }
        return null;
    }
}
