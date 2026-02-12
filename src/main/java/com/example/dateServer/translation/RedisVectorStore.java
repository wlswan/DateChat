package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path2;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.TextField;
import redis.clients.jedis.search.schemafields.VectorField;

import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public class RedisVectorStore implements VectorStore {

    private final JedisPooled jedis;
    private final ObjectMapper objectMapper;
    private final float similarityThreshold;
    private final int vectorDimension;

    private static final String INDEX_NAME = "translation_cache_idx";
    private static final String KEY_PREFIX = "translation:cache:";

    public RedisVectorStore(String host, int port, float similarityThreshold, int vectorDimension, ObjectMapper objectMapper) {
        this.jedis = new JedisPooled(host, port);
        this.objectMapper = objectMapper;
        this.similarityThreshold = similarityThreshold;
        this.vectorDimension = vectorDimension;
    }

    @PostConstruct
    public void init() {
        try {
            jedis.ftInfo(INDEX_NAME);
            log.info("Redis Search index '{}' already exists", INDEX_NAME);
        } catch (JedisDataException e) {
            createIndex();
        }
    }

    private void createIndex() {
        try {
            Map<String, Object> vectorAttrs = new HashMap<>();
            vectorAttrs.put("TYPE", "FLOAT32");
            vectorAttrs.put("DIM", vectorDimension);
            vectorAttrs.put("DISTANCE_METRIC", "COSINE");

            FTCreateParams params = FTCreateParams.createParams()
                    .on(IndexDataType.JSON)
                    .addPrefix(KEY_PREFIX);

            jedis.ftCreate(INDEX_NAME, params,
                    TextField.of("$.original").as("original"),
                    TextField.of("$.translated").as("translated"),
                    TagField.of("$.sourceLang").as("sourceLang"),
                    TagField.of("$.targetLang").as("targetLang"),
                    VectorField.builder()
                            .fieldName("$.vector")
                            .as("vector")
                            .algorithm(VectorField.VectorAlgorithm.HNSW)
                            .attributes(vectorAttrs)
                            .build()
            );
            log.info("Created Redis Search index '{}'", INDEX_NAME);
        } catch (Exception e) {
            log.error("Failed to create Redis Search index", e);
        }
    }

    @Override
    public Optional<com.example.dateServer.translation.SearchResult> search(float[] embedding, Lang sourceLang, Lang targetLang) {
        try {
            byte[] vectorBytes = floatArrayToBytes(embedding);

            String queryStr = String.format(
                    "(@sourceLang:{%s} @targetLang:{%s})=>[KNN 1 @vector $vec AS score]",
                    sourceLang.name(), targetLang.name()
            );

            Query query = new Query(queryStr)
                    .addParam("vec", vectorBytes)
                    .returnFields("original", "translated", "score")
                    .setSortBy("score", true)
                    .dialect(2);

            SearchResult result = jedis.ftSearch(INDEX_NAME, query);

            if (result.getTotalResults() == 0) {
                return Optional.empty();
            }

            Document doc = result.getDocuments().get(0);
            double distance = Double.parseDouble(doc.getString("score"));
            float similarity = (float) (1.0 - distance);

            if (similarity < similarityThreshold) {
                log.debug("Cache miss: similarity {} < threshold {}", similarity, similarityThreshold);
                return Optional.empty();
            }

            String original = doc.getString("original");
            String translated = doc.getString("translated");

            log.info("Cache hit: score={}, original={}", similarity, original);
            return Optional.of(new com.example.dateServer.translation.SearchResult(
                    doc.getId(), original, translated, similarity));

        } catch (Exception e) {
            log.error("Failed to search Redis", e);
            return Optional.empty();
        }
    }

    @Override
    public void save(float[] embedding, String original, String translated,
                     Lang sourceLang, Lang targetLang) {
        try {
            String id = hashText(original);
            String key = KEY_PREFIX + sourceLang.name() + "_" + targetLang.name() + ":" + id;

            Map<String, Object> jsonData = new HashMap<>();
            jsonData.put("original", original);
            jsonData.put("translated", translated);
            jsonData.put("sourceLang", sourceLang.name());
            jsonData.put("targetLang", targetLang.name());
            jsonData.put("vector", floatArrayToList(embedding));

            String jsonString = objectMapper.writeValueAsString(jsonData);
            jedis.jsonSet(key, Path2.ROOT_PATH, jsonString);
            log.info("Saved to Redis: {} -> {}", original, translated);

        } catch (Exception e) {
            log.error("Failed to save to Redis", e);
        }
    }

    @Override
    public void clear(Lang sourceLang, Lang targetLang) {
        try {
            String pattern = KEY_PREFIX + sourceLang.name() + "_" + targetLang.name() + ":*";
            Set<String> keys = jedis.keys(pattern);
            if (!keys.isEmpty()) {
                jedis.del(keys.toArray(new String[0]));
                log.info("Cleared {} cache entries for {}_{}", keys.size(), sourceLang, targetLang);
            }
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
        }
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private List<Float> floatArrayToList(float[] floats) {
        List<Float> list = new ArrayList<>(floats.length);
        for (float f : floats) {
            list.add(f);
        }
        return list;
    }

    private String hashText(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception e) {
            return String.valueOf(text.hashCode());
        }
    }
}
