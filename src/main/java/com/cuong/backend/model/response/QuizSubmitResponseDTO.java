package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizSubmitResponseDTO {
    private int correct;
    private int total;
    private int scorePercent;
    private int passingScore;
    private boolean passed;
    private String difficulty;
    private double masteryGain;
    private double masteryScore;
}
