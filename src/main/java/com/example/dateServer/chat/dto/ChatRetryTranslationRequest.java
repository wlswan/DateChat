package com.example.dateServer.chat.dto;

import lombok.Data;

@Data
public class ChatRetryTranslationRequest {
    private String messageId;
    private Long roomId;
    private String content;
}
