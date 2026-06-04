package com.cuong.backend.mapper;

import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.model.request.CreateLessonRequest;
import com.cuong.backend.model.request.UpdateLessonRequest;
import com.cuong.backend.model.response.CreateLessonResponse;
import com.cuong.backend.model.response.LessonResponseDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonName", source = "lessonName", qualifiedByName = "trim")
    @Mapping(target = "status", source = "status", defaultValue = "Đã xuất bản")
    LessonEntity toEntity(CreateLessonRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lessonName", source = "lessonName", qualifiedByName = "trim", conditionExpression = "java(request.getLessonName() != null && !request.getLessonName().isBlank())")
    void updateEntity(UpdateLessonRequest request, @MappingTarget LessonEntity lesson);

    @Mapping(target = "chapterName", source = "chapterName")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "grade", source = "grade")
    LessonResponseDTO toResponse(LessonEntity lesson, String chapterName, String subject, String grade);

    @Mapping(target = "id", source = "lesson.id")
    @Mapping(target = "lessonName", source = "lesson.lessonName")
    @Mapping(target = "videoUrl", source = "lesson.videoUrl")
    @Mapping(target = "pdfUrl", source = "lesson.pdfUrl")
    @Mapping(target = "chapterName", source = "chapterName")
    @Mapping(target = "message", source = "message")
    CreateLessonResponse toCreateResponse(LessonEntity lesson, String chapterName, String message);

    @Named("trim")
    default String trim(String value) {
        return value == null ? null : value.trim();
    }

}
