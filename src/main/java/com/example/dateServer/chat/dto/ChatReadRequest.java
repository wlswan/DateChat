package com.example.dateServer.chat.dto;

import lombok.Data;

@Data
public class ChatReadRequest {
    private Long roomId;
    private Long readerId;
}
