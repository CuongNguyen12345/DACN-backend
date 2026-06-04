package com.cuong.backend.mapper;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.ExamResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subjectId", source = "subjectId")
    @Mapping(target = "attemptCount", constant = "0")
    @Mapping(target = "questionItems", ignore = true)
    ExamEntity toEntity(CreateExamRequest request, int subjectId);

    @Mapping(target = "id", source = "exam.id")
    @Mapping(target = "title", source = "exam.title")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "grade", source = "grade")
    @Mapping(target = "questionCount", source = "questionCount")
    @Mapping(target = "message", expression = "java(createMessage(questionCount))")
    CreateExamResponse toCreateResponse(ExamEntity exam, String subject, String grade, int questionCount);

    @Mapping(target = "id", source = "exam.id")
    @Mapping(target = "title", source = "exam.title")
    @Mapping(target = "duration", source = "exam.duration")
    @Mapping(target = "questionCount", source = "questionCount")
    @Mapping(target = "attemptCount", source = "exam.attemptCount")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "grade", source = "grade")
    ExamResponseDTO toResponse(ExamEntity exam, String subject, String grade, int questionCount);

    default String createMessage(int questionCount) {
        return "Tạo đề thi thành công với " + questionCount + " câu hỏi.";
    }
}
