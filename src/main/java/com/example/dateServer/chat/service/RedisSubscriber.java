package com.example.dateServer.chat.service;

import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);
            ChatMessageResponse response = ChatMessageResponse.from(chatMessage);
            simpMessagingTemplate.convertAndSend(
                    "/topic/chat/" + response.getRoomId(), response
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
