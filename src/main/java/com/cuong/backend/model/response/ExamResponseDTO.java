package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamResponseDTO {
    private long id;
    private String title;
    private String subject;  // tên môn đã map FE
    private int duration;
    private int questionCount;
}
