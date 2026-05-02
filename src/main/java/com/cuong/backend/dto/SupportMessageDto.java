package com.cuong.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class SupportMessageDto {
    private long id;
    private long requestId;
    private long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private Date createdAt;
}
