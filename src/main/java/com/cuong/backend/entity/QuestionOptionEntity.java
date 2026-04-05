package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "question_options")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionOptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    QuestionEntity question;

    @Column(columnDefinition = "TEXT", nullable = false)
    String content;

    @Column(name = "is_correct")
    boolean isCorrect; // Tinyint(1) mapping
}
