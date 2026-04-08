package com.cuong.backend.controller;

import com.cuong.backend.model.request.AddQuestionListRequest;
import com.cuong.backend.model.request.UpdateQuestionRequest;
import com.cuong.backend.model.request.AiChatRequest;
import com.cuong.backend.model.response.AiChatResponse;
import com.cuong.backend.model.response.QuestionDetailResponseDTO;
import com.cuong.backend.model.response.QuestionResponseDTO;
import com.cuong.backend.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

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

}
