package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;
import com.example.dateServer.translation.embedding.EmbeddingClient;
import com.example.dateServer.translation.embedding.VectorStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {

//    private final EmbeddingClient embeddingClient;
//    private final VectorStore vectorStore;
    private final Translator translator;

    public TranslationResult translate(String text, Lang sourceLang, Lang targetLang) {
        return translateWithContext(text, sourceLang, targetLang, null);
    }

    public TranslationResult translateWithContext(String text, Lang sourceLang, Lang targetLang,
                                                   List<String> context) {
        if (sourceLang == targetLang) {
            return new TranslationResult(text, text, false);
        }

        // float[] embedding = embeddingClient.embedWithContext(text, context);
        //
        // var cacheHit = vectorStore.search(embedding, sourceLang, targetLang);
        //
        // if (cacheHit.isPresent()) {
        //     log.info("Cache hit (score={}): {}", cacheHit.get().getScore(), text);
        //     return new TranslationResult(text, cacheHit.get().getTranslated(), true);
        // }

        String translated = translator.translate(text, sourceLang, targetLang);

        // vectorStore.save(embedding, text, translated, sourceLang, targetLang);

        return new TranslationResult(text, translated, false);
    }

    public void preload(String original, String translated, Lang sourceLang, Lang targetLang) {
        // float[] embedding = embeddingClient.embed(original);
        // vectorStore.save(embedding, original, translated, sourceLang, targetLang);
    }

    @Getter
    @AllArgsConstructor
    public static class TranslationResult {
        private final String original;
        private final String translated;
        private final boolean cached;
    }
}
