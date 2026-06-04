package com.cuong.backend.repository;

import com.cuong.backend.entity.UserInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInventoryRepository extends JpaRepository<UserInventoryEntity, Long> {
    boolean existsByUserIdAndItemId(long userId, long itemId);

    Optional<UserInventoryEntity> findByUserIdAndItemId(long userId, long itemId);

    List<UserInventoryEntity> findByUserId(long userId);

    @Query("SELECT ui.itemId FROM UserInventoryEntity ui WHERE ui.userId = :userId")
    List<Long> findItemIdsByUserId(@Param("userId") long userId);
}
