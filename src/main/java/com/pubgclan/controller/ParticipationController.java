package com.pubgclan.controller;

import com.pubgclan.model.Participation;
import com.pubgclan.service.ParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @GetMapping("/weekly")
    public ResponseEntity<List<Participation>> getWeeklyParticipations() {
        return ResponseEntity.ok(participationService.getWeeklyParticipations());
    }

    @GetMapping("/weekly/summary")
    public ResponseEntity<List<Map<String, Object>>> getWeeklySummary(
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(participationService.getWeeklySummary(offset));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<Participation>> getDailyParticipations() {
        return ResponseEntity.ok(participationService.getDailyParticipations());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        String discordId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(participationService.getStats(discordId));
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<Map<String, Long>> getWeeklyStats() {
        return ResponseEntity.ok(participationService.getWeeklyStats());
    }

    @GetMapping("/range")
    public ResponseEntity<List<Participation>> getParticipationsInRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(participationService.getAllInRange(start, end));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> recordManual(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String username = body.get("username");
        String dateStr = body.get("date");

        if (userId == null || username == null || dateStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId, username, date 는 필수입니다."));
        }

        LocalDate date = LocalDate.parse(dateStr);
        participationService.recordParticipationOnDate(userId, username, date);

        Map<String, Object> result = new HashMap<>();
        result.put("message", username + "의 " + date + " 참여 기록이 등록되었습니다.");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Participation>> getUserParticipations(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        LocalDate startDate = start != null ? start : LocalDate.now().minusMonths(1);
        LocalDate endDate = end != null ? end : LocalDate.now();
        return ResponseEntity.ok(participationService.getUserParticipations(userId, startDate, endDate));
    }

    @PostMapping("/add-manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> addParticipationManually(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String username = (String) body.get("username");
        int year = ((Number) body.get("year")).intValue();
        int week = ((Number) body.get("week")).intValue();

        if (userId == null || username == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId, username은 필수입니다."));
        }

        Participation participation = participationService.addParticipationManually(userId, username, year, week);
        return ResponseEntity.ok(Map.of("message", username + "의 " + year + "년 " + week + "주 참여 기록이 추가되었습니다.", "participation", participation));
    }

    @DeleteMapping("/{participationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> removeParticipation(@PathVariable String participationId) {
        participationService.removeParticipation(participationId);
        return ResponseEntity.ok(Map.of("message", "참여 기록이 삭제되었습니다."));
    }
}
