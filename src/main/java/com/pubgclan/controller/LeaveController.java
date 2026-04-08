package com.pubgclan.controller;

import com.pubgclan.model.Leave;
import com.pubgclan.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    @Autowired
    private LeaveService leaveService;

    @PostMapping
    public ResponseEntity<Leave> createLeave(@RequestBody Leave leave) {
        Leave created = leaveService.createLeave(leave);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Leave>> getUserLeaves(@PathVariable String userId) {
        List<Leave> leaves = leaveService.getUserLeaves(userId);
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Leave>> getPendingLeaves() {
        List<Leave> leaves = leaveService.getPendingLeaves();
        return ResponseEntity.ok(leaves);
    }

    @GetMapping("/{leaveId}")
    public ResponseEntity<Leave> getLeaveById(@PathVariable String leaveId) {
        Leave leave = leaveService.getLeaveById(leaveId);
        return ResponseEntity.ok(leave);
    }

    @PutMapping("/{leaveId}/approve")
    public ResponseEntity<Leave> approveLeave(@PathVariable String leaveId, @RequestBody Map<String, String> body) {
        String adminUserId = body.get("adminUserId");
        Leave approved = leaveService.approveLeave(leaveId, adminUserId);
        return ResponseEntity.ok(approved);
    }

    @PutMapping("/{leaveId}/reject")
    public ResponseEntity<Leave> rejectLeave(@PathVariable String leaveId, @RequestBody Map<String, String> body) {
        String adminUserId = body.get("adminUserId");
        Leave rejected = leaveService.rejectLeave(leaveId, adminUserId);
        return ResponseEntity.ok(rejected);
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave(@PathVariable String leaveId) {
        leaveService.deleteLeave(leaveId);
        return ResponseEntity.ok().build();
    }
}
