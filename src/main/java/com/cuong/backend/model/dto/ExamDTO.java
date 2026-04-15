package com.cuong.backend.model.dto;

public class ExamDTO {
    private Long id;
    private String title;
    private Long subjectId;
    private Integer grade;

    public ExamDTO() {
    }

    public ExamDTO(Long id, String title, Long subjectId, Integer grade) {
        this.id = id;
        this.title = title;
        this.subjectId = subjectId;
        this.grade = grade;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public Integer getGrade() {
        return grade;
    }
}