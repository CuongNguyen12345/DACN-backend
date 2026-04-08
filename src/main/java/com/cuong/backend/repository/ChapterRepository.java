package com.cuong.backend.repository;

import com.cuong.backend.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<ChapterEntity, Integer> {
    List<ChapterEntity> findBySubjectIdOrderByOrderNumberAsc(int subjectId);
}
