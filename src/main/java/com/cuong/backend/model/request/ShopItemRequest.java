package com.cuong.backend.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopItemRequest {
    private String name;
    private String description;
    private String type;
    private int price;
    private String assetUrl;
    private String accentColor;
    private boolean active = true;
}
