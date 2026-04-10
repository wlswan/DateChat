package com.example.dateServer.chat.exception;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(Long roomId) {
        super("존재하지 않는 채팅방입니다. roomId: " + roomId);
    }
}
