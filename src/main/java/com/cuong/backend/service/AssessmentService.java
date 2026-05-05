package com.cuong.backend.service;

import com.cuong.backend.entity.QuestionEntity;
import com.cuong.backend.entity.QuestionOptionEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.model.request.AssessmentAnalyzeRequest;
import com.cuong.backend.model.response.AssessmentQuestionsResponse;
import com.cuong.backend.repository.QuestionRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.TopicRepository;
import com.cuong.backend.util.FormatUtil;

import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final ChatLanguageModel aiModel;

    // ─── Smart Picker: tỉ lệ câu hỏi theo mục tiêu điểm ───────────────────
    // targetScore -> [easePct, mediumPct, hardPct]
    private static final Map<String, int[]> SCORE_RATIOS = new LinkedHashMap<>();
    static {
        SCORE_RATIOS.put("5-6", new int[] { 80, 20, 0 });
        SCORE_RATIOS.put("7-8", new int[] { 40, 40, 20 });
        SCORE_RATIOS.put("9-10", new int[] { 20, 40, 40 });
    }

    private static final int TOTAL_QUESTIONS = 30;

    public AssessmentService(QuestionRepository questionRepository,
            SubjectRepository subjectRepository,
            TopicRepository topicRepository,
            @Qualifier("adminModel") ChatLanguageModel aiModel) {
        this.questionRepository = questionRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.aiModel = aiModel;
    }

    // ─── Lấy câu hỏi theo Smart Picker ────────────────────────────────────

    public AssessmentQuestionsResponse getAssessmentQuestions(String subject, String grade, String targetScore) {
        String dbSubject = FormatUtil.mapSubjectToDb(subject);
        String dbGrade = FormatUtil.mapGradeToDb(grade);

        SubjectEntity subjectEntity = subjectRepository.findByNameAndGrade(dbSubject, dbGrade)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy môn " + subject + " lớp " + grade));

        int subjectId = subjectEntity.getId();
        int[] ratios = SCORE_RATIOS.getOrDefault(targetScore, new int[]{40, 40, 20});

        // ── Bước 1: Lấy toàn bộ câu hỏi của môn ───────────────────────────
        List<QuestionEntity> allPool = new ArrayList<>(questionRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("subjectId"), subjectId)));

        if (allPool.isEmpty()) {
            return AssessmentQuestionsResponse.builder()
                    .subject(subject).grade(grade).targetScore(targetScore)
                    .totalQuestions(0).questions(List.of()).build();
        }

        // ── Bước 2: Nhóm theo topic, xáo trộn trong mỗi topic ─────────────
        Map<Integer, List<QuestionEntity>> byTopic = new LinkedHashMap<>();
        for (QuestionEntity q : allPool) {
            Integer tid = q.getTopicId() != null ? q.getTopicId() : -1;
            byTopic.computeIfAbsent(tid, k -> new ArrayList<>()).add(q);
        }

        // Trong mỗi topic: sắp xếp theo độ khó ưu tiên, xáo trộn trong mỗi mức
        Map<String, Integer> levelPriority = new HashMap<>();
        // Mức có ratio cao nhất → ưu tiên chọn trước
        levelPriority.put("EASY", ratios[0]);
        levelPriority.put("MEDIUM", ratios[1]);
        levelPriority.put("HARD", ratios[2]);

        for (List<QuestionEntity> topicPool : byTopic.values()) {
            Collections.shuffle(topicPool); // ngẫu nhiên trước
            // Stable sort: đưa mức ưu tiên cao lên đầu
            topicPool.sort((a, b) ->
                levelPriority.getOrDefault(b.getLevel(), 0) -
                levelPriority.getOrDefault(a.getLevel(), 0));
        }

        // ── Bước 3: Pure round-robin — lấy 1 câu/topic mỗi vòng ───────────
        //   30 câu / 8 topics = vòng 1-3 lấy 8 câu (=24), vòng 4 lấy 6 câu (=30)
        //   Kết quả: 6 topic × 4 câu + 2 topic × 3 câu → chênh đúng 1
        List<List<QuestionEntity>> topicLists = new ArrayList<>(byTopic.values());
        Collections.shuffle(topicLists); // ngẫu nhiên thứ tự topic
        int[] cursors = new int[topicLists.size()];

        List<QuestionEntity> selected = new ArrayList<>();
        while (selected.size() < TOTAL_QUESTIONS) {
            boolean added = false;
            for (int t = 0; t < topicLists.size() && selected.size() < TOTAL_QUESTIONS; t++) {
                if (cursors[t] < topicLists.get(t).size()) {
                    selected.add(topicLists.get(t).get(cursors[t]++));
                    added = true;
                }
            }
            if (!added) break; // hết câu hỏi
        }

        Collections.shuffle(selected);

        // ── Bước 5: Map sang DTO ─────────────────────────────────────────────
        String[] labels = {"A", "B", "C", "D"};
        List<AssessmentQuestionsResponse.AssessmentQuestionDTO> dtos = new ArrayList<>();

        for (int i = 0; i < selected.size(); i++) {
            QuestionEntity q = selected.get(i);
            String topicName = q.getTopicId() != null
                    ? topicRepository.findById(q.getTopicId()).map(t -> t.getName()).orElse(null)
                    : null;

            List<AssessmentQuestionsResponse.OptionDTO> optionDTOs = new ArrayList<>();
            if (q.getOptions() != null) {
                int idx = 0;
                for (QuestionOptionEntity opt : q.getOptions()) {
                    optionDTOs.add(AssessmentQuestionsResponse.OptionDTO.builder()
                            .label(idx < labels.length ? labels[idx] : String.valueOf(idx))
                            .content(opt.getContent())
                            .correct(opt.isCorrect())
                            .build());
                    idx++;
                }
            }

            dtos.add(AssessmentQuestionsResponse.AssessmentQuestionDTO.builder()
                    .id(q.getId())
                    .orderNumber(i + 1)
                    .content(q.getContent())
                    .level(FormatUtil.mapLevelToFe(q.getLevel()))
                    .topicName(topicName)
                    .explanation(q.getExplanation())
                    .options(optionDTOs)
                    .build());
        }

        return AssessmentQuestionsResponse.builder()
                .subject(subject)
                .grade(grade)
                .targetScore(targetScore)
                .totalQuestions(dtos.size())
                .questions(dtos)
                .build();
    }

    // fetchRandom giữ lại để tương thích compile — không còn được gọi từ main flow
    @SuppressWarnings("unused")
    private List<QuestionEntity> fetchRandom(int subjectId, String level, int count) {
        if (count <= 0) return List.of();
        List<QuestionEntity> pool = new ArrayList<>(questionRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("subjectId"), subjectId));
            predicates.add(cb.equal(root.get("level"), level));
            return cb.and(predicates.toArray(new Predicate[0]));
        }));
        if (pool.isEmpty()) return List.of();
        Collections.shuffle(pool);
        return pool.subList(0, Math.min(count, pool.size()));
    }

    // ─── AI Phân tích kết quả ─────────────────────────────────────────────

    public String analyzeResult(AssessmentAnalyzeRequest request) {
        // Gom nhóm câu sai theo chủ đề (giảm dần)
        Map<String, Long> topicFailMap = request.getWrongQuestions().stream()
                .filter(w -> w.getTopicName() != null && !w.getTopicName().isBlank())
                .collect(Collectors.groupingBy(
                        AssessmentAnalyzeRequest.WrongQuestionDTO::getTopicName,
                        Collectors.counting()));

        String topicLines = topicFailMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> e.getKey() + ": sai " + e.getValue() + " câu")
                .collect(Collectors.joining(", "));

        // Gom nhóm câu sai theo độ khó
        Map<String, Long> levelFailMap = request.getWrongQuestions().stream()
                .collect(Collectors.groupingBy(
                        w -> w.getLevel() != null ? w.getLevel() : "Không rõ",
                        Collectors.counting()));

        String levelLines = levelFailMap.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + " câu)")
                .collect(Collectors.joining(", "));

        String prompt = String.format("""
                Dưới đây là kết quả kiểm tra của học sinh:
                - Môn: %s - %s
                - Mục tiêu: %s điểm
                - Điểm: %.1f
                - Số câu đúng: %d/%d
                - Chủ đề sai nhiều: %s
                - Sai theo mức độ: %s

                Hãy đóng vai một thầy/cô giáo có kinh nghiệm, viết nhận xét cho học sinh bằng giọng tự nhiên, gần gũi.

                Yêu cầu bắt buộc:
                1. Chỉ sử dụng tiếng Việt, tuyệt đối không dùng bất kỳ từ hoặc cụm từ tiếng Anh nào.
                2. Văn phong tự nhiên, giống lời nhận xét thật của giáo viên (không máy móc, không liệt kê cứng nhắc).
                3. Chỉ gồm đúng 3 phần, theo thứ tự:
                   **Nhận xét tổng quan**
                   **Kiến thức cần bổ sung**
                   **Lộ trình cải thiện**
                4. Không giải thích, không tự kiểm tra, không thêm ghi chú, không lặp lại đề bài.
                5. Không sử dụng ký hiệu đặc biệt hoặc markdown dư thừa (chỉ giữ tiêu đề in đậm như trên).

                Bắt đầu viết nhận xét ngay sau dòng [BAT_DAU] dưới đây:
                [BAT_DAU]
                """,
                request.getSubject(), request.getGrade(),
                request.getTargetScore(),
                request.getScore(), request.getCorrectCount(), request.getTotalQuestions(),
                topicLines.isBlank() ? "Chưa xác định" : topicLines,
                levelLines.isBlank() ? "Không có dữ liệu" : levelLines);

        try {
            String rawResponse = aiModel.generate(prompt);
            // Trích xuất chỉ phần nội dung tiếng Việt, bỏ phần AI "suy nghĩ" tiếng Anh ở
            // đầu
            return extractVietnameseSections(rawResponse);
        } catch (Exception e) {
            return "Không thể kết nối AI phân tích. Vui lòng thử lại sau.";
        }
    }

    /**
     * Trích xuất phần phân tích tiếng Việt từ response của AI,
     * bỏ qua mọi nội dung "chain-of-thought" tiếng Anh ở đầu.
     */
    private String extractVietnameseSections(String raw) {
        if (raw == null || raw.isBlank())
            return "AI không trả về kết quả.";

        // Các model nâng cao thường output nhiều lần (1 lần suy nghĩ, 1 lần nháp, 1 lần bản chính thức).
        // Phải lấy phiên bản cuối cùng xuất hiện trong text.
        int idx = raw.lastIndexOf("**Nhận xét tổng quan**");
        if (idx < 0) {
            idx = raw.lastIndexOf("Nhận xét tổng quan");
        }

        if (idx >= 0) {
            // Cắt phần text phụ kiện ở đằng trước (các dấu *, space, v.v)
            int boldIdx = raw.lastIndexOf("**", idx);
            if (boldIdx >= 0 && boldIdx >= idx - 5) {
                return raw.substring(boldIdx).trim();
            }
            return raw.substring(idx).trim();
        }

        return raw.trim();
    }
}
