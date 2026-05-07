package com.CodeforcesAnalyzer.Backend.controller;

import com.CodeforcesAnalyzer.Backend.service.CodeforcesService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cf")
@CrossOrigin(origins = "*") // For development, allow all origins
public class CodeforcesController {

    private final CodeforcesService codeforcesService;

    public CodeforcesController(CodeforcesService codeforcesService) {
        this.codeforcesService = codeforcesService;
    }

    @GetMapping("/user-status")
    public Object getUserStatus(@RequestParam String handle) {
        return codeforcesService.getUserStatus(handle);
    }
}
