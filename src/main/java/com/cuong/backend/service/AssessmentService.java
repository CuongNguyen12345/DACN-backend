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
        int[] ratios = SCORE_RATIOS.getOrDefault(targetScore, new int[] { 40, 40, 20 });

        int easyCount = (int) Math.round(TOTAL_QUESTIONS * ratios[0] / 100.0);
        int mediumCount = (int) Math.round(TOTAL_QUESTIONS * ratios[1] / 100.0);
        int hardCount = TOTAL_QUESTIONS - easyCount - mediumCount;

        List<QuestionEntity> easyQs = fetchRandom(subjectId, "EASY", easyCount);
        List<QuestionEntity> mediumQs = fetchRandom(subjectId, "MEDIUM", mediumCount);
        List<QuestionEntity> hardQs = fetchRandom(subjectId, "HARD", hardCount);

        List<QuestionEntity> all = new ArrayList<>();
        all.addAll(easyQs);
        all.addAll(mediumQs);
        all.addAll(hardQs);
        Collections.shuffle(all);

        String[] labels = { "A", "B", "C", "D" };
        List<AssessmentQuestionsResponse.AssessmentQuestionDTO> dtos = new ArrayList<>();

        for (int i = 0; i < all.size(); i++) {
            QuestionEntity q = all.get(i);
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

    private List<QuestionEntity> fetchRandom(int subjectId, String level, int count) {
        if (count <= 0)
            return List.of();

        List<QuestionEntity> pool = questionRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("subjectId"), subjectId));
            predicates.add(cb.equal(root.get("level"), level));
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        if (pool.isEmpty())
            return List.of();

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
