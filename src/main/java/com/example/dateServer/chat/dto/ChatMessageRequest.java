package com.example.dateServer.chat.dto;

import com.example.dateServer.chat.MessageType;
import lombok.Data;

@Data
public class ChatMessageRequest {

    private MessageType type;

    private Long roomId;

    private Long senderId;

    private String content;

    private String messageId;
}
