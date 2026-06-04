package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseResponse {
    private int coinBalance;
    private boolean owned;
    private boolean equipped;
    private ShopItemResponse item;
}
