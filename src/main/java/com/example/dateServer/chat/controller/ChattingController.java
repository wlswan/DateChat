package com.example.dateServer.chat.controller;

import com.example.dateServer.chat.*;
import com.example.dateServer.chat.dto.ChatErrorResponse;
import com.example.dateServer.chat.dto.ChatEventBroadcast;
import com.example.dateServer.chat.exception.ChatRoomClosedException;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import com.example.dateServer.chat.dto.ChatReadRequest;
import com.example.dateServer.chat.dto.ChatSendRequest;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.service.ChatPublisher;
import com.example.dateServer.chat.service.ChatService;
import com.example.dateServer.common.Lang;
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

    private final ChatPublisher chatPublisher;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatSendRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        Lang userLang = StompChannelInterceptor.getUserLang(accessor);
        Lang targetLang = StompChannelInterceptor.getTargetLang(accessor, request.getRoomId());
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 메시지 전송 시도");
            return;
        }

        ChatMessageRequest chatMessageRequest = new ChatMessageRequest();
        chatMessageRequest.setSenderId(userId);
        chatMessageRequest.setContent(request.getContent());
        chatMessageRequest.setRoomId(request.getRoomId());

        ChatMessage saved = chatService.saveMessage(chatMessageRequest);
        chatPublisher.publish(saved);

        chatService.requestTranslation(
                saved.getId(),
                saved.getRoomId(),
                saved.getSenderId(),
                saved.getContent(),
                userLang,
                targetLang
        );
    }

    @MessageExceptionHandler(ChatRoomClosedException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleChatRoomClosedException(ChatRoomClosedException e) {
        log.warn("종료된 채팅방 메시지 전송 시도: {}", e.getMessage());
        return new ChatErrorResponse(e.getMessage());
    }

    @MessageMapping("/chat.read")
    public void readMessage(ChatReadRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 읽음 처리 시도");
            return;
        }
        chatService.markMessagesAsRead(request.getRoomId(), userId);
        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getRoomId() + "/events",
                new ChatEventBroadcast(ChatEventType.READ, request.getRoomId(), userId));
    }
}
