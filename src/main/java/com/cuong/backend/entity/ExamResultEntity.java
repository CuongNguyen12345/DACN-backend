package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "exam_results")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "user_id", nullable = false)
    long userId;

    @Column(name = "exam_id", nullable = false)
    long examId;

    @Column(name = "exam_title", nullable = false)
    String examTitle;

    @Column(name = "subject_name")
    String subjectName;

    @Column(name = "score")
    double score;

    @Column(name = "correct_count")
    int correctCount;

    @Column(name = "total_questions")
    int totalQuestions;

    @Column(name = "duration_seconds")
    int durationSeconds;

    @Column(name = "submitted_at")
    @CreatedDate
    Date submittedAt;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExamResultAnswerEntity> answers = new ArrayList<>();
}
