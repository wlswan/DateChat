package com.example.dateServer.chat.exception;

public class ChatMessageNotFoundException extends RuntimeException {
    public ChatMessageNotFoundException(String messageId) {
        super("존재하지 않는 메시지입니다. messageId: " + messageId);
    }
}
