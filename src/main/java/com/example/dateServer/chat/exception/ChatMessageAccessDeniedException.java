package com.example.dateServer.chat.exception;

public class ChatMessageAccessDeniedException extends RuntimeException {
    public ChatMessageAccessDeniedException() {
        super("본인의 메시지가 아닙니다.");
    }
}
