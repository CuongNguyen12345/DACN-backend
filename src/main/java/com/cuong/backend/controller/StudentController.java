package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

public class StudentController {

    @Autowired
    private UserService service;

    @GetMapping
    public String getMethodName() {
        return "success";
    }

    @GetMapping("/users")
    public List<UserEntity> getAllUsers() {
        return service.getAllUsers();
    }

}
