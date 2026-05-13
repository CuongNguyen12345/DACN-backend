package com.cuong.backend.service;

import com.cuong.backend.entity.TopicMasteryEntity;
import com.cuong.backend.entity.TopicEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.repository.TopicMasteryRepository;
import com.cuong.backend.repository.TopicRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.util.FormatUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    private final TopicMasteryRepository masteryRepo;
    private final TopicRepository topicRepo;
    private final SubjectRepository subjectRepo;
    private final UserService userService;
    private final StudyActivityService studyActivityService;

    public RoadmapService(TopicMasteryRepository masteryRepo,
                          TopicRepository topicRepo,
                          SubjectRepository subjectRepo,
                          UserService userService,
                          StudyActivityService studyActivityService) {
        this.masteryRepo = masteryRepo;
        this.topicRepo = topicRepo;
        this.subjectRepo = subjectRepo;
        this.userService = userService;
        this.studyActivityService = studyActivityService;
    }

    // ─── Save mastery từ kết quả Assessment ─────────────────────────────────

    /**
     * Tính điểm mastery cho từng topic dựa trên kết quả assessment.
     * Công thức: masteryScore = correctInTopic / totalInTopic
     * Nếu đã có mastery cũ: new = old * 0.4 + new * 0.6
     */
    @Transactional
    public void saveFromAssessment(String token,
                                   List<Map<String, Object>> questionResults) {
        long userId = userService.getProfile(token).getId();

        // Nhóm câu hỏi theo topicId
        Map<Integer, long[]> topicStats = new LinkedHashMap<>();
        // topicStats[topicId] = [correctCount, totalCount]
        long correctTotal = 0;
        long questionTotal = 0;

        for (Map<String, Object> qr : questionResults) {
            Object topicIdObj = qr.get("topicId");
            if (topicIdObj == null) continue;

            int topicId = ((Number) topicIdObj).intValue();
            boolean correct = Boolean.TRUE.equals(qr.get("correct"));
            questionTotal++;
            if (correct) correctTotal++;

            topicStats.computeIfAbsent(topicId, k -> new long[]{0, 0});
            topicStats.get(topicId)[1]++; // total
            if (correct) topicStats.get(topicId)[0]++; // correct
        }

        // Upsert mastery cho từng topic
        for (Map.Entry<Integer, long[]> entry : topicStats.entrySet()) {
            int topicId = entry.getKey();
            double rawScore = (double) entry.getValue()[0] / entry.getValue()[1];

            Optional<TopicMasteryEntity> existing = masteryRepo.findByUserIdAndTopicId(userId, topicId);
            if (existing.isPresent()) {
                TopicMasteryEntity e = existing.get();
                // Cập nhật có trọng số: giữ 40% cũ + 60% mới
                e.setMasteryScore(e.getMasteryScore() * 0.4 + rawScore * 0.6);
                masteryRepo.save(e);
            } else {
                TopicMasteryEntity e = new TopicMasteryEntity();
                e.setUserId(userId);
                e.setTopicId(topicId);
                e.setMasteryScore(rawScore);
                masteryRepo.save(e);
            }
        }

        if (questionTotal > 0 && ((double) correctTotal / questionTotal) >= 0.6) {
            studyActivityService.recordStudyActivity(userId, "ROADMAP_ASSESSMENT_60_PERCENT");
        }
    }

    // ─── Lấy Roadmap data ────────────────────────────────────────────────────

    /**
     * Trả về danh sách tất cả topics của môn, kèm masteryScore của user.
     */
    @Transactional
    public double increaseMastery(long userId, int topicId, double increment) {
        TopicMasteryEntity mastery = masteryRepo.findByUserIdAndTopicId(userId, topicId)
                .orElseGet(() -> {
                    TopicMasteryEntity entity = new TopicMasteryEntity();
                    entity.setUserId(userId);
                    entity.setTopicId(topicId);
                    entity.setMasteryScore(0);
                    return entity;
                });

        double nextScore = Math.min(1.0, mastery.getMasteryScore() + Math.max(0, increment));
        mastery.setMasteryScore(nextScore);
        masteryRepo.save(mastery);
        return nextScore;
    }

    public List<Map<String, Object>> getRoadmap(String token, String subject, String grade) {
        long userId = userService.getProfile(token).getId();
        String dbSubject = FormatUtil.mapSubjectToDb(subject);
        String dbGrade = FormatUtil.mapGradeToDb(grade);

        SubjectEntity subjectEntity = subjectRepo.findByNameAndGrade(dbSubject, dbGrade)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn"));

        List<TopicEntity> topics = topicRepo.findBySubjectId(subjectEntity.getId());

        // Map topicId → masteryScore của user
        Map<Integer, Double> masteryMap = masteryRepo.findByUserId(userId).stream()
                .collect(Collectors.toMap(
                        TopicMasteryEntity::getTopicId,
                        TopicMasteryEntity::getMasteryScore));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < topics.size(); i++) {
            TopicEntity topic = topics.get(i);
            double mastery = masteryMap.getOrDefault(topic.getId(), -1.0);

            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", topic.getId());
            node.put("name", topic.getName());
            node.put("description", topic.getDescription());
            node.put("order", i);
            // masteryScore: -1 = chưa test, 0-1 = đã có điểm
            node.put("masteryScore", mastery);
            // status: LOCKED / WEAK / MEDIUM / MASTERED / UNTESTED
            node.put("status", computeStatus(mastery, i, masteryMap, topics));
            result.add(node);
        }
        return result;
    }

    private String computeStatus(double mastery, int order,
                                  Map<Integer, Double> masteryMap,
                                  List<TopicEntity> topics) {
        if (mastery < 0) {
            // Chưa test: topic đầu tiên luôn mở, còn lại LOCKED
            return order == 0 ? "UNTESTED" : "LOCKED";
        }
        if (mastery >= 0.8) return "MASTERED";
        if (mastery >= 0.5) return "MEDIUM";
        return "WEAK";
    }
}
