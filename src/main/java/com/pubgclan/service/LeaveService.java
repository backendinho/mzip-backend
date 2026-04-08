package com.pubgclan.service;

import com.pubgclan.model.Leave;
import com.pubgclan.repository.LeaveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveService {

    @Autowired
    private LeaveRepository leaveRepository;

    public Leave createLeave(Leave leave) {
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now());
        return leaveRepository.save(leave);
    }

    public Leave approveLeave(String leaveId, String adminUserId) {
        Leave leave = leaveRepository.findById(leaveId).orElse(null);
        if (leave != null) {
            leave.setStatus("APPROVED");
            leave.setApprovedAt(LocalDateTime.now());
            leave.setApprovedBy(adminUserId);
            return leaveRepository.save(leave);
        }
        return null;
    }

    public Leave rejectLeave(String leaveId, String adminUserId) {
        Leave leave = leaveRepository.findById(leaveId).orElse(null);
        if (leave != null) {
            leave.setStatus("REJECTED");
            leave.setApprovedAt(LocalDateTime.now());
            leave.setApprovedBy(adminUserId);
            return leaveRepository.save(leave);
        }
        return null;
    }

    public List<Leave> getUserLeaves(String userId) {
        return leaveRepository.findByUserId(userId);
    }

    public List<Leave> getPendingLeaves() {
        return leaveRepository.findByStatusAndApprovedByIsNull("PENDING");
    }

    public Leave getLeaveById(String leaveId) {
        return leaveRepository.findById(leaveId).orElse(null);
    }

    public void deleteLeave(String leaveId) {
        leaveRepository.deleteById(leaveId);
    }
}
