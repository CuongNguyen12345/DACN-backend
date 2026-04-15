package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.request.AuthenticationRequest;
import com.cuong.backend.model.response.ApiResponse;
import com.cuong.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning")
public class CourseController {

    @Autowired
    private UserService service;

    @GetMapping
    public String getMethodName() {
        return "success";
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers(@RequestParam String grade,
            @RequestParam String subject) {
        List<UserEntity> users = service.getAllUsers();
        return users;
    }

    // @PostMapping("/register")
    // public ApiResponse<UserEntity> register(@RequestBody @Valid
    // UserCreationRequest request) {
    // ApiResponse<UserEntity> apiResponse = new ApiResponse<>();
    // apiResponse.setResult(service.createUser(request));
    // return apiResponse;
    // }

    // @GetMapping("/course")

}
