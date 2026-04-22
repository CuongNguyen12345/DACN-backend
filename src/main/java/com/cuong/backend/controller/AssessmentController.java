package com.cuong.backend.controller;

import com.cuong.backend.model.request.AssessmentAnalyzeRequest;
import com.cuong.backend.model.response.AssessmentQuestionsResponse;
import com.cuong.backend.service.AssessmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Smart Picker: Lấy 30 câu hỏi phù hợp theo môn, lớp và mục tiêu điểm.
     * GET /api/assessment/questions?subject=Vật Lý&grade=Lớp 12&targetScore=7-8
     */
    @GetMapping("/questions")
    public AssessmentQuestionsResponse getQuestions(
            @RequestParam String subject,
            @RequestParam String grade,
            @RequestParam String targetScore) {
        return assessmentService.getAssessmentQuestions(subject, grade, targetScore);
    }

    /**
     * AI Phân tích kết quả sau khi nộp bài.
     * POST /api/assessment/analyze
     */
    @PostMapping("/analyze")
    public String analyzeResult(@RequestBody AssessmentAnalyzeRequest request) {
        return assessmentService.analyzeResult(request);
    }
}
