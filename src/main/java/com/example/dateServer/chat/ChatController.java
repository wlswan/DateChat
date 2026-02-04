package com.example.dateServer.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final RedisTemplate<String, String> redisTemplate;
    private final ChatPublisher chatPublisher;
    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request) {
        chatPublisher.publish(request);
        chatService.saveMessage(request);
    }
}
