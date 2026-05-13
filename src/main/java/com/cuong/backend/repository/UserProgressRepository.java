package com.cuong.backend.repository;

import com.cuong.backend.entity.UserProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgressEntity, Integer> {

    /** Lấy danh sách lesson_id đã hoàn thành của user trong một môn học. */
    @Query("""
            SELECT up.lessonId FROM UserProgressEntity up
            WHERE up.userId = :userId AND up.isCompleted = true
            AND up.lessonId IN :lessonIds
            """)
    List<Integer> findCompletedLessonIds(@Param("userId") long userId,
                                         @Param("lessonIds") List<Integer> lessonIds);

    /** Tìm bản ghi tiến độ cụ thể của một user với một lesson. */
    Optional<UserProgressEntity> findFirstByUserIdAndLessonIdOrderByIdAsc(long userId, int lessonId);

    @Query("""
            SELECT up FROM UserProgressEntity up
            WHERE up.userId = :userId AND up.bookmarked = true
            ORDER BY up.bookmarkedAt DESC
            """)
    List<UserProgressEntity> findBookmarkedByUserIdOrderByBookmarkedAtDesc(@Param("userId") long userId);
}
