package com.example.dateServer.translation;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.repository.ChatMessageRepository;
import com.example.dateServer.config.RabbitMQConfig;
import com.example.dateServer.translation.dto.TranslationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.mongodb.client.result.UpdateResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationTimeoutConsumer {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MongoTemplate mongoTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_TIMEOUT_DEAD_QUEUE)
    public void consume(TranslationRequest request) {
        log.info("번역 타임아웃 감지 - 메시지 ID: {}", request.getMessageId());

        UpdateResult result = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(request.getMessageId())
                        .and("translationStatus").is(TranslationStatus.PENDING.name())),
                new Update().set("translationStatus", TranslationStatus.FAILED.name()),
                ChatMessage.class
        );

        if (result.getModifiedCount() == 0) {
            log.info("타임아웃 처리 불필요 - 이미 완료된 메시지: {}", request.getMessageId());
            return;
        }

        simpMessagingTemplate.convertAndSend(
                "/topic/chat." + request.getRoomId(),
                ChatMessageResponse.translationFailed(request.getRoomId(), request.getSenderId(), request.getMessageId())
        );

        log.warn("번역 타임아웃 처리 완료 - 메시지 ID: {}, 방 ID: {}", request.getMessageId(), request.getRoomId());
    }
}
