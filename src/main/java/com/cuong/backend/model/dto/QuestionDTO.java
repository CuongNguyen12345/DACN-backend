package com.cuong.backend.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionDTO {
    int id;
    int subjectId;
    String content;
    String explanation;
    String level; // 'BASIC', 'MEDIUM', 'HARD'
    LocalDateTime createdAt;
    List<QuestionOptionDTO> options;
}
