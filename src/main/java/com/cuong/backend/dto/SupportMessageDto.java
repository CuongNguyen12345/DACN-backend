package com.cuong.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SupportMessageDto {
    private long id;
    private long requestId;
    private long requestUserId;
    private long senderId;
    private String senderName;
    private String senderRole;
    private String requestType;
    private Long subjectId;
    private String subjectName;
    private String gradeLevel;
    private Long lessonId;
    private String lessonName;
    private String content;
    private Date createdAt;
}
