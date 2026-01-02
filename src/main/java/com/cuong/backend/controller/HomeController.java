package com.cuong.backend.controller;

import com.cuong.backend.constant.SystemConstant;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.dto.UserDTO;
import com.cuong.backend.model.request.UserRequest;
import com.cuong.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class HomeController {

    @Autowired
    private UserRepository repository;

    @GetMapping
    public String getMethodName() {
        return "success";
    }

//    @GetMapping("/users")
//    public List<UserEntity> getAllUsers() {
//        return repository.findAll();
//    }

    @PostMapping("/login")
    public String login(@RequestBody UserRequest request) {
        UserEntity entity = repository.findOneByUserName(request.getUsername());
        try {
            if(request.getPassword().equals(entity.getPassword())) {
                return SystemConstant.USER_EXIST;
            }
        }
        catch (Exception ex) {
            return SystemConstant.USER_NOT_EXIST;
        }
        return "";
    }
}
