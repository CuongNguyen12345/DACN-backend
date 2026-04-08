package com.cuong.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Entity
@Table(name = "subjects")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "name", length = 150)
    String name;

    @Column(name = "grade", length = 50)
    String grade;
}
