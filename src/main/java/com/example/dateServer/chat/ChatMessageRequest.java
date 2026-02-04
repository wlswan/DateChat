package com.example.dateServer.chat;

import lombok.Data;

@Data
public class ChatMessageRequest {

    private MessageType type;

    private Long roomId;

    private Long senderId;

    private String content;

    private String messageId;
}
