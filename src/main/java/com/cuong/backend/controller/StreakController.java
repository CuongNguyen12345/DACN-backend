package com.cuong.backend.controller;

import com.cuong.backend.model.dto.StreakDTO;
import com.cuong.backend.service.StreakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streak")
public class StreakController {

    @Autowired
    private StreakService streakService;

    /**
     * Lấy số ngày streak hiện tại của user
     * GET /api/streak/{userId}
     */
    @GetMapping("/{userId}")
    public StreakDTO getStreak(@PathVariable Long userId) {
        return streakService.getStreak(userId);
    }

    /**
     * Ghi nhận hôm nay là ngày có hoạt động học tập
     * POST /api/streak/check-in?userId=1
     */
    @PostMapping("/check-in")
    public void checkIn(@RequestParam Long userId) {
        streakService.checkIn(userId);
    }
}
