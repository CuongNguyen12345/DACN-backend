package com.cuong.backend.controller;

import com.cuong.backend.model.response.ChapterResponseDTO;
import com.cuong.backend.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/subjects")
    public List<com.cuong.backend.entity.SubjectEntity> getAllSubjects() {
        return courseService.getAllSubjects();
    }

    @GetMapping("/course")
    public List<ChapterResponseDTO> getCourseData(
            @RequestParam(required = false, defaultValue = "all") String grade,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword) {
        return courseService.getCourseData(grade, subject, keyword);
    }
}
