package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(
        name = "study_activity",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "study_date"})
)
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudyActivityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "user_id", nullable = false)
    long userId;

    @Column(name = "study_date", nullable = false)
    LocalDate studyDate;

    @Column(name = "source", nullable = false)
    String source;

    @Column(name = "created_at")
    @CreatedDate
    Date createdAt;
}
