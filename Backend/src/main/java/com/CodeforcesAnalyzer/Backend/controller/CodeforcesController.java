package com.CodeforcesAnalyzer.Backend.controller;

import com.CodeforcesAnalyzer.Backend.model.UserAnalysis;
import com.CodeforcesAnalyzer.Backend.service.CodeforcesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping("/user-status")
    public UserAnalysis getUserStatus(@RequestParam String handle) {
        long startTime = System.currentTimeMillis();
        log.info("REST: Received request for handle: {}", handle);
        
        UserAnalysis result = codeforcesService.getUserStatus(handle);
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("REST: Completed request for handle: {} in {}ms", handle, duration);
        return result;
    }
}
