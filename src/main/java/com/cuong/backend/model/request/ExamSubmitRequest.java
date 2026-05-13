package com.cuong.backend.model.request;

import lombok.Data;

import java.util.Map;

@Data
public class ExamSubmitRequest {
    private Map<String, String> answers;
    private int durationSeconds;
}
