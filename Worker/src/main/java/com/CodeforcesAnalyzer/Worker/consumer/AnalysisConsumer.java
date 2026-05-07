package com.CodeforcesAnalyzer.Worker.consumer;

import com.CodeforcesAnalyzer.Worker.config.RabbitMQConfig;
import com.CodeforcesAnalyzer.Worker.model.UserAnalysis;
import com.CodeforcesAnalyzer.Worker.repository.UserAnalysisRepository;
import com.CodeforcesAnalyzer.Worker.service.AnalysisAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class AnalysisConsumer {

    private final UserAnalysisRepository repository;
    private final AnalysisAggregator aggregator;

    public AnalysisConsumer(UserAnalysisRepository repository, AnalysisAggregator aggregator) {
        this.repository = repository;
        this.aggregator = aggregator;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeMessage(String handle) {
        log.info("WORKER: Received analysis task for handle: {}", handle);
        
        Optional<UserAnalysis> analysisOpt = repository.findById(handle);
        if (analysisOpt.isEmpty()) {
            log.error("WORKER: No data found in MongoDB for handle: {}", handle);
            return;
        }

        UserAnalysis analysis = analysisOpt.get();
        log.info("WORKER: Successfully retrieved raw data for {}. Status: {}", handle, analysis.getStatus());

        analysis.setStatus("PROCESSING");
        repository.save(analysis);

        try {
            log.info("WORKER: Calculating detailed metrics for {}...", handle);
            Map<String, Object> metrics = aggregator.aggregateMetrics(analysis.getRawSubmissions());
            
            // For now, we store the raw metrics map as a JSON string in the recommendation field
            // to verify the structure before sending to AI.
            String metricsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(metrics);

            analysis.setAiRecommendation(metricsJson);
            analysis.setStatus("COMPLETED");
            repository.save(analysis);
            log.info("WORKER: Completed detailed analysis for handle: {}", handle);

        } catch (Exception e) {
            log.error("WORKER: Error during analysis for {}: {}", handle, e.getMessage());
            analysis.setStatus("ERROR");
            repository.save(analysis);
        }
    }
}
