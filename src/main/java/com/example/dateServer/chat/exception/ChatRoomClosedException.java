package com.example.dateServer.chat.exception;

public class ChatRoomClosedException extends RuntimeException {
    public ChatRoomClosedException() {
        super("종료된 채팅방입니다.");
    }
}
