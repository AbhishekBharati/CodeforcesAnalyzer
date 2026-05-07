package com.CodeforcesAnalyzer.Backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_analysis")
public class UserAnalysis {
    @Id
    private String handle;
    private LocalDateTime lastUpdated;
    private Object rawSubmissions;
    private String status; // PENDING, PROCESSING, COMPLETED, ERROR
    private String aiRecommendation;
    private Object topicMetrics;
}
