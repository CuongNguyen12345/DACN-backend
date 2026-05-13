package com.cuong.backend.model.request;

import lombok.Data;

@Data
public class ExamSecuritySettingsRequest {
    private Boolean preventTabSwitch;
    private Boolean preventCopy;
    private Boolean showResultImmediately;
}
