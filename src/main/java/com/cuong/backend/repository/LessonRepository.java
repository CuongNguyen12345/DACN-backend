package com.cuong.backend.repository;

import com.cuong.backend.entity.LessonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<LessonEntity, Integer>, JpaSpecificationExecutor<LessonEntity> {
    List<LessonEntity> findByChapterId(int chapterId);
}
