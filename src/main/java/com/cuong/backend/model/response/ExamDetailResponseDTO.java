package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExamDetailResponseDTO {
    private long id;
    private String title;
    private String subject;
    private int duration;
    private List<QuestionItem> questions;

    @Data
    @Builder
    public static class QuestionItem {
        private long id;
        private int orderNumber;
        private String content;
        private String level;
        private String explanation;
        private List<OptionItem> options;
    }

    @Data
    @Builder
    public static class OptionItem {
        private String label;   // A, B, C, D
        private String content;
        private boolean correct;
    }
}
