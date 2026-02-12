package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final float similarityThreshold = 0.8f;

    @Override
    public Optional<SearchResult> search(float[] embedding, Lang sourceLang, Lang targetLang) {
        String langKey = makeLangKey(sourceLang, targetLang);

        SearchResult best = null;
        float bestScore = similarityThreshold;

        for (Map.Entry<String, Entry> e : store.entrySet()) {
            Entry entry = e.getValue();
            if (!entry.getLangKey().equals(langKey)) continue;

            float similarity = cosineSimilarity(embedding, entry.getEmbedding());
            if (similarity > bestScore) {
                bestScore = similarity;
                best = new SearchResult(e.getKey(), entry.getOriginal(), entry.getTranslated(), similarity);
            }
        }

        if (best != null) {
            log.info("Cache hit: score={}, original={}", best.getScore(), best.getOriginal());
        }
        return Optional.ofNullable(best);
    }

    @Override
    public void save(float[] embedding, String original, String translated,
                     Lang sourceLang, Lang targetLang) {
        String id = UUID.randomUUID().toString();
        String langKey = makeLangKey(sourceLang, targetLang);
        store.put(id, new Entry(embedding, original, translated, langKey));
        log.info("Saved: {} -> {}", original, translated);
    }

    @Override
    public void clear(Lang sourceLang, Lang targetLang) {
        String langKey = makeLangKey(sourceLang, targetLang);
        store.entrySet().removeIf(e -> e.getValue().getLangKey().equals(langKey));
        log.info("Cleared cache for {}", langKey);
    }

    private String makeLangKey(Lang sourceLang, Lang targetLang) {
        return sourceLang.name() + "_" + targetLang.name();
    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;

        float dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (float) (dot / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    @AllArgsConstructor
    @Getter
    private static class Entry {
        private final float[] embedding;
        private final String original;
        private final String translated;
        private final String langKey;
    }
}
