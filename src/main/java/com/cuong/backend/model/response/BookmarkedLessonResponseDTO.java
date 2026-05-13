package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BookmarkedLessonResponseDTO {
    private int id;
    private String lessonName;
    private String chapterName;
    private String subjectName;
    private String gradeLevel;
    private String subjectBadge;
    private String videoUrl;
    private String pdfUrl;
    private Integer lastWatchedTime;
    private Date bookmarkedAt;
}
