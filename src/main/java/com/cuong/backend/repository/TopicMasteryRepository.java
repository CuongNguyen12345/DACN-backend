package com.cuong.backend.repository;

import com.cuong.backend.entity.TopicMasteryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicMasteryRepository extends JpaRepository<TopicMasteryEntity, Long> {
    Optional<TopicMasteryEntity> findByUserIdAndTopicId(long userId, int topicId);
    List<TopicMasteryEntity> findByUserId(long userId);
}
