package com.example.dateServer.translation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaEmbeddingClient implements EmbeddingClient {

    private final ObjectMapper objectMapper;

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.embedding.model:nomic-embed-text}")
    private String model;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public float[] embed(String text) {
        try {
            String response = restClient.post()
                    .uri("/api/embeddings")
                    .body(Map.of("model", model, "prompt", text))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.path("embedding");

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }

            log.debug("Generated embedding for: {} (dim={})", text, embedding.length);
            return embedding;

        } catch (Exception e) {
            log.error("Embedding failed: {}", text, e);
            throw new RuntimeException("Embedding failed", e);
        }
    }

    @Override
    public float[] embedWithContext(String text, List<String> context) {
        String combined = text;
        if (context != null && !context.isEmpty()) {
            combined = String.join(" ", context) + " " + text;
        }
        return embed(combined);
    }

    @Override
    public int dimension() {
        return 768; // nomic-embed-text 기본 차원
    }
}
