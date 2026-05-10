package com.cuong.backend.controller;

import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.CreateExamRequest;
import com.cuong.backend.model.request.CreateChapterRequest;
import com.cuong.backend.model.request.CreateLessonRequest;
import com.cuong.backend.entity.ChapterEntity;
import com.cuong.backend.model.request.UpdateLessonRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.AiChatRequest;
import com.cuong.backend.model.response.AiChatResponse;
import com.cuong.backend.model.response.CreateExamResponse;
import com.cuong.backend.model.response.CreateLessonResponse;
import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.model.response.QuestionDetailResponseDTO;
import com.cuong.backend.model.response.QuestionResponseDTO;
import com.cuong.backend.service.AdminService;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
