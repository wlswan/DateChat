package com.example.dateServer.chat;

import lombok.Data;

@Data
public class ChatReadRequest {
    private Long roomId;
    private Long readerId;
}
