package com.cuong.backend.repository;

import com.cuong.backend.entity.SupportMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessageEntity, Long> {
    List<SupportMessageEntity> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
