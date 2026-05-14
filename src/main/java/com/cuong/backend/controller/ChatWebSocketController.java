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
        SupportMessageDto savedMessage = supportService.processMessage(payload);

        messagingTemplate.convertAndSend("/topic/support/request/" + savedMessage.getRequestId(), savedMessage);

        if ("SYSTEM".equals(savedMessage.getRequestType())) {
            messagingTemplate.convertAndSend("/topic/support/admin", savedMessage);
        } else if ("ACADEMIC".equals(savedMessage.getRequestType())) {
            messagingTemplate.convertAndSend("/topic/support/teacher", savedMessage);
        }
        
        messagingTemplate.convertAndSend("/topic/support/user/" + payload.getSenderId(), savedMessage);

        if (savedMessage.getSenderId() != savedMessage.getRequestUserId()) {
            messagingTemplate.convertAndSend("/topic/support/user/" + savedMessage.getRequestUserId(), savedMessage);
        }
    }
}
