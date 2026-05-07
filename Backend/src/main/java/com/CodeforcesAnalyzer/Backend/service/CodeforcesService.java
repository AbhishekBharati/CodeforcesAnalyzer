package com.CodeforcesAnalyzer.Backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CodeforcesService {
    private final RestTemplate restTemplate;
    private final String CF_API_URL = "https://codeforces.com/api/user.status?handle=";

    public CodeforcesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Object getUserStatus(String handle) {
        String url = CF_API_URL + handle;
        return restTemplate.getForObject(url, Object.class);
    }
}
