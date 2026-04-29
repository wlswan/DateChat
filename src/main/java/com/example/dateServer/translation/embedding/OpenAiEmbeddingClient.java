package com.example.dateServer.translation.embedding;

import com.example.dateServer.translation.exception.EmbeddingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OpenAiEmbeddingClient(String apiKey, String model, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.model = model;
        this.objectMapper = objectMapper;
    }

    @Override
    public float[] embed(String text) {
        try {
            String response = restClient.post()
                    .uri("/embeddings")
                    .body(Map.of("model", model, "input", text))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode embeddingNode = root.path("data").get(0).path("embedding");

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }

            log.debug("Generated embedding for: {} (dim={})", text, embedding.length);
            return embedding;

        } catch (RestClientException | IOException e) {
            throw new EmbeddingException("OpenAI 임베딩 실패", e);
        }
    }

    @Override
    public int dimension() {
        return 1536; // text-embedding-3-small
    }
}
