package com.CodeforcesAnalyzer.Worker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateRoadmap(String handle, Map<String, Object> metrics) {
        log.info("AI: Generating roadmap for {} using OpenAI...", handle);

        String systemPrompt = """
                You are a world-class Competitive Programming Coach.
                Your task is to analyze a user's Codeforces performance data and provide a surgical, 4-week study roadmap.
                Focus on identifying the 'Glass Ceiling' (the rating where they fail) and specific topic weaknesses.
                Be concise, technical, and encouraging. Use Markdown for formatting.
                """;

        String userPrompt = String.format("""
                Analyze the following performance metrics for Codeforces user '%s':

                %s

                Based on this, provide:
                1. A summary of their current standing.
                2. Top 3 topic weaknesses to fix.
                3. A week-by-week study plan with suggested problem rating ranges.
                """, handle, formatMetrics(metrics));

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", Arrays.asList(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }

            return "Error: AI could not generate a response at this time.";

        } catch (Exception e) {
            log.error("AI: Error calling OpenAI API: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String formatMetrics(Map<String, Object> metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("- Overall Average Solved Rating: ").append(metrics.get("overallAverageRating")).append("\n");
        sb.append("- Total Submissions Processed: ").append(metrics.get("totalSubmissions")).append("\n");
        sb.append("- Topic Breakdown (Top Topics):\n");

        List<Map<String, Object>> topics = (List<Map<String, Object>>) metrics.get("topics");
        if (topics != null) {
            topics.stream().limit(15).forEach(t -> {
                sb.append(String.format("  * %s: Solved %d problems (Avg Difficulty: %.0f, Success Rate: %.1f%%)\n",
                        t.get("topic"),
                        t.get("solvedCount"),
                        (Double) t.get("averageDifficulty"),
                        ((Double) t.get("successRate") * 100)));
            });
        }

        return sb.toString();
    }
}
