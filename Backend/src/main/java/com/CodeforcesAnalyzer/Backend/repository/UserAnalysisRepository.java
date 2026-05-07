package com.CodeforcesAnalyzer.Backend.repository;

import com.CodeforcesAnalyzer.Backend.model.UserAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnalysisRepository extends MongoRepository<UserAnalysis, String> {
}
