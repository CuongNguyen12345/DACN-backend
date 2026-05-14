package com.cuong.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverview {
    private long totalStudents;
    private long activeExams;
    private long monthlyAttempts;
    private double averageScore;
    private PercentChanges percentChanges;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PercentChanges {
        private int students;
        private int activeExams;
        private int monthlyAttempts;
        private int averageScore;
    }
}
