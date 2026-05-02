package com.cuong.backend.controller;

import com.cuong.backend.dto.MessagePayload;
import com.cuong.backend.dto.SupportMessageDto;
import com.cuong.backend.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SupportService supportService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(MessagePayload payload) {
        // Process and save the message
        SupportMessageDto savedMessage = supportService.processMessage(payload);

        // Broadcast to the specific request channel (so both student and admin/teacher viewing the ticket get it)
        messagingTemplate.convertAndSend("/topic/support/request/" + savedMessage.getRequestId(), savedMessage);

        // Also broadcast to the generic role channel for notifications (optional)
        if ("SYSTEM".equals(payload.getType())) {
            messagingTemplate.convertAndSend("/topic/support/admin", savedMessage);
        } else if ("ACADEMIC".equals(payload.getType())) {
            messagingTemplate.convertAndSend("/topic/support/teacher", savedMessage);
        }
        
        // Send back to sender to sync requestId if it was null
        messagingTemplate.convertAndSend("/topic/support/user/" + payload.getSenderId(), savedMessage);
    }
}
