package com.example.dateServer.chat;

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

    public void publish(ChatMessageRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.convertAndSend(RedisConfig.CHAT_CHANNEL,json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
