package com.example.dateServer.chat.dto;

import com.example.dateServer.chat.entity.ChatRoomStatus;
import com.example.dateServer.common.Lang;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomResponse {
    private Long roomId;
    private Long partnerId;
    private String partnerNickname;
    private String partnerProfileImageUrl;
    private Lang partnerLang;
    private ChatRoomStatus status;
    private String lastMessageContent;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
