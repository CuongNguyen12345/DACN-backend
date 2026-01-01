package com.cuong.backend.controller;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@RestController
public class HomeController {

//    @Autowired
//    private UserRepository repository;

    @GetMapping
    public String getMethodName() {
        return "success";
    }

//    @GetMapping("/users")
//    public List<UserEntity> getAllUsers() {
//        return repository.findAll();
//    }
}
