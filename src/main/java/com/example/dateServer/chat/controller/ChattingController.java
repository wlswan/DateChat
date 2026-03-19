package com.example.dateServer.chat.controller;

import com.example.dateServer.chat.*;
import com.example.dateServer.chat.dto.ChatMessageRequest;
import com.example.dateServer.chat.dto.ChatReadRequest;
import com.example.dateServer.chat.dto.ChatSendRequest;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.service.ChatPublisher;
import com.example.dateServer.chat.service.ChatService;
import com.example.dateServer.common.Lang;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

    @MessageMapping("/chat.read")
    public void readMessage(ChatReadRequest request, SimpMessageHeaderAccessor accessor) {
        Long userId = StompChannelInterceptor.getUserId(accessor);
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 읽음 처리 시도");
            return;
        }

        chatService.markMessagesAsRead(request.getRoomId(), userId);

        ChatMessageRequest notification = new ChatMessageRequest();
        notification.setType(MessageType.READ);
        notification.setRoomId(request.getRoomId());
        notification.setSenderId(userId);
        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), notification);
    }
}
