package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.repository.UserRepository;
import com.cuong.backend.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void getUserIdReturnsTheAuthenticatedProfileId() {
        UserRepository repository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserService service = new UserService(repository, jwtUtil, mock(JavaMailSender.class));

        UserEntity user = new UserEntity();
        user.setId(42L);
        user.setEmail("student@example.com");

        when(jwtUtil.extractEmail("token")).thenReturn("student@example.com");
        when(repository.findOneByEmail("student@example.com")).thenReturn(user);

        assertEquals(42L, service.getUserId("Bearer token"));
    }
}
