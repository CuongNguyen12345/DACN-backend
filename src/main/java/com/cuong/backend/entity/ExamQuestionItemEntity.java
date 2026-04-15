package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "exam_question_items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamQuestionItemEntity {

    @EmbeddedId
    ExamQuestionItemId id = new ExamQuestionItemId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examId")
    @JoinColumn(name = "exam_id")
    ExamEntity exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    QuestionEntity question;

    @Column(name = "order_number")
    int orderNumber;
}
