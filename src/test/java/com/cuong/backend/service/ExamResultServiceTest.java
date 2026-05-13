package com.cuong.backend.service;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.entity.ExamQuestionItemEntity;
import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.ExamResultEntity;
import com.cuong.backend.model.request.ExamSubmitRequest;
import com.cuong.backend.model.response.ExamResultDetailResponse;
import com.cuong.backend.model.response.ExamResultSummaryResponse;
import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.ExamResultRepository;
import com.cuong.backend.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamResultServiceTest {

    @Test
    void submitExamSavesANewResultAttemptWithAnswerSnapshots() {
        ExamRepository examRepository = mock(ExamRepository.class);
        ExamResultRepository examResultRepository = mock(ExamResultRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ExamResultService service = new ExamResultService(
                examRepository,
                examResultRepository,
                subjectRepository
        );

        ExamEntity exam = createExam();
        when(examRepository.findById(10L)).thenReturn(Optional.of(exam));
        when(subjectRepository.findById(2)).thenReturn(Optional.of(createSubject()));
        when(examResultRepository.save(any(ExamResultEntity.class))).thenAnswer(invocation -> {
            ExamResultEntity saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ExamSubmitRequest request = new ExamSubmitRequest();
        request.setDurationSeconds(125);
        request.setAnswers(Map.of("101", "A", "102", "C"));

        ExamResultDetailResponse response = service.submitExam(10L, 7L, request);

        assertEquals(99L, response.getId());
        assertEquals(10L, response.getExamId());
        assertEquals("Kiểm tra Toán", response.getExamTitle());
        assertEquals("Toán học", response.getSubjectName());
        assertEquals(5.0, response.getScore());
        assertEquals(1, response.getCorrectCount());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(125, response.getDurationSeconds());
        assertEquals(2, response.getQuestions().size());
        assertEquals("A", response.getQuestions().get(0).getSelectedOptionLabel());
        assertEquals("A", response.getQuestions().get(0).getCorrectOptionLabel());
        assertTrue(response.getQuestions().get(0).isCorrect());
        assertEquals(2, response.getQuestions().get(0).getOptions().size());

        ArgumentCaptor<ExamResultEntity> captor = ArgumentCaptor.forClass(ExamResultEntity.class);
        verify(examResultRepository).save(captor.capture());
        ExamResultEntity saved = captor.getValue();
        assertEquals(7L, saved.getUserId());
        assertEquals(2, saved.getAnswers().size());
        assertTrue(saved.getAnswers().get(0).getOptionsJson().contains("\"label\":\"A\""));
        verify(examRepository).incrementAttemptCount(10L);
    }

    @Test
    void getHistoryFiltersBySubjectAndKeywordForTheCurrentUser() {
        ExamRepository examRepository = mock(ExamRepository.class);
        ExamResultRepository examResultRepository = mock(ExamResultRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ExamResultService service = new ExamResultService(
                examRepository,
                examResultRepository,
                subjectRepository
        );

        ExamResultEntity math = new ExamResultEntity();
        math.setId(1L);
        math.setUserId(7L);
        math.setExamTitle("Kiểm tra Toán");
        math.setSubjectName("Toán học");
        math.setScore(8.5);

        ExamResultEntity physics = new ExamResultEntity();
        physics.setId(2L);
        physics.setUserId(7L);
        physics.setExamTitle("Trắc nghiệm Vật Lý");
        physics.setSubjectName("Vật Lý");
        physics.setScore(6.0);

        when(examResultRepository.findByUserIdOrderBySubmittedAtDesc(7L))
                .thenReturn(List.of(math, physics));

        List<ExamResultSummaryResponse> result = service.getHistory(7L, "Toán học", "toán");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Kiểm tra Toán", result.get(0).getExamTitle());
        assertEquals("Toán học", result.get(0).getSubjectName());
    }

    @Test
    void getResultDetailRejectsResultsOwnedByAnotherUser() {
        ExamRepository examRepository = mock(ExamRepository.class);
        ExamResultRepository examResultRepository = mock(ExamResultRepository.class);
        SubjectRepository subjectRepository = mock(SubjectRepository.class);
        ExamResultService service = new ExamResultService(
                examRepository,
                examResultRepository,
                subjectRepository
        );

        when(examResultRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.getResultDetail(7L, 99L));
        assertTrue(error.getMessage().contains("Không tìm thấy"));
    }

    private static ExamEntity createExam() {
        ExamEntity exam = new ExamEntity();
        exam.setId(10L);
        exam.setSubjectId(2);
        exam.setTitle("Kiểm tra Toán");
        exam.setDuration(45);

        QuestionEntity first = createQuestion(101L, "1 + 1 = ?", "EASY", "Vì 1 + 1 = 2", true);
        QuestionEntity second = createQuestion(102L, "2 + 2 = ?", "EASY", "Vì 2 + 2 = 4", false);

        ExamQuestionItemEntity firstItem = new ExamQuestionItemEntity();
        firstItem.setExam(exam);
        firstItem.setQuestion(first);
        firstItem.setOrderNumber(1);

        ExamQuestionItemEntity secondItem = new ExamQuestionItemEntity();
        secondItem.setExam(exam);
        secondItem.setQuestion(second);
        secondItem.setOrderNumber(2);

        exam.setQuestionItems(List.of(firstItem, secondItem));
        return exam;
    }

    private static QuestionEntity createQuestion(long id, String content, String level, String explanation, boolean firstCorrect) {
        QuestionEntity question = new QuestionEntity();
        question.setId(id);
        question.setContent(content);
        question.setLevel(level);
        question.setExplanation(explanation);

        QuestionOptionEntity first = new QuestionOptionEntity();
        first.setQuestion(question);
        first.setContent(firstCorrect ? "2" : "3");
        first.setCorrect(firstCorrect);

        QuestionOptionEntity second = new QuestionOptionEntity();
        second.setQuestion(question);
        second.setContent(firstCorrect ? "3" : "4");
        second.setCorrect(!firstCorrect);

        question.setOptions(List.of(first, second));
        return question;
    }

    private static SubjectEntity createSubject() {
        SubjectEntity subject = new SubjectEntity();
        subject.setId(2);
        subject.setName("Toán học");
        subject.setGrade("10");
        return subject;
    }
}
