package com.cuong.backend.service;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.model.dto.ExamDTO;
import com.cuong.backend.repository.ExamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    @Autowired
    private ExamRepository examRepository;

    public List<ExamDTO> getExamList(Long subjectId, Integer grade, String keyword) {
        List<ExamEntity> exams = examRepository.searchExam(subjectId, grade, keyword);
        return exams.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ExamDTO toDTO(ExamEntity e) {
        return new ExamDTO(
                e.getId(),
                e.getTitle(),
                e.getSubjectId(),
                e.getGrade());
    }
}
