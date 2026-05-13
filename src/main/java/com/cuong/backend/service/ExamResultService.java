package com.cuong.backend.service;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.entity.ExamQuestionItemEntity;
import com.cuong.backend.entity.ExamResultAnswerEntity;
import com.cuong.backend.entity.ExamResultEntity;
import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.model.request.ExamSubmitRequest;
import com.cuong.backend.model.response.ExamResultDetailResponse;
import com.cuong.backend.model.response.ExamResultSummaryResponse;
import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.ExamResultRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.util.FormatUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ExamResultService {
    private static final String[] OPTION_LABELS = {"A", "B", "C", "D", "E", "F"};

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final SubjectRepository subjectRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExamResultService(
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            SubjectRepository subjectRepository) {
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public ExamResultDetailResponse submitExam(Long examId, long userId, ExamSubmitRequest request) {
        ExamEntity exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + examId));

        String subjectName = resolveSubjectName(exam.getSubjectId());
        Map<String, String> answers = request.getAnswers() == null ? Map.of() : request.getAnswers();
        List<ExamQuestionItemEntity> questionItems = exam.getQuestionItems().stream()
                .sorted(Comparator.comparingInt(ExamQuestionItemEntity::getOrderNumber))
                .toList();

        ExamResultEntity result = new ExamResultEntity();
        result.setUserId(userId);
        result.setExamId(exam.getId());
        result.setExamTitle(exam.getTitle());
        result.setSubjectName(subjectName);
        result.setTotalQuestions(questionItems.size());
        result.setDurationSeconds(Math.max(0, request.getDurationSeconds()));
        result.setSubmittedAt(new Date());

        int correctCount = 0;
        List<ExamResultAnswerEntity> savedAnswers = new ArrayList<>();
        for (ExamQuestionItemEntity item : questionItems) {
            QuestionEntity question = item.getQuestion();
            AnswerSnapshot snapshot = buildAnswerSnapshot(question, answers.get(String.valueOf(question.getId())));
            if (snapshot.correct()) {
                correctCount++;
            }

            ExamResultAnswerEntity answer = new ExamResultAnswerEntity();
            answer.setResult(result);
            answer.setQuestionId(question.getId());
            answer.setOrderNumber(item.getOrderNumber());
            answer.setQuestionContent(question.getContent());
            answer.setLevel(FormatUtil.mapLevelToFe(question.getLevel()));
            answer.setExplanation(question.getExplanation());
            answer.setSelectedOptionLabel(snapshot.selectedOptionLabel());
            answer.setSelectedOptionContent(snapshot.selectedOptionContent());
            answer.setCorrectOptionLabel(snapshot.correctOptionLabel());
            answer.setCorrectOptionContent(snapshot.correctOptionContent());
            answer.setCorrect(snapshot.correct());
            answer.setOptionsJson(writeOptionsJson(snapshot.options()));
            savedAnswers.add(answer);
        }

        result.setCorrectCount(correctCount);
        result.setScore(questionItems.isEmpty()
                ? 0
                : Math.round((correctCount * 10.0 / questionItems.size()) * 10.0) / 10.0);
        result.setAnswers(savedAnswers);

        ExamResultEntity saved = examResultRepository.save(result);
        examRepository.incrementAttemptCount(examId);
        return toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ExamResultSummaryResponse> getHistory(long userId, String subject, String keyword) {
        String normalizedSubject = normalize(subject);
        String normalizedKeyword = normalize(keyword);

        return examResultRepository.findByUserIdOrderBySubmittedAtDesc(userId).stream()
                .filter(result -> normalizedSubject == null || normalize(result.getSubjectName()).equals(normalizedSubject))
                .filter(result -> normalizedKeyword == null || normalize(result.getExamTitle()).contains(normalizedKeyword))
                .map(this::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExamResultDetailResponse getResultDetail(long userId, long resultId) {
        ExamResultEntity result = examResultRepository.findByIdAndUserId(resultId, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả bài thi."));
        return toDetailResponse(result);
    }

    private AnswerSnapshot buildAnswerSnapshot(QuestionEntity question, String selectedLabel) {
        List<ExamResultDetailResponse.OptionResult> options = new ArrayList<>();
        String selectedContent = null;
        String correctLabel = null;
        String correctContent = null;

        int index = 0;
        for (QuestionOptionEntity option : question.getOptions()) {
            String label = index < OPTION_LABELS.length ? OPTION_LABELS[index] : String.valueOf(index);
            options.add(ExamResultDetailResponse.OptionResult.builder()
                    .label(label)
                    .content(option.getContent())
                    .correct(option.isCorrect())
                    .build());

            if (label.equals(selectedLabel)) {
                selectedContent = option.getContent();
            }
            if (option.isCorrect()) {
                correctLabel = label;
                correctContent = option.getContent();
            }
            index++;
        }

        boolean isCorrect = selectedLabel != null && selectedLabel.equals(correctLabel);
        return new AnswerSnapshot(
                selectedLabel,
                selectedContent,
                correctLabel,
                correctContent,
                isCorrect,
                options
        );
    }

    private String resolveSubjectName(int subjectId) {
        return subjectRepository.findById(subjectId)
                .map(SubjectEntity::getName)
                .map(FormatUtil::mapSubjectToFe)
                .orElse("Khác");
    }

    private ExamResultSummaryResponse toSummaryResponse(ExamResultEntity result) {
        return ExamResultSummaryResponse.builder()
                .id(result.getId())
                .examId(result.getExamId())
                .examTitle(result.getExamTitle())
                .subjectName(result.getSubjectName())
                .score(result.getScore())
                .correctCount(result.getCorrectCount())
                .totalQuestions(result.getTotalQuestions())
                .durationSeconds(result.getDurationSeconds())
                .submittedAt(result.getSubmittedAt())
                .build();
    }

    private ExamResultDetailResponse toDetailResponse(ExamResultEntity result) {
        return ExamResultDetailResponse.builder()
                .id(result.getId())
                .examId(result.getExamId())
                .examTitle(result.getExamTitle())
                .subjectName(result.getSubjectName())
                .score(result.getScore())
                .correctCount(result.getCorrectCount())
                .totalQuestions(result.getTotalQuestions())
                .durationSeconds(result.getDurationSeconds())
                .submittedAt(result.getSubmittedAt())
                .questions(result.getAnswers().stream()
                        .sorted(Comparator.comparingInt(ExamResultAnswerEntity::getOrderNumber))
                        .map(this::toQuestionResult)
                        .toList())
                .build();
    }

    private ExamResultDetailResponse.QuestionResult toQuestionResult(ExamResultAnswerEntity answer) {
        return ExamResultDetailResponse.QuestionResult.builder()
                .id(answer.getQuestionId())
                .orderNumber(answer.getOrderNumber())
                .content(answer.getQuestionContent())
                .level(answer.getLevel())
                .explanation(answer.getExplanation())
                .selectedOptionLabel(answer.getSelectedOptionLabel())
                .selectedOptionContent(answer.getSelectedOptionContent())
                .correctOptionLabel(answer.getCorrectOptionLabel())
                .correctOptionContent(answer.getCorrectOptionContent())
                .correct(answer.isCorrect())
                .options(readOptionsJson(answer.getOptionsJson()))
                .build();
    }

    private String writeOptionsJson(List<ExamResultDetailResponse.OptionResult> options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Không thể lưu đáp án bài thi.", e);
        }
    }

    private List<ExamResultDetailResponse.OptionResult> readOptionsJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank() || "all".equalsIgnoreCase(value)) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private record AnswerSnapshot(
            String selectedOptionLabel,
            String selectedOptionContent,
            String correctOptionLabel,
            String correctOptionContent,
            boolean correct,
            List<ExamResultDetailResponse.OptionResult> options
    ) {}
}
