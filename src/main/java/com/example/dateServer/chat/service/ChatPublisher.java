package com.example.dateServer.chat.service;

import com.example.dateServer.chat.RedisConfig;
import com.example.dateServer.chat.entity.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(RedisConfig.CHAT_CHANNEL, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
