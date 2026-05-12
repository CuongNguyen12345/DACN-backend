package com.cuong.backend.service;

import com.cuong.backend.dto.MessagePayload;
import com.cuong.backend.dto.SupportMessageDto;
import com.cuong.backend.dto.SupportRequestDto;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.SupportMessageEntity;
import com.cuong.backend.entity.SupportRequestEntity;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.repository.ChapterRepository;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.SupportMessageRepository;
import com.cuong.backend.repository.SupportRequestRepository;
import com.cuong.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
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
    private final ChapterRepository chapterRepository;

    public List<SupportRequestDto> getRequestsByType(String type) {
        return requestRepository.findByTypeOrderByCreatedAtDesc(type).stream()
                .map(this::mapToRequestDto)
                .collect(Collectors.toList());
    }

    public List<SupportRequestDto> getRequestsByType(String type, Long viewerId) {
        List<SupportRequestEntity> requests = requestRepository.findByTypeOrderByCreatedAtDesc(type);

        if (viewerId == null || !"ACADEMIC".equalsIgnoreCase(type)) {
            return requests.stream().map(this::mapToRequestDto).collect(Collectors.toList());
        }

        UserEntity viewer = userRepository.findById(viewerId).orElse(null);
        if (viewer == null) {
            return List.of();
        }

        if ("teacher".equalsIgnoreCase(viewer.getRole())) {
            return requests.stream()
                    .filter(request -> canTeacherHandleRequest(viewer, request))
                    .map(this::mapToRequestDto)
                    .collect(Collectors.toList());
        }

        return requests.stream().map(this::mapToRequestDto).collect(Collectors.toList());
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

    public List<SupportRequestDto> getUserRequests(Long userId, String type, Long lessonId) {
        if (lessonId != null && type != null) {
            return requestRepository.findByUser_IdAndTypeAndLesson_IdOrderByCreatedAtDesc(
                            userId,
                            type,
                            lessonId.intValue()
                    ).stream()
                    .map(this::mapToRequestDto)
                    .collect(Collectors.toList());
        }

        return getUserRequests(userId, type);
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
            SupportRequestEntity newRequest = new SupportRequestEntity();
            newRequest.setUser(sender);
            newRequest.setType(payload.getType());
            newRequest.setTitle(payload.getTitle() != null ? payload.getTitle() : "Support Request");
            newRequest.setStatus("OPEN");
            newRequest.setCreatedAt(new Date());

            if (payload.getLessonId() != null) {
                lessonRepository.findById(payload.getLessonId().intValue()).ifPresent(lesson -> {
                    newRequest.setLesson(lesson);
                    resolveSubjectByLesson(lesson).ifPresent(newRequest::setSubject);
                });
            }
            if (newRequest.getSubject() == null && payload.getSubjectId() != null) {
                subjectRepository.findById(payload.getSubjectId().intValue()).ifPresent(newRequest::setSubject);
            }

            request = requestRepository.save(newRequest);
        } else {
            request = requestRepository.findById(payload.getRequestId())
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            validateSenderCanPost(sender, request);
            
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

    private java.util.Optional<SubjectEntity> resolveSubjectByLesson(LessonEntity lesson) {
        return chapterRepository.findById(lesson.getChapterId())
                .flatMap(chapter -> subjectRepository.findById(chapter.getSubjectId()));
    }

    private void validateSenderCanPost(UserEntity sender, SupportRequestEntity request) {
        if (request.getUser().getId() == sender.getId()) {
            return;
        }

        if ("admin".equalsIgnoreCase(sender.getRole())) {
            return;
        }

        if ("teacher".equalsIgnoreCase(sender.getRole())
                && "ACADEMIC".equalsIgnoreCase(request.getType())
                && canTeacherHandleRequest(sender, request)) {
            return;
        }

        throw new RuntimeException("You are not allowed to reply to this request");
    }

    private boolean canTeacherHandleRequest(UserEntity teacher, SupportRequestEntity request) {
        SubjectEntity subject = request.getSubject();
        if (subject == null && request.getLesson() != null) {
            subject = resolveSubjectByLesson(request.getLesson()).orElse(null);
        }
        if (subject == null) {
            return false;
        }

        String teacherSubject = normalizeText(teacher.getSchoolName());
        String requestSubject = normalizeText(subject.getName());
        if (!teacherSubject.isBlank() && !teacherSubject.equals(requestSubject)) {
            return false;
        }

        List<String> allowedGrades = parseTeacherGrades(teacher.getGrade());
        if (allowedGrades.isEmpty()) {
            return true;
        }

        String requestGrade = normalizeGrade(subject.getGrade());
        return allowedGrades.contains(requestGrade);
    }

    private List<String> parseTeacherGrades(String grades) {
        if (grades == null || grades.isBlank()) {
            return List.of();
        }

        return Arrays.stream(grades.split(","))
                .map(this::normalizeGrade)
                .filter(grade -> !grade.isBlank())
                .collect(Collectors.toList());
    }

    private String normalizeGrade(String grade) {
        if (grade == null) {
            return "";
        }

        return Normalizer.normalize(grade, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase()
                .replace("lop", "")
                .replace("class", "")
                .trim();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();

        return switch (normalized) {
            case "vat ly", "ly" -> "ly";
            case "hoa hoc", "hoa" -> "hoa";
            case "tieng anh", "anh" -> "anh";
            case "toan" -> "toan";
            default -> normalized;
        };
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
        dto.setRequestUserId(entity.getRequest().getUser().getId());
        dto.setSenderId(entity.getSender().getId());
        dto.setSenderName(entity.getSender().getUserName());
        dto.setSenderRole(entity.getSender().getRole());
        dto.setRequestType(entity.getRequest().getType());
        if (entity.getRequest().getSubject() != null) {
            dto.setSubjectId((long) entity.getRequest().getSubject().getId());
            dto.setSubjectName(entity.getRequest().getSubject().getName());
            dto.setGradeLevel(entity.getRequest().getSubject().getGrade());
        }
        if (entity.getRequest().getLesson() != null) {
            dto.setLessonId((long) entity.getRequest().getLesson().getId());
            dto.setLessonName(entity.getRequest().getLesson().getLessonName());
        }
        dto.setContent(entity.getContent());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
