package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.request.AuthenticationRequest;
import com.cuong.backend.model.request.GoogleLoginRequest;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.model.request.VerifyOTPRequest;
import com.cuong.backend.model.response.ApiResponse;
import com.cuong.backend.model.response.AuthenticationResponse;
import com.cuong.backend.service.UserService;
import com.cuong.backend.model.request.ForgotPasswordRequest;
import com.cuong.backend.model.request.UpdateProfileRequest;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService service;

    @PostMapping("/register")
    public ApiResponse<UserEntity> register(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        return ApiResponse.success(service.login(request));
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        return ApiResponse.success(service.loginWithGoogle(request));
    }

    @PostMapping("/request-otp")
    public ApiResponse<String> requestOTP(@RequestBody @Valid ForgotPasswordRequest request) {
        return ApiResponse.success(service.requestOTP(request));
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOTP(@RequestBody @Valid VerifyOTPRequest request) {
        return ApiResponse.success(service.verifyOTP(request));
    }

    
    @GetMapping("/profile")
    public ApiResponse<UserEntity> getProfile(@RequestHeader("Authorization") String token) {
        return ApiResponse.success(service.getProfile(token));
    }

    
    @PutMapping("/profile")
    public ApiResponse<UserEntity> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(service.updateProfile(token, request));
    }

}
