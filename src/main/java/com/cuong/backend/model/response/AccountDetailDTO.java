package com.cuong.backend.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountDetailDTO {
    long id;
    String userName;
    String email;
    String role;
    String unit;
    String phoneNumber;
    String grade;
    String status;
    Date createdDate;

    // Stats
    int totalExams;
    double avgScore;
    int completedLessons;

    // Exam results
    List<ExamRecord> examRecords;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ExamRecord {
        long examId;
        String title;
        String subject;
        double score;
        int totalQuestions;
        int correctAnswers;
        Date submittedAt;
    }
}
