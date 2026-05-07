package com.CodeforcesAnalyzer.Worker.consumer;

import com.CodeforcesAnalyzer.Worker.config.RabbitMQConfig;
import com.CodeforcesAnalyzer.Worker.model.UserAnalysis;
import com.CodeforcesAnalyzer.Worker.repository.UserAnalysisRepository;
import com.CodeforcesAnalyzer.Worker.service.AnalysisAggregator;
import com.CodeforcesAnalyzer.Worker.service.OpenAIService;
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
    private final OpenAIService aiService;

    public AnalysisConsumer(UserAnalysisRepository repository, AnalysisAggregator aggregator, OpenAIService aiService) {
        this.repository = repository;
        this.aggregator = aggregator;
        this.aiService = aiService;
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
            
            // Save metrics to DB so frontend can show a dashboard
            analysis.setTopicMetrics(metrics);
            repository.save(analysis);

            log.info("WORKER: Calling OpenAI for {}...", handle);
            String aiRoadmap = aiService.generateRoadmap(handle, metrics);

            analysis.setAiRecommendation(aiRoadmap);
            analysis.setStatus("COMPLETED");
            repository.save(analysis);
            log.info("WORKER: Completed analysis with AI for handle: {}", handle);

        } catch (Exception e) {
            log.error("WORKER: Error during analysis for {}: {}", handle, e.getMessage());
            analysis.setStatus("ERROR");
            repository.save(analysis);
        }
    }
}
