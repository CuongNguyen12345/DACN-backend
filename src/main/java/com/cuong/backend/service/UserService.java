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
        if (repository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        UserEntity entity = new UserEntity();
        entity.setUserName(request.getUsername());
        entity.setEmail(request.getEmail());
        // entity.setProvider("LOCAL");
        entity.setPassword(request.getPassword());

        return repository.save(entity);
    }

    public List<UserEntity> getAllUsers() {
        return repository.findAll();
    }

    public List<UserEntity> getAllUsersByName(String name) {
        return repository.findAllByUserName(name);
    }

    public UserEntity login(UserCreationRequest request) {
        UserEntity user = repository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // if (user.getProvider().equals("GOOGLE")) {
        // throw new AppException(ErrorCode.EMAIL_EXISTED_GOOGLE);
        // }

        // if (!user.getPassword().equals(request.getPassword())) {
        // throw new AppException(ErrorCode.INVALID_PASSWORD);
        // }
        return user;
    }
}
