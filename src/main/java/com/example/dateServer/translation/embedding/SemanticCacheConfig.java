package com.example.dateServer.translation.embedding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeConfig {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${pinecone.api-key}")
    private String pineconeApiKey;

    @Value("${pinecone.index-name}")
    private String pineconeIndexName;

    @Value("${pinecone.similarity-threshold}")
    private float similarityThreshold;

    @Bean
    public EmbeddingClient embeddingClient(ObjectMapper objectMapper) {
        return new OpenAiEmbeddingClient(openaiApiKey, "text-embedding-3-small", objectMapper);
    }

    @Bean
    public VectorStore vectorStore() {
        return new PineconeVectorStore(pineconeApiKey, pineconeIndexName, similarityThreshold);
    }
}
