package com.cuong.backend.model.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 7, message = "Username must be at least 7 characters long")
    String username;
    String email;
    String phoneNumber;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password;
}
