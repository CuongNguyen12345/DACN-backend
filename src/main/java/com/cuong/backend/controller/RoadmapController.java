package com.cuong.backend.controller;

import com.cuong.backend.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    /**
     * GET /api/roadmap?subject=Toán&grade=Lớp 10
     * Trả về danh sách topic với masteryScore của user hiện tại.
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getRoadmap(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String subject,
            @RequestParam String grade) {
        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(roadmapService.getRoadmap(token, subject, grade));
    }

    /**
     * POST /api/roadmap/sync-assessment
     * Lưu kết quả assessment vào topic_mastery.
     * Body: { questionResults: [{topicId, correct}] }
     */
    @PostMapping("/sync-assessment")
    public ResponseEntity<Void> syncAssessment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> body) {
        String token = authHeader.replace("Bearer ", "");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questionResults =
                (List<Map<String, Object>>) body.get("questionResults");
        roadmapService.saveFromAssessment(token, questionResults);
        return ResponseEntity.ok().build();
    }
}
