package com.cuong.backend.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateChapterRequest {
    private String subjectName;
    private String grade;
    private String chapterName;
    private int orderNumber;
}
