package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "exams")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "subject_id")
    int subjectId;

    @Column(nullable = false)
    String title;

    @Column(name = "duration")
    int duration;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ExamQuestionItemEntity> questionItems = new ArrayList<>();
}
