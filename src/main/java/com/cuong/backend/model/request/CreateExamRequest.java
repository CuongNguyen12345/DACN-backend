package com.cuong.backend.model.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateExamRequest {
    private String title;
    private String subject;   // "Toán", "Vật Lý", ...
    private String grade;     // "Lớp 10", "Lớp 11", ...
    private int duration;     // phút
    private int totalQuestions;
    private String description;
    // Danh sách ID câu hỏi từ ngân hàng (dạng "Q-123" hoặc số)
    private List<String> questionIds;
}
