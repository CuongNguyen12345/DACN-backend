package com.cuong.backend.controller;

import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.request.ExamSubmitRequest;
import com.cuong.backend.model.response.ExamResultDetailResponse;
import com.cuong.backend.model.response.ExamResultSummaryResponse;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.service.AdminService;
import com.cuong.backend.service.ExamResultService;
import com.cuong.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private ExamResultService examResultService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<ExamResponseDTO> getExamList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String grade) {
        return adminService.getAllExams(keyword, subject, grade);
    }

    @GetMapping("/{id}")
    public ExamDetailResponseDTO getExamById(@PathVariable Long id) {
        return adminService.getExamById(id);
    }

    @PostMapping("/{id}/submit")
    public ExamResultDetailResponse submitExam(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody ExamSubmitRequest request) {
        long userId = userService.getProfile(token).getId();
        return examResultService.submitExam(id, userId, request);
    }

    @GetMapping("/history")
    public List<ExamResultSummaryResponse> getExamHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword) {
        long userId = userService.getProfile(token).getId();
        return examResultService.getHistory(userId, subject, keyword);
    }

    @GetMapping("/history/{resultId}")
    public ExamResultDetailResponse getExamResultDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long resultId) {
        long userId = userService.getProfile(token).getId();
        return examResultService.getResultDetail(userId, resultId);
    }
}
