package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopItemResponse {
    private long id;
    private String name;
    private String description;
    private String type;
    private int price;
    private String assetUrl;
    private String accentColor;
    private boolean owned;
    private boolean equipped;
    private boolean active;
}
