package com.example.dateServer.translation.embedding;

import com.example.dateServer.common.Lang;

import java.util.Optional;

public interface VectorStore {

    Optional<SearchResult> search(float[] embedding, Lang sourceLang, Lang targetLang);

    void save(float[] embedding, String original, String translated,
              Lang sourceLang, Lang targetLang);

    void clear(Lang sourceLang, Lang targetLang);
}
