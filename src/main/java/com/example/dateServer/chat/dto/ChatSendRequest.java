package com.example.dateServer.chat.dto;

import lombok.Data;

@Data
public class ChatSendRequest {

    private Long roomId;

    private String content;

}
