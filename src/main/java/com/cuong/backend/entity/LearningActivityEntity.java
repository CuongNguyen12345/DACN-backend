package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
    name = "learning_activity",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"})
)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LearningActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "activity_date", nullable = false)
    LocalDate activityDate;
}
