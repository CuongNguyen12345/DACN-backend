package com.cuong.backend.mapper;

import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.model.request.CreateLessonRequest;
import com.cuong.backend.model.request.UpdateLessonRequest;
import com.cuong.backend.model.response.LessonResponseDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LessonMapperTest {

    private final LessonMapper mapper = Mappers.getMapper(LessonMapper.class);

    @Test
    void mapsCreateLessonRequestToEntityAndAppliesDefaults() {
        CreateLessonRequest request = new CreateLessonRequest();
        request.setChapterId(3);
        request.setLessonName("  Dao ham  ");
        request.setContent("Noi dung");
        request.setVideoUrl("video-url");
        request.setPdfUrl("pdf-url");
        request.setDuration("45 phut");
        request.setStatus(null);
        request.setType("Video");

        LessonEntity entity = mapper.toEntity(request);

        assertEquals(3, entity.getChapterId());
        assertEquals("Dao ham", entity.getLessonName());
        assertEquals("Noi dung", entity.getContent());
        assertEquals("video-url", entity.getVideoUrl());
        assertEquals("pdf-url", entity.getPdfUrl());
        assertEquals("45 phut", entity.getDuration());
        assertEquals("Đã xuất bản", entity.getStatus());
        assertEquals("Video", entity.getType());
    }

    @Test
    void mapsLessonEntityToResponseWithContextFields() {
        LessonEntity lesson = new LessonEntity();
        lesson.setId(10);
        lesson.setChapterId(4);
        lesson.setLessonName("Gioi han");
        lesson.setContent("Ly thuyet");
        lesson.setVideoUrl("video");
        lesson.setPdfUrl("pdf");
        lesson.setDuration("30 phut");
        lesson.setStatus("Bản nháp");
        lesson.setType("Lý thuyết");

        LessonResponseDTO response = mapper.toResponse(lesson, "Chuong 1", "Toan", "10");

        assertEquals(10, response.getId());
        assertEquals(4, response.getChapterId());
        assertEquals("Gioi han", response.getLessonName());
        assertEquals("Chuong 1", response.getChapterName());
        assertEquals("Toan", response.getSubject());
        assertEquals("10", response.getGrade());
    }

    @Test
    void updateSkipsNullValuesAndTrimsLessonName() {
        LessonEntity lesson = new LessonEntity();
        lesson.setLessonName("Old name");
        lesson.setContent("Old content");
        lesson.setVideoUrl("old-video");

        UpdateLessonRequest request = new UpdateLessonRequest();
        request.setLessonName("  New name  ");
        request.setContent(null);
        request.setVideoUrl(null);

        mapper.updateEntity(request, lesson);

        assertEquals("New name", lesson.getLessonName());
        assertEquals("Old content", lesson.getContent());
        assertEquals("old-video", lesson.getVideoUrl());
        assertNull(request.getChapterId());
    }
}
