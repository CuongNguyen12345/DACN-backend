package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "lessons")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "chapter_id")
    int chapterId;

    @Column(name = "lesson_name", nullable = false)
    String lessonName;

    @Column(columnDefinition = "TEXT")
    String content;

    @Column(name = "video_url")
    String videoUrl;

    @Column(name = "pdf_url")
    String pdfUrl;

    @Column(name = "duration")
    String duration;

    @Column(name = "status")
    String status;

    @Column(name = "type")
    String type;
}
