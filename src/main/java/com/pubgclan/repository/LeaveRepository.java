package com.pubgclan.repository;

import com.pubgclan.model.Leave;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRepository extends MongoRepository<Leave, String> {
    List<Leave> findByUserId(String userId);

    List<Leave> findByStatus(String status);

    List<Leave> findByStatusAndApprovedByIsNull(String status);

    Leave findByIdAndUserId(String id, String userId);
}
