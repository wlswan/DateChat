package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;
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
public class OpenAiTranslator implements Translator {

    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.translation.model:gpt-4o-mini}")
    private String model;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String translate(String text, Lang sourceLang, Lang targetLang) {
        if (sourceLang == targetLang) {
            return text;
        }

        String systemPrompt = String.format(
                "You are a translator. Translate the following text from %s to %s. " +
                "Only respond with the translation, nothing else. Keep the tone casual and natural for chat messages.",
                getLangName(sourceLang), getLangName(targetLang)
        );

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .body(Map.of(
                            "model", model,
                            "messages", List.of(
                                    Map.of("role", "system", "content", systemPrompt),
                                    Map.of("role", "user", "content", text)
                            ),
                            "temperature", 0.3
                    ))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String translated = root.path("choices").get(0).path("message").path("content").asText();

            log.info("Translated: {} -> {}", text, translated);
            return translated;

        } catch (Exception e) {
            log.error("Translation failed: {}", text, e);
            throw new RuntimeException("Translation failed", e);
        }
    }

    private String getLangName(Lang lang) {
        return switch (lang) {
            case KO -> "Korean";
            case JP -> "Japanese";
        };
    }
}
