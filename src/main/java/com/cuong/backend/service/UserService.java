package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.exception.AppException;
import com.cuong.backend.exception.ErrorCode;
import com.cuong.backend.model.request.AuthenticationRequest;
import com.cuong.backend.model.request.ForgotPasswordRequest;
import com.cuong.backend.model.request.GoogleLoginRequest;
import com.cuong.backend.model.request.UserCreationRequest;
import com.cuong.backend.model.request.UpdateProfileRequest;
import com.cuong.backend.model.request.VerifyOTPRequest;
import com.cuong.backend.model.response.AuthenticationResponse;
import com.cuong.backend.repository.UserRepository;
import com.cuong.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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

    @Autowired
    private JavaMailSender mailSender;

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
        newUser.setRole("STUDENT");

        // Generate random password as password is required
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        UserEntity savedUser = repository.save(newUser);
        String token = jwtUtil.generateToken(savedUser.getEmail());
        return new AuthenticationResponse(token, savedUser);
    }

    public String requestOTP(ForgotPasswordRequest request) {
        UserEntity user = repository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // Generate 6-digit OTP
        java.util.Random random = new java.util.Random();
        int otp = 100000 + random.nextInt(900000);

        user.setOtp(String.valueOf(otp));
        repository.save(user);

        // Send Email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Mã OTP đặt lại mật khẩu");
        message.setText("Mã OTP của bạn là: " + otp);

        mailSender.send(message);

        return "OTP has been sent to your email";
    }

    public String verifyOTP(VerifyOTPRequest request) {
        UserEntity user = repository.findOneByEmail(request.getEmail());
        if (user == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // Generate random password
        String newPassword = UUID.randomUUID().toString().substring(0, 8);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtp(null); // Clear OTP

        repository.save(user);

        // Send Email with new password
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Mật khẩu truy cập mới");
        message.setText("Mật khẩu mới của bạn là: " + newPassword);

        mailSender.send(message);

        return "Mật khẩu mới đã được gửi vào email";
    }

    public UserEntity getProfile(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String email = jwtUtil.extractEmail(token);
            UserEntity user = repository.findOneByEmail(email);
            if (user == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            return user;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    public UserEntity updateProfile(String token, UpdateProfileRequest request) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            String email = jwtUtil.extractEmail(token);
            UserEntity user = repository.findOneByEmail(email);
            if (user == null) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
            if (request.getGrade() != null)
                user.setGrade(request.getGrade());
            if (request.getPhoneNumber() != null)
                user.setPhoneNumber(request.getPhoneNumber());
            if (request.getSchoolName() != null)
                user.setSchoolName(request.getSchoolName());
            return repository.save(user);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }
}
