package com.cuong.backend.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class QuestionDetailResponseDTO {
    private String id;
    private String content;
    private String subject;
    private String level;
    private String type;
    private String status;
    private String topicName;
    private Integer topicId;
    private Date createdAt;
    private String explanation;
    private List<OptionDTO> options;

    @Data
    @Builder
    public static class OptionDTO {
        private String id;
        private String text;
        
        @JsonProperty("isCorrect")
        private Boolean isCorrect;
    }
}
