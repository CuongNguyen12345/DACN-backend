package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "chapters")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChapterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "subject_id")
    int subjectId;

    @Column(name = "chapter_name", nullable = false)
    String chapterName;

    @Column(name = "order_number")
    int orderNumber;
}
