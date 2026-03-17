package com.example.dateServer.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {
    private Long roomId;
    private Long partnerId;
    private String partnerNickname;
}
