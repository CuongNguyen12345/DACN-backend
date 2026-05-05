package com.cuong.backend.controller;

import com.cuong.backend.model.response.ChapterResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.model.response.PageResponse;
import com.cuong.backend.service.CourseService;
import com.cuong.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @GetMapping("/subjects")
    public List<com.cuong.backend.entity.SubjectEntity> getAllSubjects() {
        return courseService.getAllSubjects();
    }

    @GetMapping("/course")
    public PageResponse<ChapterResponseDTO> getCourseData(
            @RequestParam(required = false, defaultValue = "all") String grade,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "3") int size) {
        return courseService.getCourseData(grade, subject, keyword, page, size);
    }

    @GetMapping("/course/first-lesson")
    public Integer getFirstLessonId(
            @RequestParam String grade,
            @RequestParam String subject) {
        return courseService.getFirstLessonId(grade, subject);
    }

    @GetMapping("/course/content")
    public List<ChapterResponseDTO> getCourseContentByLesson(@RequestParam Integer lessonId) {
        return courseService.getCourseDataByLessonId(lessonId);
    }

    @GetMapping("/lesson/{id}")
    public LessonResponseDTO getLessonDetails(@PathVariable Integer id) {
        return courseService.getLessonById(id);
    }

    /**
     * Lấy danh sách lesson_id đã hoàn thành của user hiện tại
     * trong tập hợp lessonIds được truyền lên.
     */
    @GetMapping("/progress")
    public List<Integer> getProgress(
            @RequestHeader("Authorization") String token,
            @RequestParam List<Integer> lessonIds) {
        long userId = userService.getProfile(token).getId();
        return courseService.getCompletedLessonIds(userId, lessonIds);
    }

    /**
     * Đánh dấu bài học đã hoàn thành.
     */
    @PostMapping("/progress/complete")
    public void markComplete(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer lessonId) {
        long userId = userService.getProfile(token).getId();
        courseService.markLessonCompleted(userId, lessonId);
    }
}
