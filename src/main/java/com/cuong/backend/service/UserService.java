package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.exception.AppException;
import com.cuong.backend.exception.ErrorCode;
import com.cuong.backend.model.request.AuthenticationRequest;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    public UserEntity createUser(UserCreationRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (repository.existsByUserName(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity entity = new UserEntity();
        entity.setUserName(request.getUsername());
        entity.setEmail(request.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));

        return repository.save(entity);
    }

    public List<UserEntity> getAllUsers() {
        List<UserEntity> users = new ArrayList<>();
        users.add(new UserEntity("Cuong", "[EMAIL_ADDRESS]", "123456"));
        users.add(new UserEntity("Han", "[EMAIL_ADDRESS]", "123456"));
        users.add(new UserEntity("Thanh", "[EMAIL_ADDRESS]", "123456"));
        users.add(new UserEntity("Linh", "[EMAIL_ADDRESS]", "123456"));
        users.add(new UserEntity("Phuong", "[EMAIL_ADDRESS]", "123456"));
        return users;
        // return repository.findAll();
    }

    public List<UserEntity> getAllUsersByName(String name) {
        return repository.findAllByUserName(name);
    }

    public UserEntity login(AuthenticationRequest request) {
        UserEntity user = repository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        // if (user.getProvider().equals("GOOGLE")) {
        // throw new AppException(ErrorCode.EMAIL_EXISTED_GOOGLE);
        // }

        return user;
    }

}
