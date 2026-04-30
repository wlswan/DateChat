package com.example.dateServer.translation;

import com.example.dateServer.chat.TranslationStatus;
import com.example.dateServer.chat.dto.ChatMessageResponse;
import com.example.dateServer.chat.entity.ChatMessage;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.data.domain.Sort;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranslationTimeoutScheduler {

    private static final long TIMEOUT_SECONDS = 30;
    private static final long FIXED_DELAY_MS = 5_000L;
    private static final String LOCK_KEY = "lock:translation-timeout";
    private static final long LOCK_TTL_SECONDS = 30;
    private static final int BATCH_SIZE = 200;

    private final MongoTemplate mongoTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    public void expirePendingTranslations() {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(LOCK_KEY, "locked", LOCK_TTL_SECONDS, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(acquired)) {
            return;
        }

        try {
            expire();
        } finally {
            redisTemplate.delete(LOCK_KEY);
        }
    }

    private void expire() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(TIMEOUT_SECONDS);

        Query query = Query.query(Criteria.where("translationStatus").is(TranslationStatus.PENDING.name())
                .and("createdAt").lt(threshold))
                .with(Sort.by(Sort.Direction.ASC, "createdAt"))
                .limit(BATCH_SIZE);

        List<ChatMessage> candidates = mongoTemplate.find(query, ChatMessage.class);
        if (candidates.isEmpty()) {
            return;
        }

        for (ChatMessage msg : candidates) {
            UpdateResult result = mongoTemplate.updateFirst(
                    Query.query(Criteria.where("_id").is(msg.getId())
                            .and("translationStatus").is(TranslationStatus.PENDING.name())),
                    new Update().set("translationStatus", TranslationStatus.FAILED.name()),
                    ChatMessage.class
            );

            if (result.getModifiedCount() == 0) continue;

            try {
                simpMessagingTemplate.convertAndSend(
                        "/topic/chat." + msg.getRoomId(),
                        ChatMessageResponse.translationFailed(msg.getRoomId(), msg.getSenderId(), msg.getId())
                );
            } catch (Exception e) {
                log.warn("타임아웃 알림 발송 실패 - 메시지 ID: {}", msg.getId(), e);
            }
            log.warn("번역 타임아웃 처리 - 메시지 ID: {}", msg.getId());
        }
    }
}

