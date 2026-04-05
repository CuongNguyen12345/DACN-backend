package com.cuong.backend.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {
    private String grade;
    private String phoneNumber;
    private String schoolName;
}
