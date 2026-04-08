package com.cuong.backend.repository;

import com.cuong.backend.entity.QuestionOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, Long> {
}
