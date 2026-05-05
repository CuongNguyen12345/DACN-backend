package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AssessmentQuestionsResponse {
    private String subject;
    private String grade;
    private String targetScore;
    private int totalQuestions;
    private List<AssessmentQuestionDTO> questions;

    @Data
    @Builder
    public static class AssessmentQuestionDTO {
        private long id;
        private int orderNumber;
        private String content;
        private String level;
        private String topicName;
        private String explanation;
        private List<OptionDTO> options;
    }

    @Data
    @Builder
    public static class OptionDTO {
        private String label;
        private String content;
        private boolean correct;
    }
}
