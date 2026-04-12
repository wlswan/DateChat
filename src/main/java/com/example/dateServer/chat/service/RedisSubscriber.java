package com.example.dateServer.chat.service;

import com.example.dateServer.chat.dto.RedisPublishPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte @Nullable [] pattern) {
        try {
            RedisPublishPayload payload = objectMapper.readValue(message.getBody(), RedisPublishPayload.class);
            log.info("Redis 수신 - destination: {}", payload.getDestination());
            simpMessagingTemplate.convertAndSend(payload.getDestination(), payload.getData());
            log.info("WebSocket 전송 완료 - destination: {}", payload.getDestination());
        } catch (IOException e) {
            log.error("Redis 메시지 처리 실패", e);
            throw new RuntimeException(e);
        }
    }
}

