package com.cuong.backend.controller;

import com.cuong.backend.dto.MessagePayload;
import com.cuong.backend.dto.SupportMessageDto;
import com.cuong.backend.dto.SupportRequestDto;
import com.cuong.backend.entity.UserEntity;
import com.cuong.backend.service.SupportService;
import com.cuong.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/requests")
    public ResponseEntity<List<SupportRequestDto>> getRequests(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long viewerId) {
        if (type != null) {
            return ResponseEntity.ok(supportService.getRequestsByType(type, viewerId));
        }
        return ResponseEntity.ok(List.of()); // Or return all if needed
    }

    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<SupportRequestDto>> getUserRequests(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long lessonId) {
        return ResponseEntity.ok(supportService.getUserRequests(userId, type, lessonId));
    }

    @GetMapping("/requests/{requestId}/messages")
    public ResponseEntity<List<SupportMessageDto>> getMessages(@PathVariable Long requestId) {
        return ResponseEntity.ok(supportService.getMessagesByRequest(requestId));
    }

    @PostMapping("/messages")
    public ResponseEntity<SupportMessageDto> sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody MessagePayload payload) {
        UserEntity sender = userService.getProfile(token);
        payload.setSenderId(sender.getId());
        SupportMessageDto savedMessage = supportService.processMessage(payload);
        publishMessage(savedMessage);
        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/test-send")
    public ResponseEntity<?> testSend(@RequestBody com.cuong.backend.dto.MessagePayload payload) {
        try {
            return ResponseEntity.ok(supportService.processMessage(payload));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " | " + e.toString());
        }
    }

    private void publishMessage(SupportMessageDto savedMessage) {
        messagingTemplate.convertAndSend("/topic/support/request/" + savedMessage.getRequestId(), savedMessage);

        if ("SYSTEM".equals(savedMessage.getRequestType())) {
            messagingTemplate.convertAndSend("/topic/support/admin", savedMessage);
        } else if ("ACADEMIC".equals(savedMessage.getRequestType())) {
            messagingTemplate.convertAndSend("/topic/support/teacher", savedMessage);
        }

        if (savedMessage.getSenderId() != savedMessage.getRequestUserId()) {
            messagingTemplate.convertAndSend("/topic/support/user/" + savedMessage.getRequestUserId(), savedMessage);
        }
    }
}
