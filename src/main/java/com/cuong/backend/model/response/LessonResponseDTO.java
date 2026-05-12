package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonResponseDTO {
    private int id;
    private String lessonName;
    private Long subjectId;
    private String subjectName;
    private String gradeLevel;
    private String subjectBadge;
    private String videoUrl;
    private String pdfUrl;
    private String content;
}
