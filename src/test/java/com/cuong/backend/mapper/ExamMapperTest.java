package com.cuong.backend.mapper;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.ExamResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExamMapperTest {

    private final ExamMapper mapper = Mappers.getMapper(ExamMapper.class);

    @Test
    void mapsCreateExamRequestToEntityWithSubjectAndAttemptDefaults() {
        CreateExamRequest request = new CreateExamRequest();
        request.setTitle("Kiem tra giua ky");
        request.setDuration(45);
        request.setDescription("Mo ta");
        request.setTotalQuestions(20);

        ExamEntity entity = mapper.toEntity(request, 5);

        assertEquals("Kiem tra giua ky", entity.getTitle());
        assertEquals(5, entity.getSubjectId());
        assertEquals(45, entity.getDuration());
        assertEquals("Mo ta", entity.getDescription());
        assertEquals(20, entity.getTotalQuestions());
        assertEquals(0, entity.getAttemptCount());
    }

    @Test
    void mapsCreateExamResponseWithDisplayFields() {
        ExamEntity exam = new ExamEntity();
        exam.setId(9);
        exam.setTitle("De so 1");

        CreateExamResponse response = mapper.toCreateResponse(exam, "Toan", "Lop 10", 12);

        assertEquals(9, response.getId());
        assertEquals("De so 1", response.getTitle());
        assertEquals("Toan", response.getSubject());
        assertEquals("Lop 10", response.getGrade());
        assertEquals(12, response.getQuestionCount());
        assertEquals("Tạo đề thi thành công với 12 câu hỏi.", response.getMessage());
    }

    @Test
    void mapsExamListResponseWithDisplayFields() {
        ExamEntity exam = new ExamEntity();
        exam.setId(7);
        exam.setTitle("De cuoi ky");
        exam.setDuration(60);
        exam.setTotalQuestions(30);
        exam.setAttemptCount(4);

        ExamResponseDTO response = mapper.toResponse(exam, "Vat Ly", "11", 30);

        assertEquals(7, response.getId());
        assertEquals("De cuoi ky", response.getTitle());
        assertEquals("Vat Ly", response.getSubject());
        assertEquals("11", response.getGrade());
        assertEquals(60, response.getDuration());
        assertEquals(30, response.getQuestionCount());
        assertEquals(4, response.getAttemptCount());
    }
}
