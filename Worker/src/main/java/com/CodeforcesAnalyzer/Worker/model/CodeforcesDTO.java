package com.CodeforcesAnalyzer.Worker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CodeforcesDTO {
    private String status;
    private List<Submission> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Submission {
        private Long id;
        private Long creationTimeSeconds;
        private Integer relativeTimeSeconds;
        private Problem problem;
        private String verdict; // OK, WRONG_ANSWER, etc.
        private String programmingLanguage;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Problem {
        private Integer contestId;
        private String index;
        private String name;
        private Integer rating;
        private List<String> tags;
    }
}
