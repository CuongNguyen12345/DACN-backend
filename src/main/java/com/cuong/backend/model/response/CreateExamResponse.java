package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateExamResponse {
    private long id;
    private String title;
    private int questionCount;
    private String message;
}
