package com.example.dateServer.chat.entity;

import com.example.dateServer.chat.TranslationStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@CompoundIndexes({
    @CompoundIndex(def = "{'roomId': 1, '_id': -1}"),
    @CompoundIndex(def = "{'roomId': 1, 'readAt': 1}")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    private String id;

    private Long roomId;

    private Long senderId;

    private String content;

    private String translatedContent;

    private TranslationStatus translationStatus = TranslationStatus.NONE;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @Builder
    public ChatMessage(Long roomId, Long senderId, String content) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.content = content;
        this.translationStatus = TranslationStatus.NONE;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }

    public void updateTranslationSuccess(String translatedContent) {
        this.translatedContent = translatedContent;
        this.translationStatus = TranslationStatus.SUCCESS;
    }

    public void updateTranslationStatus(TranslationStatus status) {
        this.translationStatus = status;
    }
}
