package com.cuong.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopStudentItem {
    private long id;
    private String name;
    private String grade;
    private double score;
    private long exams;
}
