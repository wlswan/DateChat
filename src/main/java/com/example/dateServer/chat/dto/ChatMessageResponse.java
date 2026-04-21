package com.example.dateServer.chat.dto;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private String translatedContent;
    private TranslationStatus translationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static ChatMessageResponse from(ChatMessage entity) {
        return ChatMessageResponse.builder()
                .messageId(entity.getId())
                .roomId(entity.getRoomId())
                .senderId(entity.getSenderId())
                .content(entity.getContent())
                .translatedContent(entity.getTranslatedContent())
                .translationStatus(entity.getTranslationStatus())
                .createdAt(entity.getCreatedAt())
                .readAt(entity.getReadAt())
                .build();
    }

    public static ChatMessageResponse translated(Long roomId, Long senderId, String messageId, String translatedContent) {
        return ChatMessageResponse.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .translatedContent(translatedContent)
                .translationStatus(TranslationStatus.SUCCESS)
                .build();
    }

    public static ChatMessageResponse translationPending(Long roomId, Long senderId, String messageId) {
        return ChatMessageResponse.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .translationStatus(TranslationStatus.PENDING)
                .build();
    }

    public static ChatMessageResponse translationFailed(Long roomId, Long senderId, String messageId) {
        return ChatMessageResponse.builder()
                .messageId(messageId)
                .roomId(roomId)
                .senderId(senderId)
                .translationStatus(TranslationStatus.FAILED)
                .build();
    }
}
