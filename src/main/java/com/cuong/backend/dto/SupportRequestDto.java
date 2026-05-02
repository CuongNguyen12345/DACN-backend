package com.cuong.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SupportRequestDto {
    private long id;
    private long userId;
    private String userName;
    private String avatar;
    private String type;
    private Long subjectId;
    private String subjectName;
    private Long lessonId;
    private String lessonName;
    private String gradeLevel;
    private String title;
    private String status;
    private Date createdAt;
}
