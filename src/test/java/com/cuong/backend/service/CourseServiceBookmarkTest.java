package com.cuong.backend.service;

import com.cuong.backend.entity.ChapterEntity;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.UserProgressEntity;
import com.cuong.backend.model.response.BookmarkedLessonResponseDTO;
import com.cuong.backend.repository.ChapterRepository;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceBookmarkTest {

    @Test
    void setLessonBookmarkedCreatesProgressWhenMissing() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        UserProgressRepository userProgressRepository = mock(UserProgressRepository.class);
        StudyActivityService studyActivityService = mock(StudyActivityService.class);
        CourseService service = new CourseService(
                subjectRepository,
                chapterRepository,
                lessonRepository,
                userProgressRepository,
                studyActivityService
        );

        when(userProgressRepository.findFirstByUserIdAndLessonIdOrderByIdAsc(7L, 11)).thenReturn(Optional.empty());
        when(lessonRepository.existsById(11)).thenReturn(true);

        service.setLessonBookmarked(7L, 11, true);

        ArgumentCaptor<UserProgressEntity> captor = ArgumentCaptor.forClass(UserProgressEntity.class);
        verify(userProgressRepository).save(captor.capture());
        UserProgressEntity saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals(11, saved.getLessonId());
        assertTrue(saved.isBookmarked());
        assertNotNull(saved.getBookmarkedAt());
    }

    @Test
    void getBookmarkedLessonsReturnsSavedLessonsWithMetadata() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        UserProgressRepository userProgressRepository = mock(UserProgressRepository.class);
        StudyActivityService studyActivityService = mock(StudyActivityService.class);
        CourseService service = new CourseService(
                subjectRepository,
                chapterRepository,
                lessonRepository,
                userProgressRepository,
                studyActivityService
        );

        UserProgressEntity progress = new UserProgressEntity();
        progress.setUserId(7L);
        progress.setLessonId(11);
        progress.setBookmarked(true);
        progress.setBookmarkedAt(new Date(1000));
        progress.setLastWatchedTime(125);

        LessonEntity lesson = new LessonEntity();
        lesson.setId(11);
        lesson.setChapterId(3);
        lesson.setLessonName("Bai 1: Menh de");
        lesson.setVideoUrl("abc123");
        lesson.setPdfUrl("lesson.pdf");

        ChapterEntity chapter = new ChapterEntity();
        chapter.setId(3);
        chapter.setSubjectId(2);
        chapter.setChapterName("Chuong 1");

        SubjectEntity subject = new SubjectEntity();
        subject.setId(2);
        subject.setName("Toan hoc");
        subject.setGrade("12");

        when(userProgressRepository.findBookmarkedByUserIdOrderByBookmarkedAtDesc(7L))
                .thenReturn(List.of(progress));
        when(lessonRepository.findById(11)).thenReturn(Optional.of(lesson));
        when(chapterRepository.findById(3)).thenReturn(Optional.of(chapter));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(subject));

        List<BookmarkedLessonResponseDTO> result = service.getBookmarkedLessons(7L);

        assertEquals(1, result.size());
        BookmarkedLessonResponseDTO savedLesson = result.get(0);
        assertEquals(11, savedLesson.getId());
        assertEquals("Bai 1: Menh de", savedLesson.getLessonName());
        assertEquals("Chuong 1", savedLesson.getChapterName());
        assertEquals("Toan hoc", savedLesson.getSubjectName());
        assertEquals("12", savedLesson.getGradeLevel());
        assertEquals("Toan hoc 12", savedLesson.getSubjectBadge());
        assertEquals("abc123", savedLesson.getVideoUrl());
        assertEquals("lesson.pdf", savedLesson.getPdfUrl());
        assertEquals(125, savedLesson.getLastWatchedTime());
        assertEquals(new Date(1000), savedLesson.getBookmarkedAt());
    }

    @Test
    void setLessonBookmarkedUsesExistingProgressWithoutRequiringUniqueRows() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        UserProgressRepository userProgressRepository = mock(UserProgressRepository.class);
        StudyActivityService studyActivityService = mock(StudyActivityService.class);
        CourseService service = new CourseService(
                subjectRepository,
                chapterRepository,
                lessonRepository,
                userProgressRepository,
                studyActivityService
        );

        UserProgressEntity existingProgress = new UserProgressEntity();
        existingProgress.setUserId(7L);
        existingProgress.setLessonId(11);

        when(userProgressRepository.findFirstByUserIdAndLessonIdOrderByIdAsc(7L, 11))
                .thenReturn(Optional.of(existingProgress));

        service.setLessonBookmarked(7L, 11, true);

        verify(lessonRepository, never()).existsById(11);
        verify(userProgressRepository).save(existingProgress);
        assertTrue(existingProgress.isBookmarked());
        assertNotNull(existingProgress.getBookmarkedAt());
    }

    @Test
    void isLessonBookmarkedTreatsNullBookmarkAsNotSaved() {
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ChapterRepository chapterRepository = mock(ChapterRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        UserProgressRepository userProgressRepository = mock(UserProgressRepository.class);
        StudyActivityService studyActivityService = mock(StudyActivityService.class);
        CourseService service = new CourseService(
                subjectRepository,
                chapterRepository,
                lessonRepository,
                userProgressRepository,
                studyActivityService
        );

        UserProgressEntity existingProgress = new UserProgressEntity();
        existingProgress.setUserId(7L);
        existingProgress.setLessonId(11);
        existingProgress.setBookmarked(null);

        when(userProgressRepository.findFirstByUserIdAndLessonIdOrderByIdAsc(7L, 11))
                .thenReturn(Optional.of(existingProgress));

        assertFalse(service.isLessonBookmarked(7L, 11));
    }
}
