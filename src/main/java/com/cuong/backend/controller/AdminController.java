package com.cuong.backend.controller;

import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.request.CreateChapterRequest;
import com.cuong.backend.model.request.CreateLessonRequest;
import com.cuong.backend.entity.ChapterEntity;
import com.cuong.backend.model.request.UpdateLessonRequest;
import com.cuong.backend.model.request.CreateTeacherRequest;
import com.cuong.backend.model.request.QuizRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.AiChatRequest;
import com.cuong.backend.model.response.AiChatResponse;
import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.CreateLessonResponse;
import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.model.response.QuizDetailResponseDTO;
import com.cuong.backend.model.response.QuizResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.model.response.QuestionDetailResponseDTO;
import com.cuong.backend.model.response.QuestionResponseDTO;
import com.cuong.backend.model.response.UserAccountDTO;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.service.AdminService;
import com.cuong.backend.service.QuizService;
import com.cuong.backend.util.FormatUtil;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final QuizService quizService;
    private final SubjectRepository subjectRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AdminController(AdminService adminService,
                           QuizService quizService,
                           SubjectRepository subjectRepository,
                           SimpMessagingTemplate messagingTemplate) {
        this.adminService = adminService;
        this.quizService = quizService;
        this.subjectRepository = subjectRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/ai/generate-questions")
    public AiChatResponse generateQuestions(@RequestBody AiChatRequest request) {
        String result = adminService.generateQuiz(request.getMessage(), request.getBase64Image());
        return AiChatResponse.builder()
                .result(result)
                .build();
    }

    @PostMapping("/questions")
    public String addQuestions(@RequestBody AddQuestionListRequest request) {
        return adminService.addQuestionList(request);
    }

    @GetMapping("/questions")
    public List<QuestionResponseDTO> getAllQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String topicName) {
        return adminService.getAllQuestions(keyword, subject, level, grade, topicName);
    }

    @GetMapping("/questions/{id}")
    public QuestionDetailResponseDTO getQuestion(@PathVariable Long id) {
        return adminService.getQuestionById(id);
    }

    @PutMapping("/questions/{id}")
    public String updateQuestion(@PathVariable Long id, @RequestBody UpdateQuestionRequest request) {
        return adminService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        return adminService.deleteQuestion(id);
    }

    // ---------- Topics ----------

    @GetMapping("/topics")
    public List<Map<String, Object>> getTopics(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        // Resolve subjectId from name + grade
        String dbSubject = FormatUtil.mapSubjectToDb(subject);
        String dbGrade = FormatUtil.mapGradeToDb(grade);

        if (dbSubject == null || dbGrade == null) {
            return List.of();
        }

        return subjectRepository.findByNameAndGrade(dbSubject, dbGrade)
                .map(s -> adminService.getTopicsBySubjectId(s.getId()))
                .orElse(List.of())
                .stream()
                .map(t -> Map.<String, Object>of(
                        "id", t.getId(),
                        "name", t.getName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/topics/migrate")
    public String migrateTopics() {
        return adminService.migrateQuestionTopics();
    }

    // ---------- Exams ----------

    @PostMapping("/exams")
    public CreateExamResponse createExam(@RequestBody CreateExamRequest request) {
        CreateExamResponse response = adminService.createExam(request);
        messagingTemplate.convertAndSend("/topic/exams/new", response);
        return response;
    }

    @GetMapping("/exams")
    public List<ExamResponseDTO> getAllExams(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        return adminService.getAllExams(keyword, subject, grade);
    }

    @GetMapping("/exams/{id}")
    public ExamDetailResponseDTO getExamById(@PathVariable Long id) {
        return adminService.getExamById(id);
    }

    // ---------- Quizzes / Exercises ----------

    @PostMapping("/quizzes")
    public QuizResponseDTO createQuiz(@RequestBody QuizRequest request) {
        return quizService.createQuiz(request);
    }

    @GetMapping("/quizzes")
    public List<QuizResponseDTO> getAllQuizzes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String lesson) {
        return quizService.getAllQuizzes(keyword, subject, grade, lesson);
    }

    @GetMapping("/quizzes/{id}")
    public QuizDetailResponseDTO getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    @PutMapping("/quizzes/{id}")
    public QuizResponseDTO updateQuiz(@PathVariable Long id, @RequestBody QuizRequest request) {
        return quizService.updateQuiz(id, request);
    }

    @DeleteMapping("/quizzes/{id}")
    public void deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
    }

    // ---------- Teacher Account ----------

    @PostMapping("/create-teacher")
    public String createTeacher(@RequestBody CreateTeacherRequest request) {
        return adminService.createTeacher(request);
    }

    // ---------- Account Management ----------

    @GetMapping("/accounts")
    public List<UserAccountDTO> searchAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        return adminService.searchAccounts(keyword, role);
    }
    @GetMapping("/accounts/{id}")
    public com.cuong.backend.model.response.AccountDetailDTO getAccountDetail(@PathVariable Long id) {
        return adminService.getAccountDetail(id);
    }

    @GetMapping("/chapters")
    public List<ChapterEntity> getChapters(
            @RequestParam String subject,
            @RequestParam String grade) {
        return adminService.getChapters(subject, grade);
    }

    @PostMapping("/chapters")
    public ChapterEntity createChapter(@RequestBody CreateChapterRequest request) {
        return adminService.createChapter(request);
    }

    @GetMapping("/lessons")
    public List<LessonResponseDTO> getAllLessons(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        return adminService.getAllLessons(keyword, subject, grade);
    }

    @PostMapping("/lessons")
    public CreateLessonResponse createLesson(@RequestBody CreateLessonRequest request) {
        return adminService.createLesson(request);
    }

    @PostMapping(value = "/lessons/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateLessonResponse createLessonWithUpload(
            @RequestParam int chapterId,
            @RequestParam String lessonName,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile pdfFile) throws IOException {
        return adminService.createLessonWithUpload(chapterId, lessonName, content, duration, status, type, videoFile,
                pdfFile);
    }

    @PutMapping("/lessons/{id}")
    public CreateLessonResponse updateLesson(
            @PathVariable Integer id,
            @RequestBody UpdateLessonRequest request) {
        return adminService.updateLesson(id, request);
    }

    @PutMapping(value = "/lessons/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateLessonResponse updateLessonWithUpload(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer chapterId,
            @RequestParam(required = false) String lessonName,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String duration,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile pdfFile) throws IOException {
        return adminService.updateLessonWithUpload(id, chapterId, lessonName, content, duration, status, type,
                videoFile, pdfFile);
    }

    @DeleteMapping("/lessons/{id}")
    public String deleteLesson(@PathVariable Integer id) {
        return adminService.deleteLesson(id);
    }

    @GetMapping("/lessons/{id}")
    public LessonResponseDTO getLessonById(@PathVariable Integer id) {
        return adminService.getLessonById(id);
    }

}
