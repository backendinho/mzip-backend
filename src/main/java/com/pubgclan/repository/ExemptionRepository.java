package com.pubgclan.repository;

import com.pubgclan.model.Exemption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExemptionRepository extends MongoRepository<Exemption, String> {
    List<Exemption> findByUserId(String userId);

    Exemption findByUserIdAndYearAndWeek(String userId, int year, int week);

    List<Exemption> findByYearAndWeek(int year, int week);
}
