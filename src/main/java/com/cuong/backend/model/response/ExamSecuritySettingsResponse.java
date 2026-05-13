package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamSecuritySettingsResponse {
    private boolean preventTabSwitch;
    private boolean preventCopy;
    private boolean showResultImmediately;
}
