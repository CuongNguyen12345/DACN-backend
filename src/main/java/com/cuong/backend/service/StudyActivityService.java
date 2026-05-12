package com.cuong.backend.service;

import com.cuong.backend.entity.StudyActivityEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.response.StudyActivityResponse;
import com.cuong.backend.repository.StudyActivityRepository;
import com.cuong.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudyActivityService {
    private final StudyActivityRepository studyActivityRepository;
    private final UserRepository userRepository;

    public StudyActivityService(StudyActivityRepository studyActivityRepository,
                                UserRepository userRepository) {
        this.studyActivityRepository = studyActivityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudyActivityResponse recordStudyActivity(long userId, String source) {
        LocalDate today = LocalDate.now();

        if (!studyActivityRepository.existsByUserIdAndStudyDate(userId, today)) {
            StudyActivityEntity activity = new StudyActivityEntity();
            activity.setUserId(userId);
            activity.setStudyDate(today);
            activity.setSource(source);
            studyActivityRepository.save(activity);
        }

        StudyActivityResponse response = getStudyActivity(userId);
        userRepository.findById(userId).ifPresent(user -> updateUserStreak(user, response.getCurrentStreak()));

        return response;
    }

    public StudyActivityResponse getStudyActivity(long userId) {
        List<LocalDate> studyDates = studyActivityRepository.findByUserIdOrderByStudyDateAsc(userId).stream()
                .map(StudyActivityEntity::getStudyDate)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        Set<LocalDate> studyDateSet = Set.copyOf(studyDates);

        return StudyActivityResponse.builder()
                .currentStreak(calculateCurrentStreak(studyDateSet))
                .totalStudyDays(studyDates.size())
                .studyDates(studyDates.stream().map(LocalDate::toString).collect(Collectors.toList()))
                .build();
    }

    private int calculateCurrentStreak(Set<LocalDate> studyDates) {
        if (studyDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate cursor = studyDates.contains(today) ? today : today.minusDays(1);
        int streak = 0;

        while (studyDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    private void updateUserStreak(UserEntity user, int currentStreak) {
        user.setCurrentStreak(currentStreak);
        userRepository.save(user);
    }
}
