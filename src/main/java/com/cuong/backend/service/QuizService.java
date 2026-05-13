package com.cuong.backend.service;

import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.QuizEntity;
import com.cuong.backend.entity.QuizQuestionItemEntity;
import com.cuong.backend.entity.QuizQuestionItemId;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.TopicEntity;
import com.cuong.backend.model.request.QuizRequest;
import com.cuong.backend.model.response.QuizDetailResponseDTO;
import com.cuong.backend.model.response.QuizResponseDTO;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.QuestionRepository;
import com.cuong.backend.repository.QuizRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.TopicRepository;
import com.cuong.backend.util.FormatUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class QuizService {

    private static final String[] OPTION_LABELS = { "A", "B", "C", "D", "E", "F" };
    private final QuizRepository quizRepository;
    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    public QuizService(
            QuizRepository quizRepository,
            TopicRepository topicRepository,
            SubjectRepository subjectRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository) {
        this.quizRepository = quizRepository;
        this.topicRepository = topicRepository;
        this.subjectRepository = subjectRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public QuizResponseDTO createQuiz(QuizRequest request) {
        QuizEntity quiz = new QuizEntity();
        applyQuizFields(quiz, request);
        QuizEntity saved = quizRepository.save(quiz);
        replaceQuestionItems(saved, resolveQuestions(request.getQuestionIds()));
        return toResponse(quizRepository.save(saved));
    }

    @Transactional
    public QuizResponseDTO updateQuiz(Long id, QuizRequest request) {
        QuizEntity quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập ID: " + id));

        applyQuizFields(quiz, request);
        replaceQuestionItems(quiz, resolveQuestions(request.getQuestionIds()));
        return toResponse(quizRepository.save(quiz));
    }

    @Transactional
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài tập ID: " + id);
        }
        quizRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDTO> getAllQuizzes(String keyword, String subject, String grade, String lesson) {
        String searchTerm = normalize(keyword);
        String subjectFilter = normalizeOption(subject);
        String gradeFilter = normalizeOption(grade);
        String lessonFilter = normalizeOption(lesson);

        return quizRepository.findAll().stream()
                .map(this::toResponse)
                .filter(quiz -> matchesSearch(quiz, searchTerm))
                .filter(quiz -> subjectFilter == null || quiz.getSubject().equals(subjectFilter))
                .filter(quiz -> gradeFilter == null || quiz.getGrade().equals(gradeFilter))
                .filter(quiz -> lessonFilter == null || quiz.getLessonTitle().equals(lessonFilter))
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizDetailResponseDTO getQuizById(Long id) {
        QuizEntity quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập ID: " + id));

        QuizMeta meta = resolveMeta(quiz);
        List<QuizDetailResponseDTO.QuestionItem> questions = quiz.getQuestionItems().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderNumber(), b.getOrderNumber()))
                .map(this::toQuestionItem)
                .toList();

        return QuizDetailResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .lessonId(quiz.getLessonId())
                .lessonTitle(meta.lessonTitle())
                .topicId(meta.topicId())
                .topicName(meta.topicName())
                .subject(meta.subject())
                .grade(meta.grade())
                .duration(quiz.getDuration())
                .passingScore(quiz.getPassingScore())
                .questions(questions)
                .build();
    }

    private void applyQuizFields(QuizEntity quiz, QuizRequest request) {
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        if (title.isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên bài tập.");
        }

        TopicEntity topic = resolveTopic(request);
        quiz.setTitle(title);
        quiz.setTopicId(topic.getId());
        quiz.setLessonId(resolveLessonId(request.getLessonId()));
        quiz.setDuration(Math.max(request.getDuration(), 0));
        quiz.setPassingScore(Math.max(request.getPassingScore(), 0));
    }

    private TopicEntity resolveTopic(QuizRequest request) {
        String subjectName = FormatUtil.mapSubjectToDb(request.getSubject());
        String grade = FormatUtil.mapGradeToDb(request.getGrade());

        if (subjectName == null || subjectName.isBlank() || grade == null || grade.isBlank()) {
            throw new RuntimeException("Vui lòng chọn môn học và lớp hợp lệ.");
        }

        SubjectEntity subject = subjectRepository.findByNameAndGrade(subjectName, grade)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy môn học " + subjectName + " lớp " + grade));

        String topicName = request.getTopicName() == null ? "" : request.getTopicName().trim();
        if (topicName.isEmpty() || "all".equals(topicName)) {
            throw new RuntimeException("Vui lòng chọn chủ đề.");
        }

        return topicRepository.findByNameAndSubjectId(topicName, subject.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề: " + topicName));
    }

    private Integer resolveLessonId(Integer lessonId) {
        if (lessonId == null) return null;
        if (!lessonRepository.existsById(lessonId)) {
            throw new RuntimeException("Không tìm thấy bài học ID: " + lessonId);
        }
        return lessonId;
    }

    private List<QuestionEntity> resolveQuestions(List<String> rawQuestionIds) {
        Set<Long> ids = new LinkedHashSet<>();
        if (rawQuestionIds != null) {
            for (String rawId : rawQuestionIds) {
                Long id = parseQuestionId(rawId);
                if (id != null) ids.add(id);
            }
        }

        if (ids.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một câu hỏi từ ngân hàng câu hỏi.");
        }

        List<QuestionEntity> questions = new ArrayList<>();
        for (Long id : ids) {
            QuestionEntity question = questionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi ID: " + id));
            questions.add(question);
        }
        return questions;
    }

    private Long parseQuestionId(String rawId) {
        if (rawId == null) return null;
        String cleaned = rawId.trim().replaceFirst("^Q-?", "");
        if (cleaned.isEmpty()) return null;
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void replaceQuestionItems(QuizEntity quiz, List<QuestionEntity> questions) {
        quiz.getQuestionItems().clear();
        for (int i = 0; i < questions.size(); i++) {
            QuestionEntity question = questions.get(i);
            QuizQuestionItemEntity item = new QuizQuestionItemEntity();
            item.setId(new QuizQuestionItemId(quiz.getId(), question.getId()));
            item.setQuiz(quiz);
            item.setQuestion(question);
            item.setOrderNumber(i + 1);
            quiz.getQuestionItems().add(item);
        }
    }

    private QuizResponseDTO toResponse(QuizEntity quiz) {
        QuizMeta meta = resolveMeta(quiz);
        return QuizResponseDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .lessonId(quiz.getLessonId())
                .lessonTitle(meta.lessonTitle())
                .topicId(meta.topicId())
                .topicName(meta.topicName())
                .subject(meta.subject())
                .grade(meta.grade())
                .questionCount(quiz.getQuestionItems().size())
                .duration(quiz.getDuration())
                .passingScore(quiz.getPassingScore())
                .createdAt(formatDate(quiz.getCreatedAt()))
                .updatedAt(formatDate(quiz.getUpdatedAt()))
                .build();
    }

    private QuizDetailResponseDTO.QuestionItem toQuestionItem(QuizQuestionItemEntity item) {
        QuestionEntity question = item.getQuestion();
        String topicName = question.getTopicId() == null
                ? ""
                : topicRepository.findById(question.getTopicId()).map(TopicEntity::getName).orElse("");

        List<QuizDetailResponseDTO.OptionItem> options = new ArrayList<>();
        if (question.getOptions() != null) {
            int index = 0;
            for (QuestionOptionEntity option : question.getOptions()) {
                options.add(QuizDetailResponseDTO.OptionItem.builder()
                        .label(index < OPTION_LABELS.length ? OPTION_LABELS[index] : String.valueOf(index + 1))
                        .content(option.getContent())
                        .correct(option.isCorrect())
                        .build());
                index++;
            }
        }

        return QuizDetailResponseDTO.QuestionItem.builder()
                .id(question.getId())
                .orderNumber(item.getOrderNumber())
                .content(question.getContent())
                .level(FormatUtil.mapLevelToFe(question.getLevel()))
                .explanation(question.getExplanation())
                .topicName(topicName)
                .options(options)
                .build();
    }

    private QuizMeta resolveMeta(QuizEntity quiz) {
        TopicEntity topic = topicRepository.findById(quiz.getTopicId()).orElse(null);
        SubjectEntity subject = topic == null
                ? null
                : subjectRepository.findById(topic.getSubjectId()).orElse(null);
        LessonEntity lesson = quiz.getLessonId() == null
                ? null
                : lessonRepository.findById(quiz.getLessonId()).orElse(null);

        String subjectName = subject == null ? "Khác" : FormatUtil.mapSubjectToFe(subject.getName());
        String grade = subject == null ? "Chưa rõ" : "Lớp " + subject.getGrade();
        String topicName = topic == null ? "" : topic.getName();
        String lessonTitle = lesson == null
                ? (topicName.isBlank() ? "Chưa gắn bài học" : topicName)
                : lesson.getLessonName();

        return new QuizMeta(
                topic == null ? null : topic.getId(),
                topicName,
                lessonTitle,
                subjectName,
                grade);
    }

    private boolean matchesSearch(QuizResponseDTO quiz, String searchTerm) {
        return searchTerm == null
                || containsNormalized(quiz.getTitle(), searchTerm)
                || containsNormalized(quiz.getLessonTitle(), searchTerm)
                || containsNormalized(quiz.getTopicName(), searchTerm);
    }

    private String normalizeOption(String value) {
        if (value == null || value.isBlank() || "all".equals(value)) return null;
        return value;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean containsNormalized(String value, String searchTerm) {
        String normalized = normalize(value);
        return normalized != null && normalized.contains(searchTerm);
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    private record QuizMeta(
            Integer topicId,
            String topicName,
            String lessonTitle,
            String subject,
            String grade) {
    }
}
