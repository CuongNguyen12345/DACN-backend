package com.cuong.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    // Admin search fields
    private int chapterId;
    private String chapterName;
    private String subject;
    private String grade;
    private String duration;
    private String status;
    private String type;
}
