package com.example.dateServer.chat.service;

import com.example.dateServer.chat.RedisConfig;
import com.example.dateServer.chat.dto.RedisPublishPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j

@Service
@RequiredArgsConstructor
public class ChatPublisher {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String destination, Object data) {
        try {
            JsonNode dataNode = objectMapper.valueToTree(data);
            String json = objectMapper.writeValueAsString(new RedisPublishPayload(destination, dataNode));
            log.info("Redis 발행 - destination: {}", destination);
            redisTemplate.convertAndSend(RedisConfig.CHAT_CHANNEL, json);
            log.info("Redis 발행 완료 - destination: {}", destination);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
