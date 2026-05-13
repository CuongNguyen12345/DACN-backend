package com.cuong.backend.model.request;

import lombok.Data;

import java.util.List;

@Data
public class QuizRequest {
    private String title;
    private String subject;
    private String grade;
    private String topicName;
    private Integer lessonId;
    private int duration;
    private int passingScore;
    private List<String> questionIds;
}
