package com.example.dateServer.chat.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    private String id;

    @Indexed
    private Long roomId;

    private Long senderId;

    private String content;

    private String translatedContent;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @Builder
    public ChatMessage(Long roomId, Long senderId, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public void updateTranslation(String translatedContent) {
        this.translatedContent = translatedContent;
    }
}
