package com.cuong.backend.repository;

import com.cuong.backend.entity.LearningActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LearningActivityRepository extends JpaRepository<LearningActivityEntity, Long> {

    // Lấy tất cả ngày học của user, sắp xếp mới nhất trước
    List<LearningActivityEntity> findByUserIdOrderByActivityDateDesc(Long userId);

    // Kiểm tra đã check-in ngày cụ thể chưa
    Optional<LearningActivityEntity> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);
}
