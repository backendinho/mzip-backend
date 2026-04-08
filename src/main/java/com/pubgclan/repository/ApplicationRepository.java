package com.pubgclan.repository;

import com.pubgclan.model.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {

    List<Application> findByStatus(String status);
}
