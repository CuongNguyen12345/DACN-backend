package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.model.response.ApiResponse;
import com.cuong.backend.service.UserService;

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
}
