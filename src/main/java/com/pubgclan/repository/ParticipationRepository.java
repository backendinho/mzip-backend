package com.pubgclan.repository;

import com.pubgclan.model.Participation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipationRepository extends MongoRepository<Participation, String> {

    // date는 "2026-04-08" 형식의 String
    List<Participation> findByUserIdAndDateBetween(String userId, String startDate, String endDate);

    List<Participation> findByDateBetween(String startDate, String endDate);

    long countByUserIdAndYearAndWeek(String userId, int year, int week);

    Participation findByMessageIdAndUserId(String messageId, String userId);

    // 주(week) 기준 쿼리
    List<Participation> findByYearAndWeek(int year, int week);
}
