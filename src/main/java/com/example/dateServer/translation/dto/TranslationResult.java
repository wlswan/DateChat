package com.example.dateServer.translation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResult implements Serializable {
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String translatedContent;
    private boolean success;
    private String errorMessage;
}
