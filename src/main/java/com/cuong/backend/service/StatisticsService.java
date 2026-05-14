package com.cuong.backend.service;

import com.cuong.backend.model.response.DashboardOverview;
import com.cuong.backend.model.response.ScoreDistributionItem;
import com.cuong.backend.model.response.TopStudentItem;
import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.ReportResultRepository;
import com.cuong.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final ReportResultRepository reportResultRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            ReportResultRepository reportResultRepository,
            ExamRepository examRepository,
            UserRepository userRepository) {
        this.reportResultRepository = reportResultRepository;
        this.examRepository = examRepository;
        this.userRepository = userRepository;
    }

    public DashboardOverview getOverview(LocalDate month) {
        PeriodRange current = getMonthRange(month);
        PeriodRange previous = getMonthRange(month.minusMonths(1));

        long totalStudents = userRepository.countByRole("STUDENT");
        long activeExams = examRepository.count();
        long monthlyAttempts = reportResultRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                current.start(),
                current.end()
        );
        double averageScore = roundOneDecimal(reportResultRepository.averageScoreByPeriod(
                current.start(),
                current.end()
        ));

        long currentStudents = reportResultRepository.countDistinctUsersByPeriod(current.start(), current.end());
        long previousStudents = reportResultRepository.countDistinctUsersByPeriod(previous.start(), previous.end());
        long previousAttempts = reportResultRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(
                previous.start(),
                previous.end()
        );
        double previousAverageScore = reportResultRepository.averageScoreByPeriod(previous.start(), previous.end());

        return new DashboardOverview(
                totalStudents,
                activeExams,
                monthlyAttempts,
                averageScore,
                new DashboardOverview.PercentChanges(
                        percentChange(currentStudents, previousStudents),
                        0,
                        percentChange(monthlyAttempts, previousAttempts),
                        percentChange(averageScore, previousAverageScore)
                )
        );
    }

    public List<ScoreDistributionItem> getScoreDistribution(LocalDate month) {
        PeriodRange range = getMonthRange(month);
        List<Double> scores = reportResultRepository.findScoresByPeriod(range.start(), range.end());
        long total = scores.size();

        Map<String, Long> rangeCounts = new LinkedHashMap<>();
        rangeCounts.put("Gioi (8.0 - 10)", scores.stream().filter(score -> score >= 8.0).count());
        rangeCounts.put("Kha (6.5 - 7.9)", scores.stream().filter(score -> score >= 6.5 && score < 8.0).count());
        rangeCounts.put("Trung binh (5.0 - 6.4)", scores.stream().filter(score -> score >= 5.0 && score < 6.5).count());
        rangeCounts.put("Yeu (< 5.0)", scores.stream().filter(score -> score < 5.0).count());

        String[] colors = {"bg-emerald-500", "bg-blue-500", "bg-amber-500", "bg-red-500"};
        List<ScoreDistributionItem> items = new ArrayList<>();
        int colorIndex = 0;
        for (Map.Entry<String, Long> entry : rangeCounts.entrySet()) {
            long count = entry.getValue();
            int percent = total == 0 ? 0 : (int) Math.round((count * 100.0) / total);
            items.add(new ScoreDistributionItem(entry.getKey(), percent, count, colors[colorIndex++]));
        }
        return items;
    }

    public List<TopStudentItem> getTopStudents(LocalDate month, int limit) {
        PeriodRange range = getMonthRange(month);
        int safeLimit = Math.max(1, Math.min(limit, 20));

        return reportResultRepository.findTopStudentsByPeriod(
                        range.start(),
                        range.end(),
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(this::toTopStudentItem)
                .collect(Collectors.toList());
    }

    private TopStudentItem toTopStudentItem(Object[] row) {
        return new TopStudentItem(
                ((Number) row[0]).longValue(),
                row[1] == null ? "Hoc vien" : String.valueOf(row[1]),
                row[2] == null ? "" : String.valueOf(row[2]),
                ((Number) row[3]).doubleValue(),
                ((Number) row[4]).longValue()
        );
    }

    private PeriodRange getMonthRange(LocalDate month) {
        LocalDate firstDay = month.withDayOfMonth(1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDay.plusMonths(1).atStartOfDay();
        return new PeriodRange(toDate(start), toDate(end));
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private int percentChange(double current, double previous) {
        if (previous == 0) {
            return current == 0 ? 0 : 100;
        }
        return (int) Math.round(((current - previous) / previous) * 100);
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record PeriodRange(Date start, Date end) {
    }
}
