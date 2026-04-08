package com.cuong.backend.repository;

import com.cuong.backend.entity.QuestionOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOptionEntity, Long> {
    @Modifying
    @Query("DELETE FROM QuestionOptionEntity qo WHERE qo.question.id = :questionId")
    void deleteByQuestionId(Long questionId);
}
