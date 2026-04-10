package com.example.dateServer.chat.exception;

public class ChatRoomAccessDeniedException extends RuntimeException {
    public ChatRoomAccessDeniedException() {
        super("채팅방 접근 권한이 없습니다.");
    }
}
