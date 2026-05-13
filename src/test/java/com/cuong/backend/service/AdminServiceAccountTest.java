package com.cuong.backend.service;

import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.model.response.UserAccountDTO;
import com.cuong.backend.repository.ExamRepository;
import com.cuong.backend.repository.QuestionRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.TopicMasteryRepository;
import com.cuong.backend.repository.TopicRepository;
import com.cuong.backend.repository.UserProgressRepository;
import com.cuong.backend.repository.UserRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServiceAccountTest {

    @Test
    void searchAccountsDoesNotCrashWhenAUserHasNoRole() {
        UserRepository userRepository = mock(UserRepository.class);
        AdminService service = new AdminService(
                mock(ChatLanguageModel.class),
                mock(SubjectRepository.class),
                mock(QuestionRepository.class),
                mock(ExamRepository.class),
                mock(TopicRepository.class),
                userRepository,
                mock(UserProgressRepository.class),
                mock(TopicMasteryRepository.class)
        );

        UserEntity unassigned = new UserEntity();
        unassigned.setId(7L);
        unassigned.setUserName("bookmarktest");
        unassigned.setEmail("bookmarktest@example.com");
        unassigned.setRole(null);

        UserEntity teacher = new UserEntity();
        teacher.setId(2L);
        teacher.setUserName("teacher");
        teacher.setEmail("teacher@example.com");
        teacher.setRole("teacher");
        teacher.setSchoolName("Toán");
        teacher.setGrade("10");

        UserEntity admin = new UserEntity();
        admin.setId(1L);
        admin.setUserName("admin");
        admin.setEmail("admin@gmail.com");
        admin.setRole("admin");

        when(userRepository.searchUsers(null, null)).thenReturn(List.of(unassigned, teacher, admin));

        List<UserAccountDTO> result = service.searchAccounts(null, null);

        assertEquals(2, result.size());
        assertEquals(7L, result.get(0).getId());
        assertEquals("Chưa phân quyền", result.get(0).getRole());
        assertEquals("Chưa cập nhật vai trò", result.get(0).getUnit());
        assertEquals(2L, result.get(1).getId());
        assertEquals("TEACHER", result.get(1).getRole());
    }

    @Test
    void getAccountDetailDoesNotCrashWhenAUserHasNoRole() {
        UserRepository userRepository = mock(UserRepository.class);
        UserProgressRepository userProgressRepository = mock(UserProgressRepository.class);
        TopicMasteryRepository topicMasteryRepository = mock(TopicMasteryRepository.class);
        AdminService service = new AdminService(
                mock(ChatLanguageModel.class),
                mock(SubjectRepository.class),
                mock(QuestionRepository.class),
                mock(ExamRepository.class),
                mock(TopicRepository.class),
                userRepository,
                userProgressRepository,
                topicMasteryRepository
        );

        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setUserName("bookmarktest");
        user.setEmail("bookmarktest@example.com");
        user.setRole(null);

        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userProgressRepository.findAll()).thenReturn(List.of());
        when(topicMasteryRepository.findByUserId(7L)).thenReturn(List.of());

        assertDoesNotThrow(() -> service.getAccountDetail(7L));
        assertEquals("Chưa phân quyền", service.getAccountDetail(7L).getRole());
    }
}
