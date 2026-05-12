package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudyActivityResponse {
    private int currentStreak;
    private int totalStudyDays;
    private List<String> studyDates;
}
