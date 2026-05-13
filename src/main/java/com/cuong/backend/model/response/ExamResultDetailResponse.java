package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class ExamResultDetailResponse {
    private long id;
    private long examId;
    private String examTitle;
    private String subjectName;
    private double score;
    private int correctCount;
    private int totalQuestions;
    private int durationSeconds;
    private Date submittedAt;
    private List<QuestionResult> questions;

    @Data
    @Builder
    public static class QuestionResult {
        private long id;
        private int orderNumber;
        private String content;
        private String level;
        private String explanation;
        private String selectedOptionLabel;
        private String selectedOptionContent;
        private String correctOptionLabel;
        private String correctOptionContent;
        private boolean correct;
        private List<OptionResult> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionResult {
        private String label;
        private String content;
        private boolean correct;
    }
}
