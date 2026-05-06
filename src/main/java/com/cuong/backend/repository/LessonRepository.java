package com.cuong.backend.repository;

import com.cuong.backend.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Integer>, JpaSpecificationExecutor<LessonEntity> {

    List<LessonEntity> findByChapterId(int chapterId);

    /**
     * Tìm kiếm bài học theo keyword (tên bài), subject name và grade.
     * Join qua bảng chapters để lấy subject_id, sau đó join subjects.
     */
    @Query("""
            SELECT l FROM LessonEntity l
            JOIN ChapterEntity c ON l.chapterId = c.id
            JOIN SubjectEntity s ON c.subjectId = s.id
            WHERE (:keyword IS NULL OR LOWER(l.lessonName) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:subjectName IS NULL OR s.name = :subjectName)
              AND (:grade IS NULL OR s.grade = :grade)
            ORDER BY s.grade ASC, s.name ASC, c.orderNumber ASC, l.id ASC
            """)
    List<LessonEntity> searchLessons(
            @Param("keyword") String keyword,
            @Param("subjectName") String subjectName,
            @Param("grade") String grade);
}

