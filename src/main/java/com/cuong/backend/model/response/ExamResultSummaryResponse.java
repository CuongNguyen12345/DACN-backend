package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ExamResultSummaryResponse {
    private long id;
    private long examId;
    private String examTitle;
    private String subjectName;
    private double score;
    private int correctCount;
    private int totalQuestions;
    private int durationSeconds;
    private Date submittedAt;
}
