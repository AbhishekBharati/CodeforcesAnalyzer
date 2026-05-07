package com.CodeforcesAnalyzer.Worker.service;

import com.CodeforcesAnalyzer.Worker.model.CodeforcesDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnalysisAggregator {

    private final ObjectMapper objectMapper;

    public AnalysisAggregator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> aggregateMetrics(Object rawSubmissions) {
        try {
            CodeforcesDTO data = objectMapper.convertValue(rawSubmissions, CodeforcesDTO.class);
            List<CodeforcesDTO.Submission> submissions = data.getResult();

            if (submissions == null || submissions.isEmpty()) {
                return Collections.emptyMap();
            }

            Map<String, TagMetric> tagMetrics = new HashMap<>();
            
            for (CodeforcesDTO.Submission sub : submissions) {
                if (sub.getProblem() == null || sub.getProblem().getTags() == null) continue;
                
                boolean isPassed = "OK".equals(sub.getVerdict());
                Integer rating = sub.getProblem().getRating();
                
                for (String tag : sub.getProblem().getTags()) {
                    TagMetric metric = tagMetrics.computeIfAbsent(tag, k -> new TagMetric());
                    metric.totalAttempts++;
                    if (isPassed) {
                        metric.successes++;
                        if (rating != null) {
                            metric.totalRatingPoints += rating;
                            metric.ratingCount++;
                        }
                    } else {
                        metric.failures++;
                    }
                }
            }

            // Generate detailed Topic Breakdown
            List<Map<String, Object>> topicBreakdown = tagMetrics.entrySet().stream()
                    .map(e -> {
                        TagMetric m = e.getValue();
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("topic", e.getKey());
                        map.put("solvedCount", m.successes);
                        map.put("averageDifficulty", m.ratingCount > 0 ? (double) m.totalRatingPoints / m.ratingCount : 0.0);
                        map.put("successRate", (double) m.successes / m.totalAttempts);
                        return map;
                    })
                    .sorted((a, b) -> Integer.compare((int) b.get("solvedCount"), (int) a.get("solvedCount")))
                    .collect(Collectors.toList());

            // Still keep general metrics for the top-level
            Double overallAvgRating = submissions.stream()
                    .filter(s -> "OK".equals(s.getVerdict()) && s.getProblem().getRating() != null)
                    .mapToInt(s -> s.getProblem().getRating())
                    .average().orElse(0.0);

            Map<String, Object> finalSummary = new LinkedHashMap<>();
            finalSummary.put("totalSubmissions", submissions.size());
            finalSummary.put("overallAverageRating", overallAvgRating);
            finalSummary.put("topics", topicBreakdown);

            return finalSummary;

        } catch (Exception e) {
            log.error("AGGREGATOR: Error processing metrics: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private static class TagMetric {
        int totalAttempts = 0;
        int successes = 0;
        int failures = 0;
        long totalRatingPoints = 0;
        int ratingCount = 0;
    }
}
