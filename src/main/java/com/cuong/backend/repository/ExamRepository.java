package com.cuong.backend.repository;

import com.cuong.backend.entity.ExamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ExamRepository extends JpaRepository<ExamEntity, Long>, JpaSpecificationExecutor<ExamEntity> {
    @Modifying
    @Transactional
    @Query("UPDATE ExamEntity e SET e.attemptCount = e.attemptCount + 1 WHERE e.id = :id")
    void incrementAttemptCount(@Param("id") Long id);
}
