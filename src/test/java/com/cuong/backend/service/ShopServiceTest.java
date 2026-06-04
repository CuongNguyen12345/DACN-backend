package com.cuong.backend.service;

import com.cuong.backend.entity.CoinTransactionEntity;
import com.cuong.backend.entity.ShopItemEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.entity.UserInventoryEntity;
import com.cuong.backend.model.request.ShopItemRequest;
import com.cuong.backend.model.response.CoinRewardResponse;
import com.cuong.backend.model.response.PurchaseResponse;
import com.cuong.backend.model.response.ShopItemResponse;
import com.cuong.backend.repository.CoinTransactionRepository;
import com.cuong.backend.repository.ShopItemRepository;
import com.cuong.backend.repository.UserInventoryRepository;
import com.cuong.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShopServiceTest {

    @Test
    void createAdminShopItemNormalizesTypeAndSavesActiveCatalogItem() {
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        ShopService service = new ShopService(
                mock(UserRepository.class),
                shopItemRepository,
                mock(UserInventoryRepository.class),
                mock(CoinTransactionRepository.class)
        );

        ShopItemRequest request = new ShopItemRequest();
        request.setName("Avatar Galaxy");
        request.setDescription("Avatar for top learners");
        request.setType("avatar");
        request.setPrice(70);
        request.setAssetUrl("https://example.com/avatar.svg");
        request.setAccentColor("#7c3aed");
        request.setActive(true);

        when(shopItemRepository.save(any(ShopItemEntity.class))).thenAnswer(invocation -> {
            ShopItemEntity saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });

        ShopItemResponse response = service.createShopItem(request);

        assertEquals(12L, response.getId());
        assertEquals("Avatar Galaxy", response.getName());
        assertEquals("AVATAR", response.getType());
        assertEquals(70, response.getPrice());
        assertTrue(response.isActive());

        ArgumentCaptor<ShopItemEntity> captor = ArgumentCaptor.forClass(ShopItemEntity.class);
        verify(shopItemRepository).save(captor.capture());
        assertEquals("AVATAR", captor.getValue().getType());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void updateAdminShopItemKeepsIdAndAppliesNewCatalogFields() {
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        ShopService service = new ShopService(
                mock(UserRepository.class),
                shopItemRepository,
                mock(UserInventoryRepository.class),
                mock(CoinTransactionRepository.class)
        );

        ShopItemEntity existing = new ShopItemEntity();
        existing.setId(12L);
        existing.setName("Old");
        existing.setType("AVATAR");
        existing.setPrice(30);
        existing.setActive(true);

        ShopItemRequest request = new ShopItemRequest();
        request.setName("Khung Kim Cuong");
        request.setDescription("Decor frame");
        request.setType("DECORATION");
        request.setPrice(90);
        request.setAssetUrl("");
        request.setAccentColor("#06b6d4");
        request.setActive(false);

        when(shopItemRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(shopItemRepository.save(existing)).thenReturn(existing);

        ShopItemResponse response = service.updateShopItem(12L, request);

        assertEquals(12L, response.getId());
        assertEquals("Khung Kim Cuong", response.getName());
        assertEquals("DECORATION", response.getType());
        assertEquals(90, response.getPrice());
        assertFalse(response.isActive());
        assertEquals("#06b6d4", existing.getAccentColor());
    }

    @Test
    void deactivateAdminShopItemSoftDeletesWithoutRemovingTheRow() {
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        ShopService service = new ShopService(
                mock(UserRepository.class),
                shopItemRepository,
                mock(UserInventoryRepository.class),
                mock(CoinTransactionRepository.class)
        );

        ShopItemEntity existing = new ShopItemEntity();
        existing.setId(12L);
        existing.setName("Tim tiep suc");
        existing.setType("LIFE");
        existing.setPrice(25);
        existing.setActive(true);

        when(shopItemRepository.findById(12L)).thenReturn(Optional.of(existing));
        when(shopItemRepository.save(existing)).thenReturn(existing);

        service.deactivateShopItem(12L);

        assertFalse(existing.isActive());
        verify(shopItemRepository).save(existing);
        verify(shopItemRepository, never()).delete(existing);
    }

    @Test
    void getAdminShopItemsReturnsActiveAndInactiveItems() {
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        ShopService service = new ShopService(
                mock(UserRepository.class),
                shopItemRepository,
                mock(UserInventoryRepository.class),
                mock(CoinTransactionRepository.class)
        );

        ShopItemEntity active = new ShopItemEntity();
        active.setId(1L);
        active.setName("Avatar A");
        active.setType("AVATAR");
        active.setPrice(20);
        active.setActive(true);

        ShopItemEntity inactive = new ShopItemEntity();
        inactive.setId(2L);
        inactive.setName("Khung B");
        inactive.setType("DECORATION");
        inactive.setPrice(40);
        inactive.setActive(false);

        when(shopItemRepository.findAllByOrderByTypeAscPriceAsc()).thenReturn(List.of(active, inactive));

        List<ShopItemResponse> items = service.getAdminShopItems();

        assertEquals(2, items.size());
        assertTrue(items.get(0).isActive());
        assertFalse(items.get(1).isActive());
    }

    @Test
    void rewardLessonCompletionAddsCoinsOnlyOnceForTheSameLesson() {
        UserRepository userRepository = mock(UserRepository.class);
        CoinTransactionRepository coinTransactionRepository = mock(CoinTransactionRepository.class);
        ShopService service = new ShopService(
                userRepository,
                mock(ShopItemRepository.class),
                mock(UserInventoryRepository.class),
                coinTransactionRepository
        );

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setCoinBalance(40);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(coinTransactionRepository.existsByUserIdAndSourceTypeAndSourceId(7L, "LESSON", "11"))
                .thenReturn(false)
                .thenReturn(true);

        CoinRewardResponse first = service.rewardLessonCompletion(7L, 11);
        CoinRewardResponse second = service.rewardLessonCompletion(7L, 11);

        assertEquals(10, first.getCoinsEarned());
        assertEquals(50, first.getCoinBalance());
        assertTrue(first.isRewarded());
        assertEquals(0, second.getCoinsEarned());
        assertEquals(50, second.getCoinBalance());
        assertFalse(second.isRewarded());
        verify(userRepository, times(1)).save(user);
        verify(coinTransactionRepository, times(1)).save(any(CoinTransactionEntity.class));
    }

    @Test
    void rewardQuizGivesCoinsOnlyWhenPassedAndOnlyOnce() {
        UserRepository userRepository = mock(UserRepository.class);
        CoinTransactionRepository coinTransactionRepository = mock(CoinTransactionRepository.class);
        ShopService service = new ShopService(
                userRepository,
                mock(ShopItemRepository.class),
                mock(UserInventoryRepository.class),
                coinTransactionRepository
        );

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setCoinBalance(5);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(coinTransactionRepository.existsByUserIdAndSourceTypeAndSourceId(7L, "QUIZ", "9"))
                .thenReturn(false)
                .thenReturn(true);

        CoinRewardResponse failed = service.rewardQuizSubmission(7L, 9L, false, 40);
        CoinRewardResponse passed = service.rewardQuizSubmission(7L, 9L, true, 80);
        CoinRewardResponse duplicate = service.rewardQuizSubmission(7L, 9L, true, 90);

        assertFalse(failed.isRewarded());
        assertEquals(0, failed.getCoinsEarned());
        assertTrue(passed.isRewarded());
        assertEquals(20, passed.getCoinsEarned());
        assertEquals(25, passed.getCoinBalance());
        assertFalse(duplicate.isRewarded());
        assertEquals(0, duplicate.getCoinsEarned());
        assertEquals(25, duplicate.getCoinBalance());
    }

    @Test
    void purchaseItemDeductsCoinsAndCreatesInventory() {
        UserRepository userRepository = mock(UserRepository.class);
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        UserInventoryRepository userInventoryRepository = mock(UserInventoryRepository.class);
        CoinTransactionRepository coinTransactionRepository = mock(CoinTransactionRepository.class);
        ShopService service = new ShopService(
                userRepository,
                shopItemRepository,
                userInventoryRepository,
                coinTransactionRepository
        );

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setCoinBalance(100);

        ShopItemEntity item = new ShopItemEntity();
        item.setId(3L);
        item.setName("Avatar Sao băng");
        item.setType("AVATAR");
        item.setPrice(35);
        item.setActive(true);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(shopItemRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(item));
        when(userInventoryRepository.existsByUserIdAndItemId(7L, 3L)).thenReturn(false);

        PurchaseResponse response = service.purchaseItem(7L, 3L);

        assertEquals(65, response.getCoinBalance());
        assertTrue(response.isOwned());
        assertEquals("Avatar Sao băng", response.getItem().getName());

        ArgumentCaptor<UserInventoryEntity> inventoryCaptor = ArgumentCaptor.forClass(UserInventoryEntity.class);
        verify(userInventoryRepository).save(inventoryCaptor.capture());
        assertEquals(7L, inventoryCaptor.getValue().getUserId());
        assertEquals(3L, inventoryCaptor.getValue().getItemId());
        verify(userRepository).save(user);
    }

    @Test
    void purchaseItemRejectsDuplicateOrInsufficientBalance() {
        UserRepository userRepository = mock(UserRepository.class);
        ShopItemRepository shopItemRepository = mock(ShopItemRepository.class);
        UserInventoryRepository userInventoryRepository = mock(UserInventoryRepository.class);
        ShopService service = new ShopService(
                userRepository,
                shopItemRepository,
                userInventoryRepository,
                mock(CoinTransactionRepository.class)
        );

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setCoinBalance(10);

        ShopItemEntity item = new ShopItemEntity();
        item.setId(3L);
        item.setName("Khung vàng");
        item.setType("DECORATION");
        item.setPrice(35);
        item.setActive(true);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(shopItemRepository.findByIdAndActiveTrue(3L)).thenReturn(Optional.of(item));
        when(userInventoryRepository.existsByUserIdAndItemId(7L, 3L)).thenReturn(false);

        RuntimeException lowBalance = assertThrows(RuntimeException.class, () -> service.purchaseItem(7L, 3L));
        assertTrue(lowBalance.getMessage().contains("Không đủ xu"));

        user.setCoinBalance(100);
        when(userInventoryRepository.existsByUserIdAndItemId(7L, 3L)).thenReturn(true);

        RuntimeException duplicate = assertThrows(RuntimeException.class, () -> service.purchaseItem(7L, 3L));
        assertTrue(duplicate.getMessage().contains("đã sở hữu"));
    }
}
