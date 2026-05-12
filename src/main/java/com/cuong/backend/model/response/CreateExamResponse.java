package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateExamResponse {
    private long id;
    private String title;
    private String subject;
    private String grade;
    private int questionCount;
    private String message;
}
