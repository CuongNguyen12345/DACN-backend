package com.cuong.backend.repository;

import com.cuong.backend.entity.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {

    @Query("""
                SELECT e FROM ExamEntity e
                WHERE (:subjectId IS NULL OR e.subjectId = :subjectId)
                  AND (:grade IS NULL OR e.grade = :grade)
                  AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    List<ExamEntity> searchExam(
            @Param("subjectId") Long subjectId,
            @Param("grade") Integer grade,
            @Param("keyword") String keyword);
}