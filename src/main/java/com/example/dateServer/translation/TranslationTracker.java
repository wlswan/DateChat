package com.example.dateServer.translation;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 번역 완료 추적 (테스트용)
 */
@Slf4j
@Component
public class TranslationTracker {

    private final ConcurrentHashMap<String, TranslationStatus> statusMap = new ConcurrentHashMap<>();

    @Getter
    private final AtomicInteger publishedCount = new AtomicInteger(0);

    @Getter
    private final AtomicInteger completedCount = new AtomicInteger(0);

    public void markPublished(String messageId) {
        statusMap.put(messageId, new TranslationStatus(System.currentTimeMillis()));
        publishedCount.incrementAndGet();
    }

    public void markCompleted(String messageId, String translatedContent) {
        TranslationStatus status = statusMap.get(messageId);
        if (status != null) {
            status.complete(translatedContent);
            completedCount.incrementAndGet();
            log.debug("Translation completed: {} ({}ms)", messageId, status.getElapsedTime());
        }
    }

    public TranslationStatus getStatus(String messageId) {
        return statusMap.get(messageId);
    }

    public void reset() {
        statusMap.clear();
        publishedCount.set(0);
        completedCount.set(0);
    }

    public boolean isAllCompleted() {
        return publishedCount.get() > 0 && publishedCount.get() == completedCount.get();
    }

    @Getter
    public static class TranslationStatus {
        private final long publishedAt;
        private long completedAt;
        private String translatedContent;
        private boolean completed;

        public TranslationStatus(long publishedAt) {
            this.publishedAt = publishedAt;
        }

        public void complete(String translatedContent) {
            this.completedAt = System.currentTimeMillis();
            this.translatedContent = translatedContent;
            this.completed = true;
        }

        public long getElapsedTime() {
            return completed ? completedAt - publishedAt : System.currentTimeMillis() - publishedAt;
        }
    }
}
