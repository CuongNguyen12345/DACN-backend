package com.cuong.backend.controller;

import com.cuong.backend.model.response.ChapterResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.model.response.PageResponse;
import com.cuong.backend.model.response.QuizDetailResponseDTO;
import com.cuong.backend.model.response.QuizResponseDTO;
import com.cuong.backend.model.request.QuizSubmitRequest;
import com.cuong.backend.model.response.QuizSubmitResponseDTO;
import com.cuong.backend.model.response.StudyActivityResponse;
import com.cuong.backend.service.CourseService;
import com.cuong.backend.service.QuizService;
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

    @Autowired
    private QuizService quizService;

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

    @GetMapping("/quizzes")
    public List<QuizResponseDTO> getQuizzesByLessons(@RequestParam(required = false) List<Integer> lessonIds) {
        return quizService.getQuizzesByLessonIds(lessonIds);
    }

    @GetMapping("/quizzes/course")
    public List<QuizResponseDTO> getCourseQuizzes(@RequestParam Integer lessonId) {
        return quizService.getQuizzesByCourseLesson(lessonId);
    }

    @GetMapping("/quizzes/{id}")
    public QuizDetailResponseDTO getQuizDetails(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    @PostMapping("/quizzes/{id}/submit")
    public QuizSubmitResponseDTO submitQuiz(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody QuizSubmitRequest request) {
        long userId = userService.getProfile(token).getId();
        return quizService.submitQuiz(id, userId, request);
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

    /**
     * Lưu thời gian đang xem video của bài học.
     */
    @PostMapping("/progress/time")
    public void saveWatchTime(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer lessonId,
            @RequestParam Integer time) {
        long userId = userService.getProfile(token).getId();
        courseService.saveWatchTime(userId, lessonId, time);
    }

    /**
     * Lấy thời gian đã xem của bài học.
     */
    @GetMapping("/progress/time")
    public Integer getWatchTime(
            @RequestHeader("Authorization") String token,
            @RequestParam Integer lessonId) {
        long userId = userService.getProfile(token).getId();
        Integer time = courseService.getLastWatchedTime(userId, lessonId);
        return time != null ? time : 0;
    }

    @GetMapping("/progress/study-activity")
    public StudyActivityResponse getStudyActivity(
            @RequestHeader("Authorization") String token) {
        long userId = userService.getProfile(token).getId();
        return courseService.getStudyActivity(userId);
    }
}
