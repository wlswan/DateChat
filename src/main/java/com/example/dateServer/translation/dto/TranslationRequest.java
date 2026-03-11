package com.example.dateServer.translation.dto;

import com.example.dateServer.common.Lang;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRequest implements Serializable {
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String content;
    private Lang sourceLang;
    private Lang targetLang;
}
