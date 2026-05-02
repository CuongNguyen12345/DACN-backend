package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "support_requests")
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SupportRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Column(name = "type", nullable = false)
    String type; // 'SYSTEM' or 'ACADEMIC'

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    SubjectEntity subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    LessonEntity lesson;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "status")
    String status = "OPEN"; // 'OPEN', 'CLOSED'

    @Column(name = "created_at")
    @CreatedDate
    Date createdAt;
}
