package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChapterResponseDTO {
    private int id;
    private String chapterName;
    private List<LessonResponseDTO> lessons;
}
