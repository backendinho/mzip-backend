package com.pubgclan.service;

import com.pubgclan.model.Exemption;
import com.pubgclan.repository.ExemptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExemptionService {

    @Autowired
    private ExemptionRepository exemptionRepository;

    public Exemption createExemption(String userId, String username, int year, int week, String reason, String adminUserId) {
        // 이미 존재하는지 확인
        Exemption existing = exemptionRepository.findByUserIdAndYearAndWeek(userId, year, week);
        if (existing != null) {
            return existing;
        }

        Exemption exemption = Exemption.builder()
                .userId(userId)
                .username(username)
                .year(year)
                .week(week)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .createdBy(adminUserId)
                .build();

        return exemptionRepository.save(exemption);
    }

    public Exemption getExemption(String userId, int year, int week) {
        return exemptionRepository.findByUserIdAndYearAndWeek(userId, year, week);
    }

    public List<Exemption> getUserExemptions(String userId) {
        return exemptionRepository.findByUserId(userId);
    }

    public List<Exemption> getWeekExemptions(int year, int week) {
        return exemptionRepository.findByYearAndWeek(year, week);
    }

    public void deleteExemption(String exemptionId) {
        exemptionRepository.deleteById(exemptionId);
    }

    public void deleteExemptionByUserAndWeek(String userId, int year, int week) {
        Exemption exemption = exemptionRepository.findByUserIdAndYearAndWeek(userId, year, week);
        if (exemption != null) {
            exemptionRepository.deleteById(exemption.getId());
        }
    }
}
