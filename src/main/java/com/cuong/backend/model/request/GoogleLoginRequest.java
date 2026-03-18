package com.cuong.backend.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleLoginRequest {
    @NotBlank(message = "Tên không được để trống")
    String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;
}
