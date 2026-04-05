package com.cuong.backend.controller;

import com.cuong.backend.model.request.AiChatRequest;
import com.cuong.backend.model.response.AiChatResponse;
import com.cuong.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

}
