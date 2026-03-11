package com.example.dateServer.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatPublisher chatPublisher;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request) {
        ChatMessage saved = chatService.saveMessage(request);
        request.setMessageId(saved.getId());
        chatPublisher.publish(request);

        chatService.requestTranslation(
                saved.getId(),
                request.getRoomId(),
                request.getSenderId(),
                request.getContent()
        );
    }

    @MessageMapping("/chat.read")
    public void readMessage(ChatReadRequest request) {
        chatService.markMessagesAsRead(request.getRoomId(), request.getReaderId());

        // 상대방에게 읽음 알림
        ChatMessageRequest notification = new ChatMessageRequest();
        notification.setType(MessageType.READ);
        notification.setRoomId(request.getRoomId());
        notification.setSenderId(request.getReaderId());
        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), notification);
    }
}
