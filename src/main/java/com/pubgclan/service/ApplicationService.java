package com.pubgclan.service;

import com.pubgclan.model.Application;
import com.pubgclan.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    public Application createApplication(Application application) {
        application.setStatus("pending");
        application.setCreatedAt(LocalDateTime.now());
        Application saved = applicationRepository.save(application);
        sendWebhookNotification(saved);
        return saved;
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public List<Application> findByStatus(String status) {
        return applicationRepository.findByStatus(status);
    }

    public Optional<Application> findById(String id) {
        return applicationRepository.findById(id);
    }

    public Application updateStatus(String id, String status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        application.setStatus(status);
        Application updated = applicationRepository.save(application);
        sendStatusUpdateWebhook(updated);
        return updated;
    }

    private void sendWebhookNotification(Application application) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("Discord webhook URL not configured. Skipping notification.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String content = String.format(
                    "**New Clan Application Received!**\n" +
                    "Nickname: **%s**\n" +
                    "Discord ID: %s\n" +
                    "Tier: %s\n" +
                    "Play Time: %s\n" +
                    "Introduction: %s",
                    application.getNickname(),
                    application.getDiscordId(),
                    application.getTier(),
                    application.getPlayTime(),
                    application.getIntro()
            );

            Map<String, Object> body = new HashMap<>();
            body.put("content", content);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(webhookUrl, request, String.class);
            log.info("Webhook notification sent for application: {}", application.getId());
        } catch (Exception e) {
            log.error("Failed to send webhook notification: {}", e.getMessage());
        }
    }

    private void sendStatusUpdateWebhook(Application application) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String emoji = switch (application.getStatus()) {
                case "approved" -> "✅";
                case "rejected" -> "❌";
                default -> "⏳";
            };

            String content = String.format(
                    "%s **Application Status Updated**\n" +
                    "Nickname: **%s**\n" +
                    "New Status: **%s**",
                    emoji,
                    application.getNickname(),
                    application.getStatus().toUpperCase()
            );

            Map<String, Object> body = new HashMap<>();
            body.put("content", content);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject(webhookUrl, request, String.class);
        } catch (Exception e) {
            log.error("Failed to send status update webhook: {}", e.getMessage());
        }
    }
}
