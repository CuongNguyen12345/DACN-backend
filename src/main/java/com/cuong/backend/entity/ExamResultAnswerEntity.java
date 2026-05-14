package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "exam_result_answers")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamResultAnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    ExamResultEntity result;

    @Column(name = "question_id", nullable = false)
    long questionId;

    @Column(name = "order_number")
    int orderNumber;

    @Column(name = "question_content", columnDefinition = "TEXT")
    String questionContent;

    @Column(name = "level")
    String level;

    @Column(name = "explanation", columnDefinition = "TEXT")
    String explanation;

    @Column(name = "selected_option_label")
    String selectedOptionLabel;

    @Column(name = "selected_option_content", columnDefinition = "TEXT")
    String selectedOptionContent;

    @Column(name = "correct_option_label")
    String correctOptionLabel;

    @Column(name = "correct_option_content", columnDefinition = "TEXT")
    String correctOptionContent;

    @Column(name = "is_correct")
    boolean correct;

    @Column(name = "options_json", columnDefinition = "TEXT")
    String optionsJson;
}
