package com.cuong.backend.service;

import com.cuong.backend.entity.SystemSettingsEntity;
import com.cuong.backend.model.request.ExamSecuritySettingsRequest;
import com.cuong.backend.model.response.ExamSecuritySettingsResponse;
import com.cuong.backend.repository.SystemSettingsRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {

    private static final long SETTINGS_ID = 1L;

    private final SystemSettingsRepository systemSettingsRepository;

    public SystemSettingsService(SystemSettingsRepository systemSettingsRepository) {
        this.systemSettingsRepository = systemSettingsRepository;
    }

    public ExamSecuritySettingsResponse getExamSecuritySettings() {
        return toResponse(systemSettingsRepository.findById(SETTINGS_ID).orElseGet(this::createDefaultSettings));
    }

    @Transactional
    public ExamSecuritySettingsResponse updateExamSecuritySettings(ExamSecuritySettingsRequest request) {
        SystemSettingsEntity settings = systemSettingsRepository
                .findById(SETTINGS_ID)
                .orElseGet(this::createDefaultSettings);

        settings.setPreventTabSwitch(valueOrDefault(request.getPreventTabSwitch(), settings.isPreventTabSwitch()));
        settings.setPreventCopy(valueOrDefault(request.getPreventCopy(), settings.isPreventCopy()));
        settings.setShowResultImmediately(
                valueOrDefault(request.getShowResultImmediately(), settings.isShowResultImmediately()));

        return toResponse(systemSettingsRepository.save(settings));
    }

    private SystemSettingsEntity createDefaultSettings() {
        SystemSettingsEntity settings = new SystemSettingsEntity();
        settings.setId(SETTINGS_ID);
        settings.setPreventTabSwitch(true);
        settings.setPreventCopy(true);
        settings.setShowResultImmediately(false);
        return settings;
    }

    private boolean valueOrDefault(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    private ExamSecuritySettingsResponse toResponse(SystemSettingsEntity settings) {
        return ExamSecuritySettingsResponse.builder()
                .preventTabSwitch(settings.isPreventTabSwitch())
                .preventCopy(settings.isPreventCopy())
                .showResultImmediately(settings.isShowResultImmediately())
                .build();
    }
}
