package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "questions")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "subject_id")
    int subjectId;

    @Column(name = "topic_id")
    Integer topicId;

    @Column(columnDefinition = "TEXT", nullable = false)
    String content;

    @Column(name = "image_url", length = 255)
    String imageUrl;

    @Column(columnDefinition = "TEXT")
    String explanation;

    @Column(name = "level")
    String level; // 'EASY', 'MEDIUM', 'HARD'

    @Column(name = "status", length = 10)
    String status; // 'DRAFT', 'ACTIVE', 'HIDDEN'

    @Column(name = "created_at")
    @CreatedDate
    Date createdAt;

    @Column(name = "updated_at")
    Date updatedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    List<QuestionOptionEntity> options;
}
