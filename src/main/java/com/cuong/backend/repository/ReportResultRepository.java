package com.cuong.backend.repository;

import com.cuong.backend.entity.ExamResultEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReportResultRepository extends JpaRepository<ExamResultEntity, Long> {

    @Query("""
            SELECT COUNT(DISTINCT r.userId)
            FROM ExamResultEntity r
            WHERE r.submittedAt >= :start AND r.submittedAt < :end
            """)
    long countDistinctUsersByPeriod(@Param("start") Date start, @Param("end") Date end);

    long countBySubmittedAtGreaterThanEqualAndSubmittedAtLessThan(Date start, Date end);

    @Query("""
            SELECT COALESCE(AVG(r.score), 0)
            FROM ExamResultEntity r
            WHERE r.submittedAt >= :start AND r.submittedAt < :end
            """)
    double averageScoreByPeriod(@Param("start") Date start, @Param("end") Date end);

    @Query("""
            SELECT r.score
            FROM ExamResultEntity r
            WHERE r.submittedAt >= :start AND r.submittedAt < :end
            """)
    List<Double> findScoresByPeriod(@Param("start") Date start, @Param("end") Date end);

    @Query("""
            SELECT r.userId, u.userName, u.grade, AVG(r.score), COUNT(r.id)
            FROM ExamResultEntity r
            JOIN UserEntity u ON u.id = r.userId
            WHERE r.submittedAt >= :start AND r.submittedAt < :end
            GROUP BY r.userId, u.userName, u.grade
            ORDER BY AVG(r.score) DESC, COUNT(r.id) DESC
            """)
    List<Object[]> findTopStudentsByPeriod(
            @Param("start") Date start,
            @Param("end") Date end,
            Pageable pageable);
}
