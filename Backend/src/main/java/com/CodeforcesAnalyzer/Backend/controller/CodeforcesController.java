package com.CodeforcesAnalyzer.Backend.controller;

import com.CodeforcesAnalyzer.Backend.model.UserAnalysis;
import com.CodeforcesAnalyzer.Backend.service.CodeforcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cf")
@CrossOrigin(origins = "*")
public class CodeforcesController {

    private static final Logger log = LoggerFactory.getLogger(CodeforcesController.class);
    private final CodeforcesService codeforcesService;

    public CodeforcesController(CodeforcesService codeforcesService) {
        this.codeforcesService = codeforcesService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<UserAnalysis> startAnalysis(@RequestParam String handle) {
        log.info("REST: Received analysis request for handle: {}", handle);
        UserAnalysis result = codeforcesService.processUserAnalysisRequest(handle);
        
        if ("COMPLETED".equals(result.getStatus())) {
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }

    @GetMapping("/status/{handle}")
    public ResponseEntity<UserAnalysis> getStatus(@PathVariable String handle) {
        log.info("REST: Checking status for handle: {}", handle);
        // We can reuse the same service logic or add a dedicated light-weight check
        UserAnalysis result = codeforcesService.processUserAnalysisRequest(handle);
        return ResponseEntity.ok(result);
    }
}
