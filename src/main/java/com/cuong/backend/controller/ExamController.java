package com.cuong.backend.controller;

import com.cuong.backend.model.dto.ExamDTO;
import com.cuong.backend.service.ExamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/list")
    public List<ExamDTO> getExamList(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) String keyword) {
        return examService.getExamList(subjectId, grade, keyword);
    }
}