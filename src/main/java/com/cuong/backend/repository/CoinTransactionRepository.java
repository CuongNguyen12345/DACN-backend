package com.cuong.backend.repository;

import com.cuong.backend.entity.CoinTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoinTransactionRepository extends JpaRepository<CoinTransactionEntity, Long> {
    boolean existsByUserIdAndSourceTypeAndSourceId(long userId, String sourceType, String sourceId);

    List<CoinTransactionEntity> findByUserIdOrderByCreatedAtDesc(long userId);
}
