package com.cuong.backend.service;

import com.cuong.backend.converter.ExamConverter;
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

    @Autowired
    private ExamConverter examConverter;


    public List<ExamDTO> getExamList(String subject, Integer grade, String keyword) {
        List<ExamEntity> exams = examRepository.searchExam(subject, grade, keyword);
        return exams.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ExamDTO toDTO(ExamEntity e) {
        ExamDTO dto = examConverter.toDTO(e);
        return dto;
    }
}
