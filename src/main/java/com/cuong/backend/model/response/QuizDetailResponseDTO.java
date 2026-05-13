package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class QuizDetailResponseDTO {
    private long id;
    private String title;
    private Integer lessonId;
    private String lessonTitle;
    private Integer topicId;
    private String topicName;
    private String subject;
    private String grade;
    private int duration;
    private int passingScore;
    private List<QuestionItem> questions;

    @Data
    @Builder
    public static class QuestionItem {
        private long id;
        private int orderNumber;
        private String content;
        private String level;
        private String explanation;
        private String topicName;
        private List<OptionItem> options;
    }

    @Data
    @Builder
    public static class OptionItem {
        private String label;
        private String content;
        private boolean correct;
    }
}
