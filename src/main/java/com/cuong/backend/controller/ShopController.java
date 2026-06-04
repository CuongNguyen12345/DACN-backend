package com.cuong.backend.controller;

import com.cuong.backend.model.response.PurchaseResponse;
import com.cuong.backend.model.response.ShopStatusResponse;
import com.cuong.backend.service.ShopService;
import com.cuong.backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shop")
public class ShopController {
    private final ShopService shopService;
    private final UserService userService;

    public ShopController(ShopService shopService, UserService userService) {
        this.shopService = shopService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ShopStatusResponse getMyShop(@RequestHeader("Authorization") String token) {
        long userId = userService.getUserId(token);
        return shopService.getShopStatus(userId);
    }

    @PostMapping("/items/{itemId}/purchase")
    public PurchaseResponse purchaseItem(
            @RequestHeader("Authorization") String token,
            @PathVariable long itemId) {
        long userId = userService.getUserId(token);
        return shopService.purchaseItem(userId, itemId);
    }

    @PostMapping("/items/{itemId}/equip")
    public PurchaseResponse equipItem(
            @RequestHeader("Authorization") String token,
            @PathVariable long itemId) {
        long userId = userService.getUserId(token);
        return shopService.equipItem(userId, itemId);
    }
}
