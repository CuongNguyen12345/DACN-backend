package com.cuong.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StreakDTO {

    /** Số ngày học liên tiếp hiện tại */
    private int streak;

    /** Ngày học gần nhất */
    private LocalDate lastActiveDate;
}
