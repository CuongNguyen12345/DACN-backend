package com.cuong.backend.repository;

import com.cuong.backend.entity.ShopItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopItemRepository extends JpaRepository<ShopItemEntity, Long> {
    long countByActiveTrue();

    List<ShopItemEntity> findAllByOrderByTypeAscPriceAsc();

    List<ShopItemEntity> findByActiveTrueOrderByTypeAscPriceAsc();

    Optional<ShopItemEntity> findByIdAndActiveTrue(long id);
}
