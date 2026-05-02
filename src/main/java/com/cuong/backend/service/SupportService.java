package com.cuong.backend.service;

import com.cuong.backend.dto.MessagePayload;
import com.cuong.backend.dto.SupportMessageDto;
import com.cuong.backend.dto.SupportRequestDto;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.SupportMessageEntity;
import com.cuong.backend.entity.SupportRequestEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.SupportMessageRepository;
import com.cuong.backend.repository.SupportRequestRepository;
import com.cuong.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportRequestRepository requestRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;

    public List<SupportRequestDto> getRequestsByType(String type) {
        return requestRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(this::mapToRequestDto)
                .collect(Collectors.toList());
    }

    public List<SupportRequestDto> getUserRequests(Long userId, String type) {
        if (type != null) {
            return requestRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type).stream()
                    .map(this::mapToRequestDto)
                    .collect(Collectors.toList());
        }
        return requestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToRequestDto)
                .collect(Collectors.toList());
    }

    public List<SupportMessageDto> getMessagesByRequest(Long requestId) {
        return messageRepository.findByRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupportMessageDto processMessage(MessagePayload payload) {
        UserEntity sender = userRepository.findById(payload.getSenderId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SupportRequestEntity request;
        if (payload.getRequestId() == null) {
            // Create new request
            request = new SupportRequestEntity();
            request.setUser(sender);
            request.setType(payload.getType());
            request.setTitle(payload.getTitle() != null ? payload.getTitle() : "Support Request");
            request.setStatus("OPEN");
            request.setCreatedAt(new Date());

            if (payload.getSubjectId() != null) {
                subjectRepository.findById(payload.getSubjectId().intValue()).ifPresent(request::setSubject);
            }
            if (payload.getLessonId() != null) {
                lessonRepository.findById(payload.getLessonId().intValue()).ifPresent(request::setLesson);
            }

            request = requestRepository.save(request);
        } else {
            request = requestRepository.findById(payload.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Request not found"));
            
            // Update status based on who is replying
            if (request.getUser().getId() == sender.getId()) {
                request.setStatus("OPEN");
            } else {
                request.setStatus("CLOSED");
            }
            request = requestRepository.save(request);
        }

        // Create message
        SupportMessageEntity message = new SupportMessageEntity();
        message.setRequest(request);
        message.setSender(sender);
        message.setContent(payload.getContent());
        message.setCreatedAt(new Date());

        message = messageRepository.save(message);

        return mapToMessageDto(message);
    }

    private SupportRequestDto mapToRequestDto(SupportRequestEntity entity) {
        SupportRequestDto dto = new SupportRequestDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setUserName(entity.getUser().getUserName());
        dto.setAvatar(null); // Set avatar logic if applicable
        dto.setType(entity.getType());
        dto.setStatus(entity.getStatus());
        dto.setTitle(entity.getTitle());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getSubject() != null) {
            dto.setSubjectId((long) entity.getSubject().getId());
            dto.setSubjectName(entity.getSubject().getName());
            dto.setGradeLevel(entity.getSubject().getGrade());
        }
        if (entity.getLesson() != null) {
            dto.setLessonId((long) entity.getLesson().getId());
            dto.setLessonName(entity.getLesson().getLessonName());
        }

        return dto;
    }

    private SupportMessageDto mapToMessageDto(SupportMessageEntity entity) {
        SupportMessageDto dto = new SupportMessageDto();
        dto.setId(entity.getId());
        dto.setRequestId(entity.getRequest().getId());
        dto.setSenderId(entity.getSender().getId());
        dto.setSenderName(entity.getSender().getUserName());
        dto.setSenderRole(entity.getSender().getRole());
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
