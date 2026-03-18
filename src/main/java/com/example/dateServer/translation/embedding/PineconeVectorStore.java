package com.example.dateServer.translation.embedding;

import com.example.dateServer.common.Lang;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class PineconeVectorStore implements VectorStore {

    private final Index index;
    private final float similarityThreshold;

    public PineconeVectorStore(String apiKey, String indexName, float similarityThreshold) {
        Pinecone pinecone = new Pinecone.Builder(apiKey).build();
        this.index = pinecone.getIndexConnection(indexName);
        this.similarityThreshold = similarityThreshold;
        log.info("인덱스: {}, 유사도 임계값: {}", indexName, similarityThreshold);
    }

    @Override
    public Optional<SearchResult> search(float[] embedding, Lang sourceLang, Lang targetLang) {
        String namespace = buildNamespace(sourceLang, targetLang);

        try {
            QueryResponseWithUnsignedIndices response = index.queryByVector(
                    5,
                    toList(embedding),
                    namespace,
                    null,
                    true,
                    true
            );

            List<ScoredVectorWithUnsignedIndices> matches = response.getMatchesList();

            if (matches.isEmpty()) {
                return Optional.empty();
            }

            ScoredVectorWithUnsignedIndices best = matches.get(0);
            float score = best.getScore();

            if (score < similarityThreshold) {
                log.debug("[Pinecone] 유사도 미달 - 점수: {}, 임계값: {}", score, similarityThreshold);
                return Optional.empty();
            }

            Struct metadata = best.getMetadata();
            String original = metadata.getFieldsOrThrow("original").getStringValue();
            String translated = metadata.getFieldsOrThrow("translated").getStringValue();

            log.info("[Pinecone] 검색 성공 - 유사도: {}, 원본: '{}'", score, original);
            return Optional.of(new SearchResult(best.getId(), original, translated, score));

        } catch (Exception e) {
            log.error("[Pinecone] 검색 실패", e);
            return Optional.empty();
        }
    }

    @Override
    public void save(float[] embedding, String original, String translated,
                     Lang sourceLang, Lang targetLang) {
        String namespace = buildNamespace(sourceLang, targetLang);
        String id = UUID.randomUUID().toString();

        Struct metadata = Struct.newBuilder()
                .putFields("original", Value.newBuilder().setStringValue(original).build())
                .putFields("translated", Value.newBuilder().setStringValue(translated).build())
                .build();

        try {
            index.upsert(id, toList(embedding), null, null, metadata, namespace);
            log.debug("[Pinecone] 저장 완료 - 원본: '{}' -> 번역: '{}'", original, translated);
        } catch (Exception e) {
            log.error("[Pinecone] 저장 실패", e);
        }
    }


    private String buildNamespace(Lang sourceLang, Lang targetLang) {
        return sourceLang.name() + "_" + targetLang.name();
    }

    private List<Float> toList(float[] array) {
        Float[] boxed = new Float[array.length];
        for (int i = 0; i < array.length; i++) {
            boxed[i] = array[i];
        }
        return List.of(boxed);
    }
}
