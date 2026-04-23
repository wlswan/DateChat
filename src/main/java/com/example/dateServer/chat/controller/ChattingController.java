package com.example.dateServer.chat.controller;

import com.example.dateServer.chat.*;
import com.example.dateServer.chat.dto.ChatErrorResponse;
import com.example.dateServer.chat.dto.ChatEventBroadcast;
import com.example.dateServer.chat.dto.ChatRetryTranslationRequest;
import com.example.dateServer.chat.exception.ChatMessageAccessDeniedException;
import com.example.dateServer.chat.exception.ChatMessageNotFoundException;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.chat.exception.ChatRoomNotFoundException;
import com.example.dateServer.chat.dto.ChatReadRequest;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.dto.ChatSendRequest;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.service.ChatService;
import com.example.dateServer.common.Lang;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChattingController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatSendRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        Lang userLang = StompChannelInterceptor.getUserLang(accessor);
        Lang targetLang = StompChannelInterceptor.getTargetLang(accessor, request.getRoomId());
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return;
        }

        ChatMessage saved = chatService.saveMessage(request.getRoomId(), userId, request.getContent());
        simpMessagingTemplate.convertAndSend("/topic/chat." + saved.getRoomId(), ChatMessageResponse.from(saved));

        chatService.requestTranslation(
                saved.getId(),
                saved.getRoomId(),
                saved.getSenderId(),
                saved.getContent(),
                userLang,
                targetLang
        );
    }

    @MessageMapping("/chat.retryTranslation")
    public void retryTranslation(ChatRetryTranslationRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 번역 재시도 시도");
            return;
        }
        Lang userLang = StompChannelInterceptor.getUserLang(accessor);
        Lang targetLang = StompChannelInterceptor.getTargetLang(accessor, request.getRoomId());
        chatService.retryTranslation(request.getMessageId(), request.getRoomId(), userId, request.getContent(), userLang, targetLang);
    }
    @MessageMapping("/chat.read")
    public void readMessage(ChatReadRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 읽음 처리 시도");
            return;
        }
        chatService.markMessagesAsRead(request.getRoomId(), userId);
        simpMessagingTemplate.convertAndSend(
                "/topic/chat." + request.getRoomId() + ".events",
                new ChatEventBroadcast(ChatEventType.READ, request.getRoomId(), userId));
    }
    @MessageExceptionHandler(ChatRoomClosedException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleChatRoomClosedException(ChatRoomClosedException e) {
        log.warn("종료된 채팅방 메시지 전송 시도: {}", e.getMessage());
        return new ChatErrorResponse(e.getMessage());
    }

    @MessageExceptionHandler(ChatRoomNotFoundException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleChatRoomNotFoundException(ChatRoomNotFoundException e) {
        log.warn("존재하지 않는 채팅방 메시지 전송 시도: {}", e.getMessage());
        return new ChatErrorResponse(e.getMessage());
    }

    @MessageExceptionHandler(ChatMessageNotFoundException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleChatMessageNotFoundException(ChatMessageNotFoundException e) {
        log.warn("존재하지 않는 메시지 접근 시도: {}", e.getMessage());
        return new ChatErrorResponse(e.getMessage());
    }

    @MessageExceptionHandler(ChatMessageAccessDeniedException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleChatMessageAccessDeniedException(ChatMessageAccessDeniedException e) {
        log.warn("타인 메시지 번역 재시도 시도: {}", e.getMessage());
        return new ChatErrorResponse(e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleException(Exception e) {
        log.error("WebSocket 처리 중 예외 발생", e);
        return new ChatErrorResponse("서버 오류가 발생했습니다.");
    }

}
