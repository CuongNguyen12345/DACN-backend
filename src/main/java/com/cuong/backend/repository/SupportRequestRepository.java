package com.cuong.backend.repository;

import com.cuong.backend.entity.SupportRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequestEntity, Long> {
    List<SupportRequestEntity> findByType(String type);
    List<SupportRequestEntity> findByTypeOrderByCreatedAtDesc(String type);
    List<SupportRequestEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<SupportRequestEntity> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, String type);
}
