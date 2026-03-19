package com.example.dateServer.chat;

import com.example.dateServer.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    private static final String USER_ID_KEY = "userId";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        //MessageHeaderAccessor -> SimpMessageHeaderAccessor -> StompHeaderAccessor message를 각 accessor로 wrap을 싸서 사용
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (command == null) {
            return message;
        }

        switch (command) {
            case CONNECT:
                handleConnect(accessor);
                break;
            case SUBSCRIBE:
                validateAuthentication(accessor, "구독");
                break;
            case SEND:
                validateAuthentication(accessor, "메시지 전송");
                break;
            case DISCONNECT:
                handleDisconnect(accessor);
                break;
        }

        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("WebSocket 연결 거부: Authorization 헤더 없음");
            throw new MessagingException("인증 토큰이 필요합니다");
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validateToken(token)) {
            log.warn("WebSocket 연결 거부: 유효하지 않은 토큰");
            throw new MessagingException("유효하지 않은 토큰입니다");
        }

        Long userId = jwtProvider.getUserId(token);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            sessionAttributes.put(USER_ID_KEY, userId);
            log.info("WebSocket 연결 성공: userId={}", userId);
        }
    }

    private void validateAuthentication(StompHeaderAccessor accessor, String action) {
        Long userId = getUserIdFromSession(accessor);

        if (userId == null) {
            log.warn("{} 거부: 인증되지 않은 사용자", action);
            throw new MessagingException("인증이 필요합니다");
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        Long userId = getUserIdFromSession(accessor);
        log.info("WebSocket 연결 종료: userId={}", userId);
    }

    private Long getUserIdFromSession(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        return (Long) sessionAttributes.get(USER_ID_KEY);
    }

    public static Long getUserId(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        return (Long) sessionAttributes.get(USER_ID_KEY);
    }
}
