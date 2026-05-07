package com.CodeforcesAnalyzer.Backend.service;

import com.CodeforcesAnalyzer.Backend.model.UserAnalysis;
import com.CodeforcesAnalyzer.Backend.repository.UserAnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CodeforcesService {
    private static final Logger log = LoggerFactory.getLogger(CodeforcesService.class);
    private final RestTemplate restTemplate;
    private final UserAnalysisRepository repository;
    private final String CF_API_URL = "https://codeforces.com/api/user.status?handle=";

    public CodeforcesService(RestTemplate restTemplate, UserAnalysisRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public UserAnalysis getUserStatus(String handle) {
        log.info("Incoming request to analyze handle: {}", handle);
        Optional<UserAnalysis> cachedData = repository.findById(handle);

        if (cachedData.isPresent()) {
            UserAnalysis analysis = cachedData.get();
            if (analysis.getLastUpdated().isAfter(LocalDateTime.now().minusDays(1))) {
                log.info("CACHE HIT: Returning existing data for handle: {}", handle);
                return analysis;
            }
            log.info("CACHE STALE: Data for {} is older than 24 hours.", handle);
        } else {
            log.info("CACHE MISS: No existing data for handle: {}", handle);
        }

        log.info("EXTERNAL API: Fetching status from Codeforces for handle: {}", handle);
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

            log.info("DATABASE: Saving analysis for handle: {}", handle);
            UserAnalysis saved = repository.save(analysis);
            log.info("DATABASE: Successfully persisted handle: {}", handle);
            return saved;
        } catch (Exception e) {
            log.error("ERROR: Failed to fetch or save data for handle: {}. Message: {}", handle, e.getMessage());
            throw e;
        }
    }
}
