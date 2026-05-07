package com.CodeforcesAnalyzer.Backend.service;

import com.CodeforcesAnalyzer.Backend.config.RabbitMQConfig;
import com.CodeforcesAnalyzer.Backend.model.UserAnalysis;
import com.CodeforcesAnalyzer.Backend.repository.UserAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CodeforcesService {
    private static final Logger log = LoggerFactory.getLogger(CodeforcesService.class);
    private final RestTemplate restTemplate;
    private final UserAnalysisRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final String CF_API_URL = "https://codeforces.com/api/user.status?handle=";

    public CodeforcesService(RestTemplate restTemplate, UserAnalysisRepository repository, RabbitTemplate rabbitTemplate) {
        this.restTemplate = restTemplate;
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public UserAnalysis processUserAnalysisRequest(String handle) {
        log.info("Processing analysis request for handle: {}", handle);
        Optional<UserAnalysis> cachedData = repository.findById(handle);

        if (cachedData.isPresent()) {
            UserAnalysis analysis = cachedData.get();
            // If completed and fresh (less than 24h), return it
            if ("COMPLETED".equals(analysis.getStatus()) && 
                analysis.getLastUpdated().isAfter(LocalDateTime.now().minusDays(1))) {
                log.info("CACHE HIT: Returning completed analysis for handle: {}", handle);
                return analysis;
            }
            
            // If already pending/processing, just return the current state
            if ("PENDING".equals(analysis.getStatus()) || "PROCESSING".equals(analysis.getStatus())) {
                log.info("ALREADY IN PROGRESS: Returning status {} for handle: {}", analysis.getStatus(), handle);
                return analysis;
            }
        }

        // Cache miss or stale: Start new ingestion
        log.info("INGESTION START: Fetching raw data for handle: {}", handle);
        return fetchAndQueue(handle);
    }

    private UserAnalysis fetchAndQueue(String handle) {
        String url = CF_API_URL + handle;
        try {
            Object response = restTemplate.getForObject(url, Object.class);
            log.info("EXTERNAL API: Successfully retrieved data for handle: {}", handle);

            UserAnalysis analysis = UserAnalysis.builder()
                    .handle(handle)
                    .lastUpdated(LocalDateTime.now())
                    .rawSubmissions(response)
                    .status("PENDING")
                    .build();

            log.info("DATABASE: Saving raw data for handle: {}", handle);
            UserAnalysis saved = repository.save(analysis);

            // Trigger RabbitMQ for the Worker (Phase 2)
            log.info("RABBITMQ: Sending analysis request for handle: {}", handle);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, handle);

            return saved;
        } catch (Exception e) {
            log.error("ERROR: Failed to fetch data for handle: {}. Message: {}", handle, e.getMessage());
            throw e;
        }
    }
}
