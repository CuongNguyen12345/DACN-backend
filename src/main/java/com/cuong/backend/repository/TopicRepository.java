package com.cuong.backend.repository;

import com.cuong.backend.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Integer> {
    List<TopicEntity> findBySubjectId(int subjectId);
    Optional<TopicEntity> findByNameAndSubjectId(String name, int subjectId);
}
