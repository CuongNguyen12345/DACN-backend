package com.cuong.backend.controller;

import com.cuong.backend.dto.SupportMessageDto;
import com.cuong.backend.dto.SupportRequestDto;
import com.cuong.backend.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @GetMapping("/requests")
    public ResponseEntity<List<SupportRequestDto>> getRequests(@RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(supportService.getRequestsByType(type));
        }
        return ResponseEntity.ok(List.of()); // Or return all if needed
    }

    @GetMapping("/requests/user/{userId}")
    public ResponseEntity<List<SupportRequestDto>> getUserRequests(@PathVariable Long userId, @RequestParam(required = false) String type) {
        return ResponseEntity.ok(supportService.getUserRequests(userId, type));
    }

    @GetMapping("/requests/{requestId}/messages")
    public ResponseEntity<List<SupportMessageDto>> getMessages(@PathVariable Long requestId) {
        return ResponseEntity.ok(supportService.getMessagesByRequest(requestId));
    }

    @PostMapping("/test-send")
    public ResponseEntity<?> testSend(@RequestBody com.cuong.backend.dto.MessagePayload payload) {
        try {
            return ResponseEntity.ok(supportService.processMessage(payload));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage() + " | " + e.toString());
        }
    }
}
