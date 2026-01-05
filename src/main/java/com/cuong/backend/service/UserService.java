package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.exception.AppException;
import com.cuong.backend.exception.ErrorCode;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public UserEntity createUser(UserCreationRequest request) {
        if (repository.existsByUserName(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        UserEntity entity = new UserEntity();
        entity.setUserName(request.getUsername());
        entity.setEmail(request.getEmail());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setPassword(request.getPassword());

        return repository.save(entity);
    }

    public List<UserEntity> getAllUsers() {
        return repository.findAll();
    }

    public List<UserEntity> getAllUsersByName(String name) {
        return repository.findAllByUserName(name);
    }
}
