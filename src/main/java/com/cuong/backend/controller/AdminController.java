package com.cuong.backend.controller;

import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.request.CreateTeacherRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.AiChatRequest;
import com.cuong.backend.model.response.AiChatResponse;
import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.model.response.QuestionDetailResponseDTO;
import com.cuong.backend.model.response.QuestionResponseDTO;
import com.cuong.backend.model.response.UserAccountDTO;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.service.AdminService;
import com.cuong.backend.util.FormatUtil;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final SubjectRepository subjectRepository;

    public AdminController(AdminService adminService, SubjectRepository subjectRepository) {
        this.adminService = adminService;
        this.subjectRepository = subjectRepository;
    }

    @PostMapping("/ai/generate-questions")
    public AiChatResponse generateQuestions(@RequestBody AiChatRequest request) {
        String result = adminService.generateQuiz(request.getMessage());
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
            @RequestParam(required = false) String grade) {
        return adminService.getAllQuestions(keyword, subject, level, grade);
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

    // ---------- Exams ----------

    @PostMapping("/exams")
    public CreateExamResponse createExam(@RequestBody CreateExamRequest request) {
        return adminService.createExam(request);
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

}
