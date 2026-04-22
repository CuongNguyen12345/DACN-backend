package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class QuestionResponseDTO {
    private String id;
    private String content;
    private String subject;
    private String level;
    private String type;
    private String status;
    private String topicName;
    private Date createdAt;
}
