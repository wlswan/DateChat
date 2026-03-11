package com.example.dateServer.translation;

import com.example.dateServer.common.Lang;
import com.example.dateServer.translation.dto.TranslationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 부하 테스트용 컨트롤러
 *
 * /api/test/translate/sync  - GPT 직접 호출 (블로킹)
 * /api/test/translate/async - MQ 발행 (논블로킹)
 */
@Slf4j
@RestController
@RequestMapping("/api/test/translate")
@RequiredArgsConstructor
public class TranslationTestController {

    private final TranslationRequestPublisher publisher;
    private final TranslationTracker tracker;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String apiKey;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            this.restClient = RestClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .build();
            log.info("OpenAI API 클라이언트 초기화 완료");
        } else {
            log.warn("OpenAI API 키가 설정되지 않음 - sync 엔드포인트 사용 불가");
        }
    }

    @PostMapping("/sync")
    public ResponseEntity<?> translateSync(@RequestBody TranslateTestRequest request) {
        long start = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("[SYNC] 시작 - thread: {}, text: {}", threadName, request.text());

        if (restClient == null) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long elapsed = System.currentTimeMillis() - start;
            return ResponseEntity.ok(Map.of(
                    "mode", "sync-simulated",
                    "thread", threadName,
                    "elapsed", elapsed + "ms",
                    "translated", "[SIMULATED] " + request.text()
            ));
        }

        try {
            String translated = callGpt(request.text());
            long elapsed = System.currentTimeMillis() - start;
            log.info("[SYNC] 완료 - thread: {}, elapsed: {}ms", threadName, elapsed);

            return ResponseEntity.ok(Map.of(
                    "mode", "sync",
                    "thread", threadName,
                    "elapsed", elapsed + "ms",
                    "translated", translated
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/async")
    public ResponseEntity<?> translateAsync(@RequestBody TranslateTestRequest request) {
        long start = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("[ASYNC] 시작 - thread: {}, text: {}", threadName, request.text());

        String messageId = UUID.randomUUID().toString();

        TranslationRequest translationRequest = TranslationRequest.builder()
                .messageId(messageId)
                .roomId(1L)
                .senderId(1L)
                .content(request.text())
                .sourceLang(Lang.KO)
                .targetLang(Lang.JP)
                .build();

        tracker.markPublished(messageId);
        publisher.publish(translationRequest);

        long elapsed = System.currentTimeMillis() - start;
        log.info("[ASYNC] 완료 - thread: {}, elapsed: {}ms", threadName, elapsed);

        return ResponseEntity.ok(Map.of(
                "mode", "async",
                "thread", threadName,
                "elapsed", elapsed + "ms",
                "messageId", messageId,
                "status", "번역 요청이 워커로 전송됨. 완료 시 WebSocket으로 push됩니다."
        ));
    }

    @GetMapping("/status/{messageId}")
    public ResponseEntity<?> getStatus(@PathVariable String messageId) {
        var status = tracker.getStatus(messageId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "messageId", messageId,
                "completed", status.isCompleted(),
                "elapsedTime", status.getElapsedTime() + "ms",
                "translatedContent", status.getTranslatedContent() != null ? status.getTranslatedContent() : ""
        ));
    }

    /**
     * 전체 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAllStatus() {
        return ResponseEntity.ok(Map.of(
                "published", tracker.getPublishedCount().get(),
                "completed", tracker.getCompletedCount().get(),
                "allCompleted", tracker.isAllCompleted()
        ));
    }

    /**
     * 상태 리셋
     */
    @PostMapping("/reset")
    public ResponseEntity<?> reset() {
        tracker.reset();
        return ResponseEntity.ok(Map.of("status", "reset"));
    }

    private String callGpt(String text) throws Exception {
        String response = restClient.post()
                .uri("/chat/completions")
                .body(Map.of(
                        "model", "gpt-4o-mini",
                        "messages", List.of(
                                Map.of("role", "system", "content",
                                        "Translate to Japanese. Only respond with the translation."),
                                Map.of("role", "user", "content", text)
                        ),
                        "temperature", 0.3
                ))
                .retrieve()
                .body(String.class);

        JsonNode root = objectMapper.readTree(response);
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    public record TranslateTestRequest(String text) {}
}
