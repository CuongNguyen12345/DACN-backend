package com.cuong.backend.service;

import com.cuong.backend.entity.LearningActivityEntity;
import com.cuong.backend.model.dto.StreakDTO;
import com.cuong.backend.repository.LearningActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StreakService {

    @Autowired
    private LearningActivityRepository learningActivityRepository;

    /**
     * Ghi nhận ngày học hôm nay cho user.
     * Nếu đã check-in hôm nay rồi thì bỏ qua (không insert trùng).
     */
    public void checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        boolean alreadyCheckedIn = learningActivityRepository
                .findByUserIdAndActivityDate(userId, today)
                .isPresent();

        if (!alreadyCheckedIn) {
            LearningActivityEntity activity = new LearningActivityEntity();
            activity.setUserId(userId);
            activity.setActivityDate(today);
            learningActivityRepository.save(activity);
        }
    }

    /**
     * Tính số ngày học liên tiếp (streak) của user.
     *
     * Thuật toán:
     * 1. Lấy danh sách ngày học, sắp xếp mới nhất → cũ nhất
     * 2. Nếu không có dữ liệu → streak = 0
     * 3. Bắt đầu đếm từ ngày gần nhất:
     *    - Nếu ngày gần nhất là hôm nay hoặc hôm qua → bắt đầu streak
     *    - Ngược lại → streak = 0 (đã gián đoạn)
     * 4. Tiếp tục đếm ngược: nếu ngày kế tiếp = ngày trước - 1 ngày → streak++
     *    Dừng khi bị gián đoạn
     */
    public StreakDTO getStreak(Long userId) {
        List<LearningActivityEntity> activities =
                learningActivityRepository.findByUserIdOrderByActivityDateDesc(userId);

        if (activities.isEmpty()) {
            return new StreakDTO(0, null);
        }

        LocalDate today = LocalDate.now();
        LocalDate mostRecentDate = activities.get(0).getActivityDate();

        // Nếu ngày học gần nhất cách hôm nay hơn 1 ngày → streak đã bị reset
        if (mostRecentDate.isBefore(today.minusDays(1))) {
            return new StreakDTO(0, mostRecentDate);
        }

        // Đếm streak
        int streak = 1;
        LocalDate expected = mostRecentDate.minusDays(1);

        for (int i = 1; i < activities.size(); i++) {
            LocalDate date = activities.get(i).getActivityDate();
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                // Bị gián đoạn → dừng
                break;
            }
        }

        return new StreakDTO(streak, mostRecentDate);
    }
}
