package com.pubgclan.service;

import com.pubgclan.model.Participation;
import com.pubgclan.model.User;
import com.pubgclan.repository.ParticipationRepository;
import com.pubgclan.repository.UserRepository;
import com.pubgclan.repository.ExemptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final ExemptionRepository exemptionRepository;

    private String formatDate(LocalDate date) {
        return DateTimeFormatter.ISO_DATE.format(date);
    }

    public Participation recordParticipation(String userId, String username) {
        return recordParticipationOnDate(userId, username, LocalDate.now(ZoneId.of("Asia/Seoul")), null);
    }

    public Participation recordParticipationOnDate(String userId, String username, LocalDate date) {
        return recordParticipationOnDate(userId, username, date, null);
    }

    public Participation recordParticipationOnDate(String userId, String username, LocalDate date, String messageId) {
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = date.getYear();
        String dateStr = formatDate(date);  // "2026-04-08" 형식

        // 메시지 ID + userId가 있으면 먼저 확인 (같은 메시지에서 같은 유저 중복 방지)
        if (messageId != null && !messageId.isBlank()) {
            Participation existing = participationRepository.findByMessageIdAndUserId(messageId, userId);
            if (existing != null) {
                log.info("Participation already recorded for message {} and user {}", messageId, userId);
                return existing;
            }
        }

        // 새 참여 기록 생성 (같은 메시지ID + userId가 아닌 이상 중복 허용)
        Participation participation = Participation.builder()
                .userId(userId)
                .username(username)
                .messageId(messageId)
                .date(dateStr)
                .week(week)
                .year(year)
                .build();

        return participationRepository.save(participation);
    }

    public List<Participation> getWeeklyParticipations() {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = now.getYear();
        return participationRepository.findByYearAndWeek(year, week);
    }

    public List<Participation> getDailyParticipations() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String todayStr = formatDate(today);
        return participationRepository.findByDateBetween(todayStr, todayStr);
    }

    public List<Participation> getUserParticipations(String userId, LocalDate startDate, LocalDate endDate) {
        return participationRepository.findByUserIdAndDateBetween(userId, formatDate(startDate), formatDate(endDate));
    }

    public List<Map<String, Object>> getWeeklySummary(int weekOffset) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul")).plusWeeks(weekOffset);
        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = now.getYear();

        // week + year 기준으로 그 주의 모든 참여 기록 조회 (date String 범위 비교 문제 해결)
        List<Participation> records = participationRepository.findByYearAndWeek(year, week);
        Map<String, Long> countByUserId = records.stream()
                .collect(Collectors.groupingBy(Participation::getUserId, Collectors.counting()));

        // 전체 유저를 기준으로 참여 횟수 합산 (참여 안 한 유저는 0)
        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (User user : allUsers) {
            int count = countByUserId.getOrDefault(user.getDiscordId(), 0L).intValue();
            
            // 면제 여부 확인
            boolean isExempt = exemptionRepository.findByUserIdAndYearAndWeek(user.getDiscordId(), year, week) != null;
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", user.getDiscordId());
            summary.put("username", user.getUsername());
            summary.put("weeklyCount", count);
            summary.put("isExempt", isExempt);
            // 면제되었거나 count >= 2이면 "met"
            summary.put("status", (isExempt || count >= 2) ? "met" : "below");
            result.add(summary);
        }

        // 참여 횟수 내림차순 정렬
        result.sort((a, b) -> Integer.compare((int) b.get("weeklyCount"), (int) a.get("weeklyCount")));
        return result;
    }

    public Map<String, Long> getWeeklyStats() {
        List<Participation> weekly = getWeeklyParticipations();
        return weekly.stream()
                .collect(Collectors.groupingBy(Participation::getUsername, Collectors.counting()));
    }

    public Map<String, Object> getStats(String userId) {
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        int week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int year = now.getYear();

        long weeklyCount = participationRepository.countByUserIdAndYearAndWeek(userId, year, week);
        List<Participation> monthlyList = participationRepository.findByUserIdAndDateBetween(userId, formatDate(startOfMonth), formatDate(endOfMonth));

        Map<String, Object> stats = new HashMap<>();
        stats.put("weeklyCount", weeklyCount);
        stats.put("monthlyCount", (long) monthlyList.size());
        stats.put("weekStart", startOfWeek.toString());
        stats.put("weekEnd", endOfWeek.toString());

        return stats;
    }

    public List<Participation> getAllInRange(LocalDate startDate, LocalDate endDate) {
        return participationRepository.findByDateBetween(formatDate(startDate), formatDate(endDate));
    }

    public Participation addParticipationManually(String userId, String username, int year, int week) {
        // 주의: 관리자가 수동으로 추가하는 경우 (Discord 날짜 계산 필요)
        // 주어진 주의 월요일을 계산
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate date = now.plusWeeks(0); // 기본값: 현재 주

        // year와 week에 맞는 날짜 계산
        try {
            date = LocalDate.now()
                    .withYear(year)
                    .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week);
        } catch (Exception e) {
            log.warn("Invalid year/week: {}/{}, using current date", year, week);
        }

        return recordParticipationOnDate(userId, username, date, "manual-" + System.currentTimeMillis());
    }

    public void removeParticipation(String participationId) {
        participationRepository.deleteById(participationId);
        log.info("Participation removed: {}", participationId);
    }
}

