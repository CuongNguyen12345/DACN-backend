package com.cuong.backend.repository;

import com.cuong.backend.entity.StudyActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudyActivityRepository extends JpaRepository<StudyActivityEntity, Long> {
    boolean existsByUserIdAndStudyDate(long userId, LocalDate studyDate);

    List<StudyActivityEntity> findByUserIdOrderByStudyDateAsc(long userId);
}
