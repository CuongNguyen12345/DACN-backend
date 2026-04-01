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
        ApiResponse<UserEntity> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.createUser(request));
        return apiResponse;
    }

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.login(request));
        return apiResponse;
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> loginWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.loginWithGoogle(request));
        return apiResponse;
    }

    @PostMapping("/request-otp")
    public ApiResponse<String> requestOTP(@RequestBody @Valid ForgotPasswordRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.requestOTP(request));
        return apiResponse;
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOTP(@RequestBody @Valid VerifyOTPRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.verifyOTP(request));
        return apiResponse;
    }

    
    // @GetMapping("/profile")
    // public ApiResponse<UserEntity> getProfile() {
    // ApiResponse<UserEntity> apiResponse = new ApiResponse<>();
    // apiResponse.setResult(service.getProfile());
    // return apiResponse;
    // }

}
