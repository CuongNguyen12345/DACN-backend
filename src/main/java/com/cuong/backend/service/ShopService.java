package com.cuong.backend.service;

import com.cuong.backend.entity.CoinTransactionEntity;
import com.cuong.backend.entity.ShopItemEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.entity.UserInventoryEntity;
import com.cuong.backend.model.request.ShopItemRequest;
import com.cuong.backend.model.response.CoinRewardResponse;
import com.cuong.backend.model.response.PurchaseResponse;
import com.cuong.backend.model.response.ShopItemResponse;
import com.cuong.backend.model.response.ShopStatusResponse;
import com.cuong.backend.repository.CoinTransactionRepository;
import com.cuong.backend.repository.ShopItemRepository;
import com.cuong.backend.repository.UserInventoryRepository;
import com.cuong.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private static final int LESSON_REWARD = 10;
    private static final int QUIZ_REWARD = 20;

    private final UserRepository userRepository;
    private final ShopItemRepository shopItemRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final CoinTransactionRepository coinTransactionRepository;

    public ShopService(
            UserRepository userRepository,
            ShopItemRepository shopItemRepository,
            UserInventoryRepository userInventoryRepository,
            CoinTransactionRepository coinTransactionRepository) {
        this.userRepository = userRepository;
        this.shopItemRepository = shopItemRepository;
        this.userInventoryRepository = userInventoryRepository;
        this.coinTransactionRepository = coinTransactionRepository;
    }

    @Transactional
    public ShopStatusResponse getShopStatus(long userId) {
        UserEntity user = getUser(userId);
        seedDefaultItemsIfNeeded();

        List<ShopItemEntity> items = shopItemRepository.findByActiveTrueOrderByTypeAscPriceAsc();
        Map<Long, UserInventoryEntity> inventoryByItemId = userInventoryRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(UserInventoryEntity::getItemId, Function.identity(), (first, ignored) -> first));

        return ShopStatusResponse.builder()
                .coinBalance(getCoinBalance(user))
                .items(items.stream()
                        .map(item -> toItemResponse(item, inventoryByItemId.get(item.getId())))
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ShopItemResponse> getAdminShopItems() {
        return shopItemRepository.findAllByOrderByTypeAscPriceAsc().stream()
                .map(item -> toItemResponse(item, null))
                .toList();
    }

    @Transactional
    public ShopItemResponse createShopItem(ShopItemRequest request) {
        ShopItemEntity item = new ShopItemEntity();
        applyShopItemFields(item, request);
        return toItemResponse(shopItemRepository.save(item), null);
    }

    @Transactional
    public ShopItemResponse updateShopItem(long itemId, ShopItemRequest request) {
        ShopItemEntity item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm trong shop."));
        applyShopItemFields(item, request);
        return toItemResponse(shopItemRepository.save(item), null);
    }

    @Transactional
    public void deactivateShopItem(long itemId) {
        ShopItemEntity item = shopItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm trong shop."));
        item.setActive(false);
        shopItemRepository.save(item);
    }

    @Transactional
    public PurchaseResponse purchaseItem(long userId, long itemId) {
        UserEntity user = getUser(userId);
        ShopItemEntity item = shopItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm trong shop."));

        if (userInventoryRepository.existsByUserIdAndItemId(userId, itemId)) {
            throw new RuntimeException("Bạn đã sở hữu vật phẩm này.");
        }

        int balance = getCoinBalance(user);
        if (balance < item.getPrice()) {
            throw new RuntimeException("Không đủ xu để mua vật phẩm này.");
        }

        user.setCoinBalance(balance - item.getPrice());
        userRepository.save(user);

        UserInventoryEntity inventory = new UserInventoryEntity();
        inventory.setUserId(userId);
        inventory.setItemId(itemId);
        userInventoryRepository.save(inventory);

        saveTransaction(userId, -item.getPrice(), "PURCHASE", String.valueOf(itemId), "Mua " + item.getName());

        return PurchaseResponse.builder()
                .coinBalance(getCoinBalance(user))
                .owned(true)
                .equipped(false)
                .item(toItemResponse(item, inventory))
                .build();
    }

    @Transactional
    public PurchaseResponse equipItem(long userId, long itemId) {
        UserEntity user = getUser(userId);
        ShopItemEntity item = shopItemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vật phẩm trong shop."));
        UserInventoryEntity selected = userInventoryRepository.findByUserIdAndItemId(userId, itemId)
                .orElseThrow(() -> new RuntimeException("Bạn cần mua vật phẩm trước khi sử dụng."));

        Set<Long> sameTypeItemIds = shopItemRepository.findByActiveTrueOrderByTypeAscPriceAsc().stream()
                .filter(candidate -> normalizeType(candidate.getType()).equals(normalizeType(item.getType())))
                .map(ShopItemEntity::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (UserInventoryEntity inventory : userInventoryRepository.findByUserId(userId)) {
            if (sameTypeItemIds.contains(inventory.getItemId())) {
                inventory.setEquipped(false);
                userInventoryRepository.save(inventory);
            }
        }

        selected.setEquipped(true);
        userInventoryRepository.save(selected);
        applyEquippedItemToUser(user, item);
        userRepository.save(user);

        return PurchaseResponse.builder()
                .coinBalance(getCoinBalance(user))
                .owned(true)
                .equipped(true)
                .item(toItemResponse(item, selected))
                .build();
    }

    @Transactional
    public CoinRewardResponse rewardLessonCompletion(long userId, int lessonId) {
        return rewardOnce(userId, LESSON_REWARD, "LESSON", String.valueOf(lessonId), "Hoàn thành bài học");
    }

    @Transactional
    public CoinRewardResponse rewardQuizSubmission(long userId, long quizId, boolean passed, int scorePercent) {
        if (!passed) {
            return noReward(userId, "Bài tập chưa đạt điểm yêu cầu.");
        }
        return rewardOnce(userId, QUIZ_REWARD, "QUIZ", String.valueOf(quizId), "Hoàn thành bài tập " + scorePercent + "%");
    }

    @Transactional
    public CoinRewardResponse rewardExamSubmission(long userId, long examId, double score) {
        if (score < 5.0) {
            return noReward(userId, "Bài kiểm tra cần đạt từ 5 điểm để nhận xu.");
        }
        int reward = Math.max(10, Math.min(50, (int) Math.round(score * 5)));
        return rewardOnce(userId, reward, "EXAM", String.valueOf(examId), "Hoàn thành bài kiểm tra " + score + " điểm");
    }

    private CoinRewardResponse rewardOnce(long userId, int amount, String sourceType, String sourceId, String description) {
        UserEntity user = getUser(userId);
        if (coinTransactionRepository.existsByUserIdAndSourceTypeAndSourceId(userId, sourceType, sourceId)) {
            return CoinRewardResponse.builder()
                    .rewarded(false)
                    .coinsEarned(0)
                    .coinBalance(getCoinBalance(user))
                    .message("Bạn đã nhận xu cho hoạt động này rồi.")
                    .build();
        }

        user.setCoinBalance(getCoinBalance(user) + amount);
        userRepository.save(user);
        saveTransaction(userId, amount, sourceType, sourceId, description);

        return CoinRewardResponse.builder()
                .rewarded(true)
                .coinsEarned(amount)
                .coinBalance(getCoinBalance(user))
                .message("Bạn nhận được " + amount + " xu.")
                .build();
    }

    private CoinRewardResponse noReward(long userId, String message) {
        UserEntity user = getUser(userId);
        return CoinRewardResponse.builder()
                .rewarded(false)
                .coinsEarned(0)
                .coinBalance(getCoinBalance(user))
                .message(message)
                .build();
    }

    private UserEntity getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
    }

    private int getCoinBalance(UserEntity user) {
        return user.getCoinBalance() == null ? 0 : user.getCoinBalance();
    }

    private void saveTransaction(long userId, int amount, String sourceType, String sourceId, String description) {
        CoinTransactionEntity transaction = new CoinTransactionEntity();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setSourceType(sourceType);
        transaction.setSourceId(sourceId);
        transaction.setDescription(description);
        coinTransactionRepository.save(transaction);
    }

    private void applyEquippedItemToUser(UserEntity user, ShopItemEntity item) {
        String type = normalizeType(item.getType());
        if ("AVATAR".equals(type)) {
            user.setAvatar(item.getAssetUrl());
        } else if ("DECORATION".equals(type)) {
            user.setEquippedDecoration(item.getAccentColor());
        } else if ("LIFE".equals(type)) {
            user.setEquippedLifeIcon(item.getAssetUrl());
        }
    }

    private void applyShopItemFields(ShopItemEntity item, ShopItemRequest request) {
        String name = request == null || request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên vật phẩm.");
        }

        item.setName(name);
        item.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
        item.setType(normalizeType(request.getType()));
        item.setPrice(Math.max(request.getPrice(), 0));
        item.setAssetUrl(request.getAssetUrl() == null ? "" : request.getAssetUrl().trim());
        item.setAccentColor(request.getAccentColor() == null ? "" : request.getAccentColor().trim());
        item.setActive(request.isActive());
    }

    private ShopItemResponse toItemResponse(ShopItemEntity item, UserInventoryEntity inventory) {
        boolean owned = inventory != null;
        return ShopItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .type(normalizeType(item.getType()))
                .price(item.getPrice())
                .assetUrl(item.getAssetUrl())
                .accentColor(item.getAccentColor())
                .owned(owned)
                .equipped(owned && inventory.isEquipped())
                .active(item.isActive())
                .build();
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) return "DECORATION";
        return type.trim().toUpperCase(Locale.ROOT);
    }

    private void seedDefaultItemsIfNeeded() {
        if (shopItemRepository.countByActiveTrue() > 0) {
            return;
        }

        List<ShopItemEntity> defaults = List.of(
                newItem("Avatar Sao băng", "AVATAR", 35, "https://api.dicebear.com/7.x/adventurer/svg?seed=Star", "#2563eb", "Một avatar năng động cho hồ sơ học tập."),
                newItem("Avatar Nhà thám hiểm", "AVATAR", 45, "https://api.dicebear.com/7.x/adventurer/svg?seed=Explorer", "#16a34a", "Gương mặt mới cho hành trình học."),
                newItem("Khung vàng", "DECORATION", 60, "", "#f59e0b", "Trang trí nổi bật quanh hồ sơ của bạn."),
                newItem("Khung xanh ngọc", "DECORATION", 50, "", "#14b8a6", "Một viền hồ sơ dịu mắt và hiện đại."),
                newItem("Tim tiếp sức", "LIFE", 25, "heart", "#ef4444", "Biểu tượng mạng để dùng cho các tính năng luyện tập sau này."),
                newItem("Tia chớp", "LIFE", 30, "zap", "#8b5cf6", "Biểu tượng năng lượng cho tài khoản của bạn.")
        );
        shopItemRepository.saveAll(defaults);
    }

    private ShopItemEntity newItem(String name, String type, int price, String assetUrl, String accentColor, String description) {
        ShopItemEntity item = new ShopItemEntity();
        item.setName(name);
        item.setType(type);
        item.setPrice(price);
        item.setAssetUrl(assetUrl);
        item.setAccentColor(accentColor);
        item.setDescription(description);
        item.setActive(true);
        return item;
    }
}
