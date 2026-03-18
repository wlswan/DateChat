package com.example.dateServer.translation.embedding;

import com.example.dateServer.common.Lang;
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
        float[] embedding = embeddingClient.embed(text);
        var cacheHit = vectorStore.search(embedding, sourceLang, targetLang);

        if (cacheHit.isPresent()) {
            log.info("[캐시 히트] 원본: '{}' -> 번역: '{}'", text, cacheHit.get().getTranslated());
            return Optional.of(cacheHit.get().getTranslated());
        }

        log.info("[캐시 미스] 원본: '{}'", text);
        return Optional.empty();
    }

    public void saveToCache(String original, String translated, Lang sourceLang, Lang targetLang) {
        float[] embedding = embeddingClient.embed(original);
        vectorStore.save(embedding, original, translated, sourceLang, targetLang);
        log.info("[캐시 저장] 원본: '{}' -> 번역: '{}'", original, translated);
    }


    public void preload(String original, String translated, Lang sourceLang, Lang targetLang) {
        saveToCache(original, translated, sourceLang, targetLang);
    }
}
