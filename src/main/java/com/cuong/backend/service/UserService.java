package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.exception.AppException;
import com.cuong.backend.exception.ErrorCode;
import com.cuong.backend.model.request.AuthenticationRequest;
import com.cuong.backend.model.request.GoogleLoginRequest;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.model.response.AuthenticationResponse;
import com.cuong.backend.repository.UserRepository;
import com.cuong.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository repository;

    @Autowired
    private JwtUtil jwtUtil;

    public UserEntity createUser(UserCreationRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        UserEntity entity = new UserEntity();
        entity.setUserName(request.getUsername());
        entity.setEmail(request.getEmail());

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        entity.setPassword(passwordEncoder.encode(request.getPassword()));

        return repository.save(entity);
    }

    public List<UserEntity> getAllUsers() {
        return repository.findAll();
    }

    public List<UserEntity> getAllUsersByName(String name) {
        return repository.findAllByUserName(name);
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        UserEntity user = repository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_PASSWORD);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthenticationResponse(token, user);
    }

    public AuthenticationResponse loginWithGoogle(GoogleLoginRequest request) {
        // Find user by email
        UserEntity existingUser = repository.findOneByEmail(request.getEmail());

        if (existingUser != null && existingUser.getLoginByGoogle() == 1) {
            // User already exists, return it
            String token = jwtUtil.generateToken(existingUser.getEmail());
            return new AuthenticationResponse(token, existingUser);
        }

        // User doesn't exist, create a new one
        UserEntity newUser = new UserEntity();
        newUser.setUserName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setLoginByGoogle(1);

        // Generate random password as password is required
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        UserEntity savedUser = repository.save(newUser);
        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new AuthenticationResponse(token, savedUser);
    }

    // public UserEntity getProfile() {
    // UserEntity user = repository.findOneByEmail(request.getEmail());
    // if (user == null) {
    // throw new AppException(ErrorCode.USER_NOT_FOUND);
    // }
    // return user;
    // }
}
