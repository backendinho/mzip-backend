package com.pubgclan.controller;

import com.pubgclan.model.Exemption;
import com.pubgclan.service.ExemptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exemptions")
public class ExemptionController {

    @Autowired
    private ExemptionService exemptionService;

    @PostMapping
    public ResponseEntity<Exemption> createExemption(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String username = (String) body.get("username");
        int year = ((Number) body.get("year")).intValue();
        int week = ((Number) body.get("week")).intValue();
        String reason = (String) body.get("reason");
        String adminUserId = (String) body.get("adminUserId");

        Exemption created = exemptionService.createExemption(userId, username, year, week, reason, adminUserId);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Exemption>> getUserExemptions(@PathVariable String userId) {
        List<Exemption> exemptions = exemptionService.getUserExemptions(userId);
        return ResponseEntity.ok(exemptions);
    }

    @GetMapping("/week/{year}/{week}")
    public ResponseEntity<List<Exemption>> getWeekExemptions(@PathVariable int year, @PathVariable int week) {
        List<Exemption> exemptions = exemptionService.getWeekExemptions(year, week);
        return ResponseEntity.ok(exemptions);
    }

    @DeleteMapping("/{exemptionId}")
    public ResponseEntity<Void> deleteExemption(@PathVariable String exemptionId) {
        exemptionService.deleteExemption(exemptionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/{userId}/week/{year}/{week}")
    public ResponseEntity<Void> deleteExemptionByWeek(@PathVariable String userId, @PathVariable int year, @PathVariable int week) {
        exemptionService.deleteExemptionByUserAndWeek(userId, year, week);
        return ResponseEntity.ok().build();
    }
}
