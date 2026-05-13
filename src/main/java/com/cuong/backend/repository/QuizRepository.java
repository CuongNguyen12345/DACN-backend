package com.cuong.backend.repository;

import com.cuong.backend.entity.QuizEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<QuizEntity, Long> {
    List<QuizEntity> findByLessonIdIn(Collection<Integer> lessonIds);
    List<QuizEntity> findByTopicIdIn(Collection<Integer> topicIds);
}
