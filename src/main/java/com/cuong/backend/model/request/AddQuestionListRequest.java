package com.cuong.backend.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AddQuestionListRequest {
    private String subject;
    private String grade;
    private List<QuestionItemRequest> questions;

    @Data
    public static class QuestionItemRequest {
        private String content;
        private String explanation;
        private String level;
        private List<OptionItemRequest> options;
    }

    @Data
    public static class OptionItemRequest {
        private String content;
        
        @JsonProperty("isCorrect")
        private Boolean isCorrect;
    }
}
