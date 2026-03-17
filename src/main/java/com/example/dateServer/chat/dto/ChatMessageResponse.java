package com.example.dateServer.chat.dto;

import com.example.dateServer.chat.MessageType;
import com.example.dateServer.chat.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private String id;
    private Long roomId;
    private Long senderId;
    private String content;
    private String translatedContent;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private MessageType type;

    public static ChatMessageResponse from(ChatMessage entity) {
        return ChatMessageResponse.builder()
                .id(entity.getId())
                .roomId(entity.getRoomId())
                .senderId(entity.getSenderId())
                .content(entity.getContent())
                .translatedContent(entity.getTranslatedContent())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .type(MessageType.MESSAGE)
                .build();
    }
}
