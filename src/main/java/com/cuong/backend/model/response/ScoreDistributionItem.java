package com.cuong.backend.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreDistributionItem {
    private String range;
    private int percent;
    private long count;
    private String color;
}
