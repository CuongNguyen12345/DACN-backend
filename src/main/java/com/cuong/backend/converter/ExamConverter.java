package com.cuong.backend.converter;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.model.dto.ExamDTO;
import org.springframework.stereotype.Component;

@Component
public class ExamConverter {
    public ExamDTO toDTO(ExamEntity entity) {
        ExamDTO dto = new ExamDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSubject(entity.getSubject());
        dto.setGrade(entity.getGrade());
        return dto;
    }
}
