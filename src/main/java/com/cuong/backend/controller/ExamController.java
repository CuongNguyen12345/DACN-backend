package com.cuong.backend.controller;

import com.cuong.backend.model.response.ExamDetailResponseDTO;
import com.cuong.backend.model.response.ExamResponseDTO;
import com.cuong.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    @Autowired
    private AdminService adminService;

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
    public void submitExam(@PathVariable Long id) {
        adminService.incrementAttemptCount(id);
    }
}
