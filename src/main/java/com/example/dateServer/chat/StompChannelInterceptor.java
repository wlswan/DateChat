package com.example.dateServer.chat;

import com.example.dateServer.auth.service.JwtProvider;
import com.example.dateServer.auth.entity.User;
import com.example.dateServer.auth.exception.InvalidAccessTokenException;
import com.example.dateServer.auth.exception.UserNotFoundException;
import com.example.dateServer.auth.repository.UserRepository;
import com.example.dateServer.chat.exception.ChatRoomAccessDeniedException;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.common.Lang;
import com.example.dateServer.chat.entity.ChatRoom;
import com.example.dateServer.chat.entity.ChatRoomStatus;
import com.example.dateServer.chat.exception.ChatRoomNotFoundException;
import com.example.dateServer.chat.repository.ChatRoomRepository;
import com.example.dateServer.like.entity.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    private static final String USER_ID_KEY = "userId";
    private static final String USER_LANG_KEY = "userLang";
    private static final String TARGET_LANG_KEY = "targetRoomIdsAndLangs";

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
                handleSubscribe(accessor);
                break;
            case SEND:
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
            throw new InvalidAccessTokenException();
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validateToken(token)) {
            log.warn("WebSocket 연결 거부: 유효하지 않은 토큰");
            throw new InvalidAccessTokenException();
        }

        Long userId = jwtProvider.getUserId(token);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Lang myLang = user.getLang();

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            sessionAttributes.put(USER_ID_KEY, userId);
            sessionAttributes.put(USER_LANG_KEY, myLang);
            sessionAttributes.put(TARGET_LANG_KEY, new HashMap<Long, Lang>());
            log.info("WebSocket 연결 성공: userId={}, userLang={}", userId, myLang);
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || !destination.matches("/topic/chat\\.\\d+(\\.events)?")) {
            return;
        }

        // /topic/chat.123
        Long roomId = getRoomId(destination);
        Long userId = getUserIdFromSession(accessor);

        ChatRoom chatRoom = chatRoomRepository.findByIdWithUsers(roomId).orElseThrow(() -> new ChatRoomNotFoundException(roomId));
        if (chatRoom.getStatus() == ChatRoomStatus.CLOSED) {
            throw new ChatRoomClosedException();
        }

        Match match = chatRoom.getMatch();
        if (!match.getUser1().getId().equals(userId) && !match.getUser2().getId().equals(userId)) {
            throw new ChatRoomAccessDeniedException();
        }
        if(!destination.endsWith(".events")) {
            Lang targetLang = match.getUser1().getId().equals(userId) ? match.getUser2().getLang() : match.getUser1().getLang();

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            Map<Long, Lang> targetRoomIdsAndLangs = (Map<Long, Lang>) sessionAttributes.get(TARGET_LANG_KEY);

            targetRoomIdsAndLangs.put(roomId, targetLang);
            log.info("채팅방 구독: userId={}, roomId={}, targetLang={}", userId, roomId, targetLang);
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

    private static Long getRoomId(String destination) {
        // "/topic/chat.123" 또는 "/topic/chat.123.events"
        String afterPrefix = destination.substring("/topic/chat.".length()); // "123" or "123.events"
        String roomIdStr = afterPrefix.contains(".") ? afterPrefix.substring(0, afterPrefix.indexOf('.')) : afterPrefix;
        return Long.parseLong(roomIdStr);
    }

    public static Long getUserId(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        return (Long) sessionAttributes.get(USER_ID_KEY);
    }

    public static Lang getUserLang(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) return null;
        return (Lang) sessionAttributes.get(USER_LANG_KEY);
    }

    public static Lang getTargetLang(SimpMessageHeaderAccessor accessor, Long roomId) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) return null;
        Map<Long, Lang> targetLangs = (Map<Long, Lang>) sessionAttributes.get(TARGET_LANG_KEY);
        return targetLangs != null ? targetLangs.get(roomId) : null;
    }
}
