package com.example.dateServer.chat.dto;

import com.example.dateServer.chat.ChatEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatEventBroadcast {
    private ChatEventType type;
    private Long roomId;
    private Long senderId;
}
