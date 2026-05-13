package com.cuong.backend.controller;

import com.cuong.backend.model.request.ExamSecuritySettingsRequest;
import com.cuong.backend.model.response.ExamSecuritySettingsResponse;
import com.cuong.backend.service.SystemSettingsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemSettingsController {

    private final SystemSettingsService systemSettingsService;

    public SystemSettingsController(SystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }

    @GetMapping("/api/settings/exam-security")
    public ExamSecuritySettingsResponse getExamSecuritySettings() {
        return systemSettingsService.getExamSecuritySettings();
    }

    @PutMapping("/api/admin/settings/exam-security")
    public ExamSecuritySettingsResponse updateExamSecuritySettings(
            @RequestBody ExamSecuritySettingsRequest request) {
        return systemSettingsService.updateExamSecuritySettings(request);
    }
}
