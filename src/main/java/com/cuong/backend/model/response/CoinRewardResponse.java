package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoinRewardResponse {
    private boolean rewarded;
    private int coinsEarned;
    private int coinBalance;
    private String message;
}
