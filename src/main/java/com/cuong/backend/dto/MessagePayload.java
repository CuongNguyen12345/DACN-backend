package com.cuong.backend.dto;

import lombok.Data;

@Data
public class MessagePayload {
    private long senderId;
    private Long requestId; // Null if creating a new request
    private String type; // 'SYSTEM' or 'ACADEMIC'
    private Long subjectId;
    private Long lessonId;
    private String title;
    private String content;
}
