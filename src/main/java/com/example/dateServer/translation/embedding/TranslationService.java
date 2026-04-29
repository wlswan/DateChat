package com.example.dateServer.translation.embedding;

import com.example.dateServer.common.Lang;
import com.example.dateServer.translation.exception.EmbeddingException;
import com.example.dateServer.translation.exception.VectorStoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;

    public Optional<String> checkCache(String text, Lang sourceLang, Lang targetLang) {
        try {
            float[] embedding = embeddingClient.embed(text);
            return vectorStore.search(embedding, sourceLang, targetLang)
                    .map(SearchResult::getTranslated);
        } catch (EmbeddingException | VectorStoreException e) {
            log.warn("[캐시 스킵] {} - 비동기 번역 폴백: '{}'", e.getMessage(), text, e);
            return Optional.empty();
        }
    }

    public void saveToCache(String original, String translated, Lang sourceLang, Lang targetLang) {
        try {
            float[] embedding = embeddingClient.embed(original);
            vectorStore.save(embedding, original, translated, sourceLang, targetLang);
            log.info("[캐시 저장] 원본: '{}' -> 번역: '{}'", original, translated);
        } catch (EmbeddingException | VectorStoreException e) {
            log.warn("[캐시 저장 실패] {}: '{}'", e.getMessage(), original, e);
        }
    }


    public void preload(String original, String translated, Lang sourceLang, Lang targetLang) {
        saveToCache(original, translated, sourceLang, targetLang);
    }
}
