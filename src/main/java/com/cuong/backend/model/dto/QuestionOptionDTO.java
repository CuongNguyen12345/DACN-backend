package com.cuong.backend.model.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionOptionDTO {
    Integer id;
    Integer questionId;
    String content;
    boolean isCorrect;
}
