package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ShopStatusResponse {
    private int coinBalance;
    private List<ShopItemResponse> items;
}
