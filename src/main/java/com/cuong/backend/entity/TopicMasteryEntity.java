package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "topic_mastery",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "topic_id"}))
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopicMasteryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "user_id", nullable = false)
    long userId;

    @Column(name = "topic_id", nullable = false)
    int topicId;

    /** 0.0 – 1.0: điểm thành thạo của user tại topic này */
    @Column(name = "mastery_score", nullable = false)
    double masteryScore;

    @Column(name = "updated_at")
    @LastModifiedDate
    Date updatedAt;
}
