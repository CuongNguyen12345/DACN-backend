package com.cuong.backend.model.request;

import lombok.Data;
import java.util.List;

@Data
public class AssessmentAnalyzeRequest {
    private String subject;
    private String grade;
    private String targetScore;
    private int totalQuestions;
    private int correctCount;
    private double score;
    private List<WrongQuestionDTO> wrongQuestions;

    @Data
    public static class WrongQuestionDTO {
        private long questionId;
        private String content;
        private String topicName;
        private String level;
        private String userAnswer;
        private String correctAnswer;
    }
}
