package com.cuong.backend.repository;

import com.cuong.backend.entity.ExamResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResultEntity, Long> {
    List<ExamResultEntity> findByUserIdOrderBySubmittedAtDesc(long userId);

    Optional<ExamResultEntity> findByIdAndUserId(long id, long userId);
}
