package com.cuong.backend.repository;

import com.cuong.backend.entity.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamRepository extends JpaRepository<ExamEntity, Long> {

        @Query("""
                            SELECT e FROM ExamEntity e
                            WHERE e.id IN (
                                SELECT MIN(e2.id) FROM ExamEntity e2
                                WHERE (:subject IS NULL OR LOWER(e2.subject) LIKE LOWER(CONCAT('%', :subject, '%')))
                                  AND (:grade IS NULL OR e2.grade = :grade)
                                  AND (:keyword IS NULL OR LOWER(e2.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
                                GROUP BY e2.title, e2.subject, e2.grade
                            )
                        """)
        List<ExamEntity> searchExam(
                        @Param("subject") String subject,
                        @Param("grade") Integer grade,
                        @Param("keyword") String keyword);

}