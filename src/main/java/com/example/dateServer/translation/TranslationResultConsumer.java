package com.example.dateServer.translation;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.entity.ChatMessage;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.config.RabbitMQConfig;
import com.example.dateServer.translation.dto.TranslationResult;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationResultConsumer {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MongoTemplate mongoTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSLATION_RESULT_QUEUE)
    public void consume(TranslationResult result) {
        log.info("번역 결과 수신 - 메시지 ID: {}, 방 ID: {}", result.getMessageId(), result.getRoomId());

        if (!result.isSuccess()) {
            log.error("번역 실패 - 메시지 ID: {}, 에러: {}", result.getMessageId(), result.getErrorMessage());
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(result.getMessageId())
                            .and("translationStatus").is(TranslationStatus.PENDING.name())),
                    new Update().set("translationStatus", TranslationStatus.FAILED.name()),
                    ChatMessage.class
            );
            simpMessagingTemplate.convertAndSend("/topic/chat." + result.getRoomId(),
                    ChatMessageResponse.translationFailed(result.getRoomId(), result.getSenderId(), result.getMessageId()));
            return;
        }

        UpdateResult r = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(result.getMessageId())
                        .and("translationStatus").is(TranslationStatus.PENDING.name())),
                new Update()
                        .set("translationStatus", TranslationStatus.SUCCESS.name())
                        .set("translatedContent", result.getTranslatedContent()),
                ChatMessage.class
        );

        if (r.getModifiedCount() == 0) {
            log.info("결과 도착했으나 이미 종료 상태 - 메시지 ID: {}", result.getMessageId());
            return;
        }

        simpMessagingTemplate.convertAndSend("/topic/chat." + result.getRoomId(),
                ChatMessageResponse.translated(result.getRoomId(), result.getSenderId(), result.getMessageId(), result.getTranslatedContent()));

        log.info("번역 완료 - 메시지 ID: {}", result.getMessageId());
    }
}
