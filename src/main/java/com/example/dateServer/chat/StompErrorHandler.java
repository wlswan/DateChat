package com.example.dateServer.chat;

import com.example.dateServer.auth.exception.InvalidAccessTokenException;
import com.example.dateServer.chat.exception.ChatRoomAccessDeniedException;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.chat.exception.ChatRoomNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;

@Slf4j
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

        String errorCode;
        String errorMessage;

        if (cause instanceof InvalidAccessTokenException) {
            errorCode = "UNAUTHORIZED";
            errorMessage = "인증이 필요합니다.";
        } else if (cause instanceof ChatRoomClosedException) {
            errorCode = "CHAT_ROOM_CLOSED";
            errorMessage = "종료된 채팅방입니다.";
        } else if (cause instanceof ChatRoomNotFoundException) {
            errorCode = "CHAT_ROOM_NOT_FOUND";
            errorMessage = "채팅방을 찾을 수 없습니다.";
        } else if (cause instanceof ChatRoomAccessDeniedException) {
            errorCode = "FORBIDDEN";
            errorMessage = "채팅방 접근 권한이 없습니다.";
        } else {
            errorCode = "SERVER_ERROR";
            errorMessage = "서버 오류가 발생했습니다.";
        }

        log.warn("STOMP 인터셉터 에러 [{}]: {}", errorCode, errorMessage);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setNativeHeader("error-code", errorCode);
        accessor.setMessage(errorMessage);
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(
                errorMessage.getBytes(StandardCharsets.UTF_8),
                accessor.getMessageHeaders()
        );
    }
}
