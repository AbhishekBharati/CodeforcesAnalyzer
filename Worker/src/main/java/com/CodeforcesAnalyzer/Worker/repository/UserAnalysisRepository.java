package com.CodeforcesAnalyzer.Worker.repository;

import com.CodeforcesAnalyzer.Worker.model.UserAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnalysisRepository extends MongoRepository<UserAnalysis, String> {
}
