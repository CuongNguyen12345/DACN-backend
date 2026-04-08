package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LessonResponseDTO {
    private int id;
    private String lessonName;
    private String subjectBadge;
    private String videoUrl;
    private String pdfUrl;
}
