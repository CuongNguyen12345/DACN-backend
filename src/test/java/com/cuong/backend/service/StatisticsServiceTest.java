package com.cuong.backend.service;

import com.cuong.backend.model.response.DashboardOverview;
import com.cuong.backend.model.response.ScoreDistributionItem;
import com.cuong.backend.model.response.TopStudentItem;
import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.ReportResultRepository;
import com.cuong.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsServiceTest {

    @Test
    void getOverviewUsesSubmittedAtPeriodsAndCalculatesPercentChanges() {
        ReportResultRepository reportResultRepository = mock(ReportResultRepository.class);
        ExamRepository examRepository = mock(ExamRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        StatisticsService service = new StatisticsService(
                reportResultRepository,
                examRepository,
                userRepository
        );

        when(userRepository.countByRole("STUDENT")).thenReturn(10L);
        when(examRepository.count()).thenReturn(4L);
        when(reportResultRepository.countDistinctUsersByPeriod(any(), any()))
                .thenReturn(6L)
                .thenReturn(3L);
        when(reportResultRepository.countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(any(), any()))
                .thenReturn(20L)
                .thenReturn(10L);
        when(reportResultRepository.averageScoreByPeriod(any(), any()))
                .thenReturn(7.5)
                .thenReturn(5.0);

        DashboardOverview result = service.getOverview(LocalDate.of(2026, 5, 1));

        assertEquals(10L, result.getTotalStudents());
        assertEquals(4L, result.getActiveExams());
        assertEquals(20L, result.getMonthlyAttempts());
        assertEquals(7.5, result.getAverageScore());
        assertEquals(100, result.getPercentChanges().getStudents());
        assertEquals(100, result.getPercentChanges().getMonthlyAttempts());
        assertEquals(50, result.getPercentChanges().getAverageScore());
        assertEquals(0, result.getPercentChanges().getActiveExams());
    }

    @Test
    void getScoreDistributionGroupsScoresByRanges() {
        ReportResultRepository reportResultRepository = mock(ReportResultRepository.class);
        StatisticsService service = new StatisticsService(
                reportResultRepository,
                mock(ExamRepository.class),
                mock(UserRepository.class)
        );

        when(reportResultRepository.findScoresByPeriod(any(), any()))
                .thenReturn(List.of(9.0, 7.0, 6.0, 4.0));

        List<ScoreDistributionItem> result = service.getScoreDistribution(LocalDate.of(2026, 5, 1));

        assertEquals(4, result.size());
        assertEquals("Gioi (8.0 - 10)", result.get(0).getRange());
        assertEquals(25, result.get(0).getPercent());
        assertEquals(1L, result.get(0).getCount());
        assertEquals("Yeu (< 5.0)", result.get(3).getRange());
        assertEquals(25, result.get(3).getPercent());
    }

    @Test
    void getTopStudentsMapsRepositoryRows() {
        ReportResultRepository reportResultRepository = mock(ReportResultRepository.class);
        StatisticsService service = new StatisticsService(
                reportResultRepository,
                mock(ExamRepository.class),
                mock(UserRepository.class)
        );

        when(reportResultRepository.findTopStudentsByPeriod(any(), any(), any(Pageable.class)))
                .thenReturn(List.<Object[]>of(new Object[]{7L, "Nguyen Van A", "Lop 12", 8.75, 3L}));

        List<TopStudentItem> result = service.getTopStudents(LocalDate.of(2026, 5, 1), 5);

        assertEquals(1, result.size());
        assertEquals(7L, result.get(0).getId());
        assertEquals("Nguyen Van A", result.get(0).getName());
        assertEquals("Lop 12", result.get(0).getGrade());
        assertEquals(8.75, result.get(0).getScore());
        assertEquals(3L, result.get(0).getExams());
        verify(reportResultRepository).findTopStudentsByPeriod(any(), any(), any(Pageable.class));
    }
}
