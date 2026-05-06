package com.cuong.backend.service;

import com.cuong.backend.entity.ExamEntity;
import com.cuong.backend.entity.ExamQuestionItemEntity;
import com.cuong.backend.entity.ExamQuestionItemId;
import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.CreateLessonRequest;
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
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.ChapterRepository;
import com.cuong.backend.service.FileStorageService;
import com.cuong.backend.util.FormatUtil;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final LessonRepository lessonRepository;
    private final ChapterRepository chapterRepository;
    private final FileStorageService fileStorageService;

    public AdminService(@Qualifier("adminModel") ChatLanguageModel aiModel,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            ExamRepository examRepository,
            LessonRepository lessonRepository,
            ChapterRepository chapterRepository,
            FileStorageService fileStorageService) {
        this.aiModel = aiModel;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.examRepository = examRepository;
        this.lessonRepository = lessonRepository;
        this.chapterRepository = chapterRepository;
        this.fileStorageService = fileStorageService;
    }

    public String generateQuiz(String lessonContent) {
        // Gọi AI model để sinh câu hỏi
        return aiModel.generate(lessonContent);
    }

    @Transactional
    public String addQuestionList(AddQuestionListRequest request) {
        // Map Frontend names to DB names if necessary. Example: "Lớp 10" -> "10"
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

            // Map FE level to DB level
            String level = FormatUtil.mapLevelToDb(q.getLevel());
            questionEntity.setLevel(level);

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

    public List<QuestionResponseDTO> getAllQuestions(String keyword, String subject, String level, String grade) {
        List<QuestionEntity> entities;
        if ((keyword == null || keyword.isEmpty()) &&
                (subject == null || "all".equals(subject)) &&
                (level == null || "all".equals(level)) &&
                (grade == null || "all".equals(grade))) {
            entities = questionRepository.findAll();
        } else {
            // Xây dựng Specification hoàn chỉnh bao gồm mapping subject
            entities = questionRepository.findAll((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (keyword != null && !keyword.isEmpty()) {
                    predicates.add(cb.like(root.get("content"), "%" + keyword + "%"));
                }

                if (level != null && !"all".equals(level)) {
                    String dbLevel = FormatUtil.mapLevelToDb(level);
                    predicates.add(cb.equal(root.get("level"), dbLevel));
                }

                // Vì không có @ManyToOne, ta dùng subquery để tìm subjectId
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

        List<QuestionResponseDTO> result = new ArrayList<>();

        for (QuestionEntity q : entities) {
            String subjectName = "Khác";
            String statusName = "Chưa rõ";

            var sOpt = subjectRepository.findById(q.getSubjectId());
            if (sOpt.isPresent()) {
                SubjectEntity s = sOpt.get();
                subjectName = FormatUtil.mapSubjectToDb(s.getName());
                statusName = "Lớp " + s.getGrade();
            }

            String levelName = q.getLevel();
            if ("MEDIUM".equals(q.getLevel()))
                levelName = "Trung bình";
            else if ("HARD".equals(q.getLevel()))
                levelName = "Khó";

            result.add(QuestionResponseDTO.builder()
                    .id("Q-" + q.getId())
                    .content(q.getContent())
                    .subject(subjectName)
                    .level(levelName)
                    .type("Trắc nghiệm")
                    .status(statusName)
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
            subjectName = FormatUtil.mapSubjectToDb(s.getName());
            statusName = "Lớp " + s.getGrade();
        }

        String levelName = q.getLevel();
        if ("MEDIUM".equals(q.getLevel()))
            levelName = "Trung bình";
        else if ("HARD".equals(q.getLevel()))
            levelName = "Khó";

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
        }

        questionEntity.setContent(request.getContent());
        questionEntity.setExplanation(request.getExplanation());

        // Map Level
        if (request.getLevel() != null) {
            String level = FormatUtil.mapLevelToDb(request.getLevel());
            questionEntity.setLevel(level);
        }

        // Cập nhật Options: Tận dụng orphanRemoval = true
        // Xóa tất cả options cũ bằng cách clear collection
        if (questionEntity.getOptions() != null) {
            questionEntity.getOptions().clear();
        } else {
            questionEntity.setOptions(new ArrayList<>());
        }

        // Thêm các options mới
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

    @Transactional
    public CreateExamResponse createExam(CreateExamRequest request) {
        // Map subject name FE -> DB
        String subjectName = FormatUtil.mapSubjectToDb(request.getSubject());

        // Map Grade
        String grade = FormatUtil.mapGradeToDb(request.getGrade());

        // Find subjectId
        final String finalSubjectName = subjectName;
        final String finalGrade = grade;
        int subjectId = subjectRepository.findByNameAndGrade(finalSubjectName, finalGrade)
                .map(SubjectEntity::getId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn học " + finalSubjectName + " lớp " + finalGrade));

        ExamEntity exam = new ExamEntity();
        exam.setTitle(request.getTitle());
        exam.setSubjectId(subjectId);
        exam.setDuration(request.getDuration());
        exam.setDescription(request.getDescription());
        exam.setTotalQuestions(request.getTotalQuestions());
        exam.setAttemptCount(0); // Optional default

        // Parse question IDs: "Q-123" or plain numeric
        List<Long> numericIds = new ArrayList<>();
        if (request.getQuestionIds() != null) {
            for (String raw : request.getQuestionIds()) {
                try {
                    String cleaned = raw.startsWith("Q-") ? raw.substring(2) : raw;
                    numericIds.add(Long.parseLong(cleaned));
                } catch (NumberFormatException ignored) {}
            }
        }

        List<QuestionEntity> questions = questionRepository.findAllById(numericIds);

        // Build question items - save exam first to get ID
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
            // Map subjectId -> ten mon FE
            String subjectName = "Khác";
            String gradeName = "Chưa rõ";
            var sOpt = subjectRepository.findById(exam.getSubjectId());
            if (sOpt.isPresent()) {
                subjectName = FormatUtil.mapSubjectToDb(sOpt.get().getName());
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
                .orElseThrow(() -> new RuntimeException("Kh\u00f4ng t\u00ecm th\u1ea5y đ\u1ec1 thi v\u1edbi ID: " + id));

        // Map subject
        String subjectName = "Kh\u00e1c";
        var sOpt = subjectRepository.findById(exam.getSubjectId());
        if (sOpt.isPresent()) {
            subjectName = sOpt.get().getName();
            if ("L\u00fd".equals(subjectName)) subjectName = "V\u1eadt L\u00fd";
            else if ("H\u00f3a".equals(subjectName)) subjectName = "H\u00f3a H\u1ecdc";
            else if ("Anh".equals(subjectName)) subjectName = "Ti\u1ebfng Anh";
        }

        String[] letters = {"A", "B", "C", "D", "E", "F"};

        List<ExamDetailResponseDTO.QuestionItem> questionItems = exam.getQuestionItems().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderNumber(), b.getOrderNumber()))
                .map(item -> {
                    QuestionEntity q = item.getQuestion();

                    String levelName = "D\u1ec5";
                    if ("MEDIUM".equals(q.getLevel())) levelName = "Trung b\u00ecnh";
                    else if ("HARD".equals(q.getLevel())) levelName = "Kh\u00f3";

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

    // ===================== LESSON CREATE (ADMIN) =====================

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

        String videoUrl = (videoFile != null && !videoFile.isEmpty()) ? fileStorageService.uploadVideo(videoFile) : null;
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

    // ===================== LESSON UPDATE (ADMIN) =====================

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

    // ===================== LESSON DELETE (ADMIN) =====================

    /**
     * Xóa bài học theo ID.
     * Endpoint: DELETE /api/admin/lessons/{id}
     */
    @Transactional
    public String deleteLesson(Integer id) {
        if (!lessonRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài học với ID: " + id);
        }
        lessonRepository.deleteById(id);
        return "Đã xóa thành công bài học ID: " + id;
    }

    // ===================== LESSON SEARCH (ADMIN) =====================

    public List<LessonResponseDTO> getAllLessons(String keyword, String subject, String grade) {
        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String searchSubject = (subject != null && !subject.isBlank() && !subject.equalsIgnoreCase("all")) ? subject.trim() : null;
        String searchGrade = (grade != null && !grade.isBlank() && !grade.equalsIgnoreCase("all")) ? grade.trim() : null;

        if (searchGrade != null) {
            if (searchGrade.equalsIgnoreCase("10")) searchGrade = "Lớp 10";
            else if (searchGrade.equalsIgnoreCase("11")) searchGrade = "Lớp 11";
            else if (searchGrade.equalsIgnoreCase("12")) searchGrade = "Lớp 12";
        }

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
            dto.setChapterName(chapterName);
            dto.setSubject(subjectName);
            dto.setGrade(gradeName);
            return dto;
        }).toList();
    }
}
