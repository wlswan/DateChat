package com.example.dateServer.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;


    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try {
            ChatMessageRequest chatMessage = objectMapper.readValue(message.getBody(), ChatMessageRequest.class);

            simpMessagingTemplate.convertAndSend(
                    "/topic/chat/" + chatMessage.getRoomId(),chatMessage
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
