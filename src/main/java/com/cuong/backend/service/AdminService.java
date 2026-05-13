package com.cuong.backend.service;

import java.text.Normalizer;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.entity.ExamQuestionItemEntity;
import com.cuong.backend.entity.ExamQuestionItemId;
import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.TopicEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.entity.TopicMasteryEntity;
import com.cuong.backend.entity.UserProgressEntity;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;

import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.request.CreateTeacherRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.CreateLessonRequest;
import com.cuong.backend.model.request.CreateChapterRequest;
import com.cuong.backend.model.request.UpdateLessonRequest;

import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.CreateLessonResponse;
import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.model.response.QuestionDetailResponseDTO;
import com.cuong.backend.model.response.QuestionResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.ChapterEntity;

import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.QuestionRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.TopicRepository;
import com.cuong.backend.repository.UserRepository;
import com.cuong.backend.repository.UserProgressRepository;
import com.cuong.backend.repository.TopicMasteryRepository;

import com.cuong.backend.model.response.AccountDetailDTO;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.ChapterRepository;

import com.cuong.backend.util.FormatUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.langchain4j.model.chat.ChatLanguageModel;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final ChatLanguageModel aiModel;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final TopicRepository topicRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final TopicMasteryRepository topicMasteryRepository;
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;

    public AdminService(@Qualifier("adminModel") ChatLanguageModel aiModel,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            ExamRepository examRepository,
            TopicRepository topicRepository,
            UserRepository userRepository,
            UserProgressRepository userProgressRepository,
            TopicMasteryRepository topicMasteryRepository,
            LessonRepository lessonRepository,
            ChapterRepository chapterRepository,
            FileStorageService fileStorageService) {
        this.aiModel = aiModel;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.topicMasteryRepository = topicMasteryRepository;
        this.lessonRepository = lessonRepository;
        this.chapterRepository = chapterRepository;
        this.fileStorageService = fileStorageService;
    }

    public String generateQuiz(String lessonContent, String base64Image) {
        if (base64Image != null && !base64Image.isEmpty()) {
            String base64Data = base64Image;
            String mimeType = "image/png";
            if (base64Image.startsWith("data:")) {
                int commaIndex = base64Image.indexOf(",");
                if (commaIndex > 0) {
                    mimeType = base64Image.substring(5, base64Image.indexOf(";"));
                    base64Data = base64Image.substring(commaIndex + 1);
                }
            }
            UserMessage userMessage = UserMessage.from(
                TextContent.from(lessonContent),
                ImageContent.from(base64Data, mimeType)
            );
            return aiModel.generate(userMessage).content().text();
        }
        return aiModel.generate(lessonContent);
    }

    // ---------- Topic helpers ----------

    /**
     * Resolve topic by name within a subject. Returns topicId.
     * If topic doesn't exist, create it automatically.
     */
    private Integer resolveTopicId(String topicName, int subjectId) {
        return resolveTopicId(topicName, subjectId, null);
    }

    /**
     * Resolve topic by name within a subject, with optional content-based
     * validation.
     * Priority: exact match → content-based match → fuzzy name match → create new.
     */
    private Integer resolveTopicId(String topicName, int subjectId, String questionContent) {
        if (topicName == null || topicName.isBlank()) {
            // No topic name provided — try content-based detection
            if (questionContent != null && !questionContent.isBlank()) {
                Integer contentId = findTopicByContent(questionContent, subjectId);
                if (contentId != null)
                    return contentId;
            }
            return null;
        }
        String trimmedName = topicName.trim();

        // 1. Exact match — trust it, no override
        var exactMatch = topicRepository.findByNameAndSubjectId(trimmedName, subjectId);
        if (exactMatch.isPresent()) {
            return exactMatch.get().getId();
        }

        // 2. Fuzzy name match
        Integer similarId = findSimilarTopicId(trimmedName, subjectId);
        if (similarId != null)
            return similarId;

        // 3. Content-based match (chỉ dùng khi không match được tên)
        if (questionContent != null && !questionContent.isBlank()) {
            Integer contentId = findTopicByContent(questionContent, subjectId);
            if (contentId != null)
                return contentId;
        }

        // 4. Create new topic
        TopicEntity newTopic = new TopicEntity();
        newTopic.setSubjectId(subjectId);
        newTopic.setName(trimmedName);
        return topicRepository.save(newTopic).getId();
    }

    /**
     * Analyze question content to find the best matching topic.
     * Checks if significant keywords from topic names appear in the question text.
     */
    private Integer findTopicByContent(String content, int subjectId) {
        String normalizedContent = normalize(content);
        List<TopicEntity> candidates = topicRepository.findBySubjectId(subjectId);
        int bestScore = 0;
        Integer bestId = null;

        for (TopicEntity t : candidates) {
            String normalizedTopicName = normalize(t.getName());
            java.util.Set<String> topicKeywords = extractKeywords(normalizedTopicName);

            int score = 0;
            for (String keyword : topicKeywords) {
                if (normalizedContent.contains(keyword)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestId = t.getId();
            }
        }
        // Require at least 2 keyword matches, or 1 if topic has only 1 keyword
        if (bestId != null && bestScore >= 1) {
            return bestId;
        }
        return null;
    }

    private static final java.util.Set<String> STOP_WORDS = java.util.Set.of(
            "va", "cac", "cua", "trong", "cho", "theo", "den", "tu", "la", "co", "mot", "nhung", "voi");

    private java.util.Set<String> extractKeywords(String normalizedStr) {
        return java.util.Arrays.stream(normalizedStr.split("\\s+"))
                .filter(s -> !s.isEmpty() && s.length() > 1 && !STOP_WORDS.contains(s))
                .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Find a topic with a name similar to the given one within the same subject.
     * Uses keyword overlap after normalizing and filtering stop words.
     */
    private Integer findSimilarTopicId(String topicName, int subjectId) {
        java.util.Set<String> targetKeywords = extractKeywords(normalize(topicName));
        if (targetKeywords.isEmpty())
            return null;

        List<TopicEntity> candidates = topicRepository.findBySubjectId(subjectId);
        int bestScore = 0;
        Integer bestId = null;

        for (TopicEntity t : candidates) {
            java.util.Set<String> candKeywords = extractKeywords(normalize(t.getName()));
            java.util.Set<String> intersection = new java.util.HashSet<>(targetKeywords);
            intersection.retainAll(candKeywords);
            int score = intersection.size();
            if (score > bestScore) {
                bestScore = score;
                bestId = t.getId();
            }
        }

        // Require significant overlap: at least 50% of the smaller keyword set
        if (bestScore > 0 && bestId != null) {
            java.util.Set<String> bestKeywords = extractKeywords(normalize(
                    topicRepository.findById(bestId).map(TopicEntity::getName).orElse("")));
            int minSize = Math.min(targetKeywords.size(), bestKeywords.size());
            if (minSize > 0 && bestScore >= Math.max(1, (minSize + 1) / 2)) {
                return bestId;
            }
        }
        return null;
    }

    private String normalize(String str) {
        if (str == null)
            return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase();
    }

    private String resolveTopicName(Integer topicId) {
        if (topicId == null)
            return null;
        return topicRepository.findById(topicId)
                .map(TopicEntity::getName)
                .orElse(null);
    }

    public List<TopicEntity> getTopicsBySubjectId(int subjectId) {
        return topicRepository.findBySubjectId(subjectId);
    }

    /**
     * Migration: re-process all existing questions and fix their topic assignments
     * using content-based matching.
     */
    @Transactional
    public String migrateQuestionTopics() {
        List<QuestionEntity> allQuestions = questionRepository.findAll();
        int fixedCount = 0;

        for (QuestionEntity q : allQuestions) {
            if (q.getContent() == null || q.getContent().isBlank())
                continue;

            Integer newTopicId = findTopicByContent(q.getContent(), q.getSubjectId());
            if (newTopicId != null && !newTopicId.equals(q.getTopicId())) {
                q.setTopicId(newTopicId);
                fixedCount++;
            }
        }

        if (fixedCount > 0) {
            questionRepository.saveAll(allQuestions);
        }
        return "Đã cập nhật topic cho " + fixedCount + "/" + allQuestions.size() + " câu hỏi.";
    }

    // ---------- Question CRUD ----------

    @Transactional
    public String addQuestionList(AddQuestionListRequest request) {
        String grade = FormatUtil.mapGradeToDb(request.getGrade());
        String subjectName = FormatUtil.mapSubjectToDb(request.getSubject());

        SubjectEntity subject = subjectRepository.findByNameAndGrade(subjectName, grade)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy môn học " + request.getSubject() + " lớp " + request.getGrade()));

        List<QuestionEntity> entitiesToSave = new ArrayList<>();

        for (AddQuestionListRequest.QuestionItemRequest q : request.getQuestions()) {
            QuestionEntity questionEntity = new QuestionEntity();
            questionEntity.setSubjectId(subject.getId());
            questionEntity.setContent(q.getContent());
            questionEntity.setExplanation(q.getExplanation());
            questionEntity.setStatus("ACTIVE");

            // Map FE level to DB level
            String level = FormatUtil.mapLevelToDb(q.getLevel());
            questionEntity.setLevel(level);

            // Resolve topic (with content-based matching)
            Integer topicId = resolveTopicId(q.getTopicName(), subject.getId(), q.getContent());
            questionEntity.setTopicId(topicId);

            List<QuestionOptionEntity> optionEntities = new ArrayList<>();
            for (AddQuestionListRequest.OptionItemRequest opt : q.getOptions()) {
                QuestionOptionEntity optionEntity = new QuestionOptionEntity();
                optionEntity.setQuestion(questionEntity);
                optionEntity.setContent(opt.getContent());
                optionEntity.setCorrect(opt.getIsCorrect() != null ? opt.getIsCorrect() : false);
                optionEntities.add(optionEntity);
            }
            questionEntity.setOptions(optionEntities);
            entitiesToSave.add(questionEntity);
        }

        questionRepository.saveAll(entitiesToSave);
        return "Lưu thành công " + entitiesToSave.size() + " câu hỏi.";
    }

    public List<QuestionResponseDTO> getAllQuestions(String keyword, String subject, String level, String grade, String topicName) {
        List<QuestionEntity> entities;
        if ((keyword == null || keyword.isEmpty()) &&
                (subject == null || "all".equals(subject)) &&
                (level == null || "all".equals(level)) &&
                (grade == null || "all".equals(grade)) &&
                (topicName == null || "all".equals(topicName))) {
            entities = questionRepository.findAll();
        } else {
            entities = questionRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (keyword != null && !keyword.isEmpty()) {
                    predicates.add(cb.like(root.get("content"), "%" + keyword + "%"));
                }

                if (level != null && !"all".equals(level)) {
                    String dbLevel = FormatUtil.mapLevelToDb(level);
                    predicates.add(cb.equal(root.get("level"), dbLevel));
                }

                if ((subject != null && !"all".equals(subject)) || (grade != null && !"all".equals(grade))) {
                    var subQuery = query.subquery(Integer.class);
                    var subRoot = subQuery.from(SubjectEntity.class);
                    subQuery.select(subRoot.get("id"));

                    List<Predicate> subPredicates = new ArrayList<>();
                    if (subject != null && !"all".equals(subject)) {
                        String dbSubject = FormatUtil.mapSubjectToDb(subject);
                        subPredicates.add(cb.equal(subRoot.get("name"), dbSubject));
                    }
                    if (grade != null && !"all".equals(grade)) {
                        String dbGrade = FormatUtil.mapGradeToDb(grade);
                        subPredicates.add(cb.equal(subRoot.get("grade"), dbGrade));
                    }

                    subQuery.where(subPredicates.toArray(new Predicate[0]));
                    predicates.add(cb.in(root.get("subjectId")).value(subQuery));
                }

                if (topicName != null && !"all".equals(topicName)) {
                    var topicSubQuery = query.subquery(Integer.class);
                    var topicSubRoot = topicSubQuery.from(TopicEntity.class);
                    topicSubQuery.select(topicSubRoot.get("id"));

                    topicSubQuery.where(cb.equal(topicSubRoot.get("name"), topicName.trim()));

                    predicates.add(cb.in(root.get("topicId")).value(topicSubQuery));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            });
        }

        List<QuestionResponseDTO> result = new ArrayList<>();

        for (QuestionEntity q : entities) {
            String subjectName = "Khác";
            String statusName = "Chưa rõ";

            var sOpt = subjectRepository.findById(q.getSubjectId());
            if (sOpt.isPresent()) {
                SubjectEntity s = sOpt.get();
                subjectName = FormatUtil.mapSubjectToFe(s.getName());
                statusName = "Lớp " + s.getGrade();
            }

            String levelName = FormatUtil.mapLevelToFe(q.getLevel());
            String resolvedTopicName = resolveTopicName(q.getTopicId());

            result.add(QuestionResponseDTO.builder()
                    .id("Q-" + q.getId())
                    .content(q.getContent())
                    .subject(subjectName)
                    .level(levelName)
                    .type("Trắc nghiệm")
                    .status(statusName)
                    .topicName(resolvedTopicName)
                    .createdAt(q.getCreatedAt())
                    .build());
        }

        return result;
    }

    public QuestionDetailResponseDTO getQuestionById(Long id) {
        QuestionEntity q = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi với ID: " + id));

        String subjectName = "Khác";
        String statusName = "Chưa rõ";

        var sOpt = subjectRepository.findById(q.getSubjectId());
        if (sOpt.isPresent()) {
            SubjectEntity s = sOpt.get();
            subjectName = FormatUtil.mapSubjectToFe(s.getName());
            statusName = "Lớp " + s.getGrade();
        }

        String levelName = FormatUtil.mapLevelToFe(q.getLevel());
        String topicName = resolveTopicName(q.getTopicId());

        List<QuestionDetailResponseDTO.OptionDTO> optionDTOs = new ArrayList<>();
        String[] letters = { "A", "B", "C", "D", "E", "F" };
        int idx = 0;
        if (q.getOptions() != null) {
            for (QuestionOptionEntity opt : q.getOptions()) {
                optionDTOs.add(QuestionDetailResponseDTO.OptionDTO.builder()
                        .id(idx < letters.length ? letters[idx] : String.valueOf(idx))
                        .text(opt.getContent())
                        .isCorrect(opt.isCorrect())
                        .build());
                idx++;
            }
        }

        return QuestionDetailResponseDTO.builder()
                .id("Q-" + q.getId())
                .content(q.getContent())
                .subject(subjectName)
                .level(levelName)
                .type("Trắc nghiệm")
                .status(statusName)
                .topicName(topicName)
                .topicId(q.getTopicId())
                .createdAt(q.getCreatedAt())
                .explanation(q.getExplanation())
                .options(optionDTOs)
                .build();
    }

    @Transactional
    public String updateQuestion(Long id, UpdateQuestionRequest request) {
        QuestionEntity questionEntity = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy câu hỏi với ID: " + id));

        // Map Grade
        String grade = request.getGrade();
        if (grade != null && grade.startsWith("Lớp ")) {
            grade = grade.replace("Lớp ", "").trim();
        }

        // Map Subject
        String subjectName = FormatUtil.mapSubjectToDb(request.getSubject());
        if (subjectName != null && grade != null) {
            final String finalSubjectName = subjectName;
            final String finalGrade = grade;
            SubjectEntity subject = subjectRepository.findByNameAndGrade(finalSubjectName, finalGrade)
                    .orElseThrow(() -> new RuntimeException(
                            "Không tìm thấy môn học " + finalSubjectName + " lớp " + finalGrade));
            questionEntity.setSubjectId(subject.getId());

            // Resolve topic (with content-based matching)
            Integer topicId = resolveTopicId(request.getTopicName(), subject.getId(), request.getContent());
            questionEntity.setTopicId(topicId);
        }

        questionEntity.setContent(request.getContent());
        questionEntity.setExplanation(request.getExplanation());

        // Map Level
        if (request.getLevel() != null) {
            String level = FormatUtil.mapLevelToDb(request.getLevel());
            questionEntity.setLevel(level);
        }

        // Cập nhật Options
        if (questionEntity.getOptions() != null) {
            questionEntity.getOptions().clear();
        } else {
            questionEntity.setOptions(new ArrayList<>());
        }

        if (request.getOptions() != null) {
            for (UpdateQuestionRequest.OptionItemRequest opt : request.getOptions()) {
                QuestionOptionEntity optionEntity = new QuestionOptionEntity();
                optionEntity.setQuestion(questionEntity);
                optionEntity.setContent(opt.getContent());
                optionEntity.setCorrect(opt.getIsCorrect() != null ? opt.getIsCorrect() : false);
                questionEntity.getOptions().add(optionEntity);
            }
        }

        questionRepository.save(questionEntity);
        return "Cập nhật câu hỏi thành công.";
    }

    public String deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy câu hỏi với ID: " + id);
        }
        questionRepository.deleteById(id);
        return "Đã xóa thành công câu hỏi ID: " + id;
    }

    // ---------- Exam CRUD ----------

    @Transactional
    public CreateExamResponse createExam(CreateExamRequest request) {
        String subjectName = FormatUtil.mapSubjectToDb(request.getSubject());
        String grade = FormatUtil.mapGradeToDb(request.getGrade());

        final String finalSubjectName = subjectName;
        final String finalGrade = grade;
        int subjectId = subjectRepository.findByNameAndGrade(finalSubjectName, finalGrade)
                .map(SubjectEntity::getId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy môn học " + finalSubjectName + " lớp " + finalGrade));

        ExamEntity exam = new ExamEntity();
        exam.setTitle(request.getTitle());
        exam.setSubjectId(subjectId);
        exam.setDuration(request.getDuration());
        exam.setDescription(request.getDescription());
        exam.setTotalQuestions(request.getTotalQuestions());
        exam.setAttemptCount(0);

        List<Long> numericIds = new ArrayList<>();
        if (request.getQuestionIds() != null) {
            for (String raw : request.getQuestionIds()) {
                try {
                    String cleaned = raw.startsWith("Q-") ? raw.substring(2) : raw;
                    numericIds.add(Long.parseLong(cleaned));
                } catch (NumberFormatException ignored) {
                
                }
            }
        }

        List<QuestionEntity> questions = questionRepository.findAllById(numericIds);
        ExamEntity saved = examRepository.save(exam);

        for (int i = 0; i < questions.size(); i++) {
            ExamQuestionItemEntity item = new ExamQuestionItemEntity();
            item.setId(new ExamQuestionItemId(saved.getId(), questions.get(i).getId()));
            item.setExam(saved);
            item.setQuestion(questions.get(i));
            item.setOrderNumber(i + 1);
            saved.getQuestionItems().add(item);
        }

        examRepository.save(saved);

        return CreateExamResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .subject(request.getSubject())
                .grade(request.getGrade())
                .questionCount(questions.size())
                .message("Tạo đề thi thành công với " + questions.size() + " câu hỏi.")
                .build();
    }

    public List<ExamResponseDTO> getAllExams(String keyword, String subject, String grade) {
        List<ExamEntity> entities;
        if ((keyword == null || keyword.isEmpty()) &&
                (subject == null || "all".equals(subject)) &&
                (grade == null || "all".equals(grade))) {
            entities = examRepository.findAll();
        } else {
            entities = examRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (keyword != null && !keyword.isEmpty()) {
                    predicates.add(cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
                }

                if ((subject != null && !"all".equals(subject)) || (grade != null && !"all".equals(grade))) {
                    var subQuery = query.subquery(Integer.class);
                    var subRoot = subQuery.from(SubjectEntity.class);
                    subQuery.select(subRoot.get("id"));

                    List<Predicate> subPredicates = new ArrayList<>();
                    if (subject != null && !"all".equals(subject)) {
                        String dbSubject = FormatUtil.mapSubjectToDb(subject);
                        subPredicates.add(cb.equal(subRoot.get("name"), dbSubject));
                    }
                    if (grade != null && !"all".equals(grade)) {
                        String dbGrade = FormatUtil.mapGradeToDb(grade);
                        subPredicates.add(cb.equal(subRoot.get("grade"), dbGrade));
                    }

                    subQuery.where(subPredicates.toArray(new Predicate[0]));
                    predicates.add(cb.in(root.get("subjectId")).value(subQuery));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            });
        }

        return entities.stream().map(exam -> {

            String subjectName = "Khác";
            String gradeName = "Chưa rõ";
            var sOpt = subjectRepository.findById(exam.getSubjectId());
            if (sOpt.isPresent()) {
                subjectName = FormatUtil.mapSubjectToFe(sOpt.get().getName());
                gradeName = "Lớp " + sOpt.get().getGrade();
            }
            return ExamResponseDTO.builder()
                    .id(exam.getId())
                    .title(exam.getTitle())
                    .subject(subjectName)
                    .grade(gradeName)
                    .duration(exam.getDuration())
                    .questionCount(exam.getQuestionItems().size())
                    .attemptCount(exam.getAttemptCount())
                    .build();
        }).toList();
    }

    public ExamDetailResponseDTO getExamById(Long id) {
        ExamEntity exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi với ID: " + id));

        String subjectName = "Khác";
        var sOpt = subjectRepository.findById(exam.getSubjectId());
        if (sOpt.isPresent()) {
            subjectName = FormatUtil.mapSubjectToFe(sOpt.get().getName());
        }

        String[] letters = { "A", "B", "C", "D", "E", "F" };

        List<ExamDetailResponseDTO.QuestionItem> questionItems = exam.getQuestionItems().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderNumber(), b.getOrderNumber()))
                .map(item -> {
                    QuestionEntity q = item.getQuestion();

                    String levelName = FormatUtil.mapLevelToFe(q.getLevel());

                    List<ExamDetailResponseDTO.OptionItem> options = new ArrayList<>();
                    if (q.getOptions() != null) {
                        int idx = 0;
                        for (QuestionOptionEntity opt : q.getOptions()) {
                            options.add(ExamDetailResponseDTO.OptionItem.builder()
                                    .label(idx < letters.length ? letters[idx] : String.valueOf(idx))
                                    .content(opt.getContent())
                                    .correct(opt.isCorrect())
                                    .build());
                            idx++;
                        }
                    }

                    return ExamDetailResponseDTO.QuestionItem.builder()
                            .id(q.getId())
                            .orderNumber(item.getOrderNumber())
                            .content(q.getContent())
                            .level(levelName)
                            .explanation(q.getExplanation())
                            .options(options)
                            .build();
                }).toList();

        return ExamDetailResponseDTO.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .subject(subjectName)
                .duration(exam.getDuration())
                .questions(questionItems)
                .build();
    }


    public void incrementAttemptCount(Long id) {
        examRepository.incrementAttemptCount(id);
    }

    // ---------- Teacher Account ----------

    @Transactional
    public String createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống: " + request.getEmail());
        }
        if (request.getGrades() == null || request.getGrades().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một lớp để phân công.");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        String gradesStr = request.getGrades().stream()
                .map(g -> g.replace("Lớp ", "").trim())
                .collect(java.util.stream.Collectors.joining(","));

        UserEntity teacher = new UserEntity();
        teacher.setUserName(request.getName());
        teacher.setEmail(request.getEmail());
        teacher.setPassword(passwordEncoder.encode(request.getPassword()));
        teacher.setRole("TEACHER");
        teacher.setSchoolName(FormatUtil.mapSubjectToDb(request.getSubject()));
        teacher.setGrade(gradesStr);

        userRepository.save(teacher);
        return "Tạo tài khoản giáo viên thành công: " + request.getEmail();
    }

    // ---------- Account Detail ----------

    public AccountDetailDTO getAccountDetail(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản ID: " + id));

        boolean isTeacher = user.getRole().equalsIgnoreCase("TEACHER");

        // Build unit string
        String unit;
        if (isTeacher) {
            String subject = FormatUtil.mapSubjectToFe(user.getSchoolName());
            String grades = user.getGrade() != null ? " (Lớp " + user.getGrade() + ")" : "";
            unit = "Tổ " + (subject != null ? subject : user.getSchoolName()) + grades;
        } else {
            String grade = user.getGrade();
            if (grade != null && !grade.isBlank()) {
                String gradeNum = grade.replace("Lớp ", "").trim();
                unit = "Học sinh khối " + gradeNum;
            } else {
                unit = "Chưa cập nhật";
            }
        }

        // Calculate stats
        List<UserProgressEntity> progressList = userProgressRepository.findAll()
                .stream().filter(p -> p.getUserId() == user.getId() && p.isCompleted())
                .collect(java.util.stream.Collectors.toList());
        int completedLessons = progressList.size();

        List<TopicMasteryEntity> masteryList = topicMasteryRepository.findByUserId(user.getId());
        double avgScore = 0;
        if (!masteryList.isEmpty()) {
            avgScore = masteryList.stream()
                    .mapToDouble(TopicMasteryEntity::getMasteryScore)
                    .average().orElse(0);
            avgScore = Math.round(avgScore * 100.0) / 10.0; // Convert to 10-scale
        }

        // Count exams from exam entity (teacher: created, student: attempted)
        int totalExams;
        if (isTeacher) {
            totalExams = (int) examRepository.count();
        } else {
            totalExams = masteryList.size();
        }

        return AccountDetailDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .role(user.getRole())
                .unit(unit)
                .phoneNumber(user.getPhoneNumber())
                .grade(user.getGrade())
                .status("Hoạt động")
                .createdDate(user.getCreatedDate())
                .totalExams(totalExams)
                .avgScore(avgScore)
                .completedLessons(completedLessons)
                .examRecords(List.of())
                .build();
    }

    // ---------- Account Search ----------

    public List<com.cuong.backend.model.response.UserAccountDTO> searchAccounts(String keyword, String role) {
        List<UserEntity> users = userRepository.searchUsers(keyword, role);
        return users.stream()
                .filter(u -> !u.getRole().equalsIgnoreCase("ADMIN"))
                .map(u -> {
                    String unit;
                    if (u.getRole().equalsIgnoreCase("TEACHER")) {
                        // Giáo viên: hiển thị môn học + lớp phân công
                        String subject = FormatUtil.mapSubjectToFe(u.getSchoolName());
                        String grades = u.getGrade() != null ? " (Lớp " + u.getGrade() + ")" : "";
                        unit = "Tổ " + (subject != null ? subject : u.getSchoolName()) + grades;
                    } else {
                        // Học viên: hiển thị khối học
                        String grade = u.getGrade();
                        if (grade != null && !grade.isBlank()) {
                            // Xử lý trường hợp grade có thể là "10" hoặc "Lớp 10"
                            String gradeNum = grade.replace("Lớp ", "").trim();
                            unit = "Học sinh khối " + gradeNum;
                        } else {
                            unit = "Chưa cập nhật khối học";
                        }
                    }
                    return com.cuong.backend.model.response.UserAccountDTO.builder()
                            .id(u.getId())
                            .userName(u.getUserName())
                            .email(u.getEmail())
                            .role(u.getRole())
                            .unit(unit)
                            .createdDate(u.getCreatedDate())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public CreateLessonResponse createLesson(CreateLessonRequest request) {
        ChapterEntity chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương với ID: " + request.getChapterId()));

        if (request.getLessonName() == null || request.getLessonName().isBlank()) {
            throw new IllegalArgumentException("Tên bài học không được để trống.");
        }

        LessonEntity lesson = new LessonEntity();
        lesson.setChapterId(request.getChapterId());
        lesson.setLessonName(request.getLessonName().trim());
        lesson.setContent(request.getContent());
        lesson.setVideoUrl(request.getVideoUrl());
        lesson.setPdfUrl(request.getPdfUrl());
        lesson.setDuration(request.getDuration());
        lesson.setStatus(request.getStatus() != null ? request.getStatus() : "Đã xuất bản");
        lesson.setType(request.getType());

        LessonEntity saved = lessonRepository.save(lesson);

        return CreateLessonResponse.builder()
                .id(saved.getId())
                .lessonName(saved.getLessonName())
                .chapterName(chapter.getChapterName())
                .videoUrl(saved.getVideoUrl())
                .pdfUrl(saved.getPdfUrl())
                .message("Thêm bài học thành công.")
                .build();
    }

    @Transactional
    public CreateLessonResponse createLessonWithUpload(
            int chapterId,
            String lessonName,
            String content,
            String duration,
            String status,
            String type,
            MultipartFile videoFile,
            MultipartFile pdfFile) throws IOException {

        ChapterEntity chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương với ID: " + chapterId));

        if (lessonName == null || lessonName.isBlank()) {
            throw new IllegalArgumentException("Tên bài học không được để trống.");
        }

        String videoUrl = (videoFile != null && !videoFile.isEmpty()) ? fileStorageService.uploadVideo(videoFile)
                : null;
        String pdfUrl = (pdfFile != null && !pdfFile.isEmpty()) ? fileStorageService.uploadPdf(pdfFile) : null;

        LessonEntity lesson = new LessonEntity();
        lesson.setChapterId(chapterId);
        lesson.setLessonName(lessonName.trim());
        lesson.setContent(content);
        lesson.setVideoUrl(videoUrl);
        lesson.setPdfUrl(pdfUrl);
        lesson.setDuration(duration);
        lesson.setStatus(status != null ? status : "Đã xuất bản");
        lesson.setType(type);

        LessonEntity saved = lessonRepository.save(lesson);

        return CreateLessonResponse.builder()
                .id(saved.getId())
                .lessonName(saved.getLessonName())
                .chapterName(chapter.getChapterName())
                .videoUrl(saved.getVideoUrl())
                .pdfUrl(saved.getPdfUrl())
                .message("Thêm bài học và upload file thành công.")
                .build();
    }

    @Transactional
    public CreateLessonResponse updateLesson(Integer id, UpdateLessonRequest request) {
        LessonEntity lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + id));

        ChapterEntity chapter = null;
        if (request.getChapterId() != null) {
            chapter = chapterRepository.findById(request.getChapterId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chương với ID: " + request.getChapterId()));
            lesson.setChapterId(request.getChapterId());
        } else {
            chapter = chapterRepository.findById(lesson.getChapterId()).orElse(null);
        }

        if (request.getLessonName() != null && !request.getLessonName().isBlank()) {
            lesson.setLessonName(request.getLessonName().trim());
        }
        if (request.getContent() != null) {
            lesson.setContent(request.getContent());
        }
        if (request.getVideoUrl() != null) {
            lesson.setVideoUrl(request.getVideoUrl());
        }
        if (request.getPdfUrl() != null) {
            lesson.setPdfUrl(request.getPdfUrl());
        }
        if (request.getDuration() != null) {
            lesson.setDuration(request.getDuration());
        }
        if (request.getStatus() != null) {
            lesson.setStatus(request.getStatus());
        }
        if (request.getType() != null) {
            lesson.setType(request.getType());
        }

        LessonEntity saved = lessonRepository.save(lesson);

        return CreateLessonResponse.builder()
                .id(saved.getId())
                .lessonName(saved.getLessonName())
                .chapterName(chapter != null ? chapter.getChapterName() : "")
                .videoUrl(saved.getVideoUrl())
                .pdfUrl(saved.getPdfUrl())
                .message("Cập nhật bài học thành công.")
                .build();
    }

    @Transactional
    public CreateLessonResponse updateLessonWithUpload(
            Integer id,
            Integer chapterId,
            String lessonName,
            String content,
            String duration,
            String status,
            String type,
            MultipartFile videoFile,
            MultipartFile pdfFile) throws IOException {

        LessonEntity lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + id));

        ChapterEntity chapter = null;
        if (chapterId != null) {
            chapter = chapterRepository.findById(chapterId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chương với ID: " + chapterId));
            lesson.setChapterId(chapterId);
        } else {
            chapter = chapterRepository.findById(lesson.getChapterId()).orElse(null);
        }

        if (lessonName != null && !lessonName.isBlank()) {
            lesson.setLessonName(lessonName.trim());
        }
        if (content != null) {
            lesson.setContent(content);
        }
        if (duration != null) {
            lesson.setDuration(duration);
        }
        if (status != null) {
            lesson.setStatus(status);
        }
        if (type != null) {
            lesson.setType(type);
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            String videoUrl = fileStorageService.uploadVideo(videoFile);
            lesson.setVideoUrl(videoUrl);
        }
        if (pdfFile != null && !pdfFile.isEmpty()) {
            String pdfUrl = fileStorageService.uploadPdf(pdfFile);
            lesson.setPdfUrl(pdfUrl);
        }

        LessonEntity saved = lessonRepository.save(lesson);

        return CreateLessonResponse.builder()
                .id(saved.getId())
                .lessonName(saved.getLessonName())
                .chapterName(chapter != null ? chapter.getChapterName() : "")
                .videoUrl(saved.getVideoUrl())
                .pdfUrl(saved.getPdfUrl())
                .message("Cập nhật bài học và upload file thành công.")
                .build();
    }

    @Transactional
    public String deleteLesson(Integer id) {
        if (!lessonRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài học với ID: " + id);
        }
        lessonRepository.deleteById(id);
        return "Đã xóa thành công bài học ID: " + id;
    }

    public LessonResponseDTO getLessonById(Integer id) {
        LessonEntity lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài học với ID: " + id));

        ChapterEntity chapter = chapterRepository.findById(lesson.getChapterId()).orElse(null);
        String chapterName = "";
        String subjectName = "";
        String gradeName = "";

        if (chapter != null) {
            chapterName = chapter.getChapterName();
            SubjectEntity sub = subjectRepository.findById(chapter.getSubjectId()).orElse(null);
            if (sub != null) {
                subjectName = sub.getName();
                gradeName = sub.getGrade();
            }
        }

        LessonResponseDTO dto = new LessonResponseDTO();
        dto.setId(lesson.getId());
        dto.setLessonName(lesson.getLessonName());
        dto.setContent(lesson.getContent());
        dto.setVideoUrl(lesson.getVideoUrl());
        dto.setPdfUrl(lesson.getPdfUrl());
        dto.setDuration(lesson.getDuration());
        dto.setStatus(lesson.getStatus());
        dto.setType(lesson.getType());
        dto.setChapterId(lesson.getChapterId());
        dto.setChapterName(chapterName);
        dto.setSubject(subjectName);
        dto.setGrade(gradeName);
        return dto;
    }

    // ===================== LESSON SEARCH (ADMIN) =====================

    public List<LessonResponseDTO> getAllLessons(String keyword, String subject, String grade) {
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String searchSubject = (subject != null && !subject.isBlank() && !subject.equalsIgnoreCase("all"))
                ? FormatUtil.mapSubjectToDb(subject.trim())
                : null;
        String searchGrade = (grade != null && !grade.isBlank() && !grade.equalsIgnoreCase("all"))
                ? FormatUtil.mapGradeToDb(grade.trim())
                : null;

        List<LessonEntity> lessons = lessonRepository.searchLessons(searchKeyword, searchSubject, searchGrade);

        return lessons.stream().map(lesson -> {
            String chapterName = "";
            String subjectName = "";
            String gradeName = "";

            if (lesson.getChapterId() > 0) {
                ChapterEntity chapter = chapterRepository.findById(lesson.getChapterId()).orElse(null);
                if (chapter != null) {
                    chapterName = chapter.getChapterName();
                    SubjectEntity sub = subjectRepository.findById(chapter.getSubjectId()).orElse(null);
                    if (sub != null) {
                        subjectName = sub.getName();
                        gradeName = sub.getGrade();
                    }
                }
            }

            LessonResponseDTO dto = new LessonResponseDTO();
            dto.setId(lesson.getId());
            dto.setLessonName(lesson.getLessonName());
            dto.setContent(lesson.getContent());
            dto.setVideoUrl(lesson.getVideoUrl());
            dto.setPdfUrl(lesson.getPdfUrl());
            dto.setDuration(lesson.getDuration());
            dto.setStatus(lesson.getStatus());
            dto.setType(lesson.getType());
            dto.setChapterId(lesson.getChapterId());
            dto.setChapterName(chapterName);
            dto.setSubject(subjectName);
            dto.setGrade(gradeName);
            return dto;
        }).toList();
    }

    // ===================== CHAPTERS (ADMIN) =====================

    public List<ChapterEntity> getChapters(String subject, String grade) {
        String dbSubject = FormatUtil.mapSubjectToDb(subject);
        String dbGrade = FormatUtil.mapGradeToDb(grade);

        SubjectEntity sub = subjectRepository.findByNameAndGrade(dbSubject, dbGrade)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học: " + subject + " lớp " + grade));

        return chapterRepository.findBySubjectIdOrderByOrderNumberAsc(sub.getId());
    }

    @Transactional
    public ChapterEntity createChapter(CreateChapterRequest request) {
        String dbSubject = FormatUtil.mapSubjectToDb(request.getSubjectName());
        String dbGrade = FormatUtil.mapGradeToDb(request.getGrade());

        SubjectEntity sub = subjectRepository.findByNameAndGrade(dbSubject, dbGrade)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy môn học: " + request.getSubjectName() + " lớp " + request.getGrade()));

        ChapterEntity chapter = new ChapterEntity();
        chapter.setSubjectId(sub.getId());
        chapter.setChapterName(request.getChapterName());
        chapter.setOrderNumber(request.getOrderNumber() > 0 ? request.getOrderNumber() : 1);

        return chapterRepository.save(chapter);
    }
}
