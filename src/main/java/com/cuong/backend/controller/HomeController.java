package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.exception.ApiResponse;
import com.cuong.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class HomeController {

    @Autowired
    private UserService service;

    @GetMapping
    public String getMethodName() {
        return "success";
    }

    // @GetMapping("/users")
    // public List<UserEntity> getAllUsers() {
    // return repository.findAll();
    // }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return service.getAllUsers();
    }

    @PostMapping("/register")
    public ApiResponse<UserEntity> register(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserEntity> apiResponse = new ApiResponse<>();
        apiResponse.setResult(service.createUser(request));
        return apiResponse;
    }
}
