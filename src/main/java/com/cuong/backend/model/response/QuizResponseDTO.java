package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizResponseDTO {
    private long id;
    private String title;
    private Integer lessonId;
    private String lessonTitle;
    private Integer topicId;
    private String topicName;
    private String subject;
    private String grade;
    private int questionCount;
    private int duration;
    private int passingScore;
    private String createdAt;
    private String updatedAt;
}
