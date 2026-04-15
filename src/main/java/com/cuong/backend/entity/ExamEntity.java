package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "exams")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamEntity {

    public ExamEntity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    @Column(name = "subject_id")
    Long subjectId;

    Integer grade;
}