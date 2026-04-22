package com.cuong.backend.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class UpdateQuestionRequest {
    private String content;
    private String explanation;
    private String level;
    private String subject;
    private String grade;
    private String topicName;
    private List<OptionItemRequest> options;

    @Data
    public static class OptionItemRequest {
        private String content;
        
        @JsonProperty("isCorrect")
        private Boolean isCorrect;
    }
}
