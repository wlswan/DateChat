package com.example.dateServer.initdata;

import com.example.dateServer.common.Lang;
import com.example.dateServer.translation.embedding.EmbeddingClient;
import com.example.dateServer.translation.embedding.VectorStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
public class InitDataController {

    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;


    @PostMapping("/push")
    public ResponseEntity<String> addTranslations(@RequestBody List<TranslationData> dataList) {
        for (TranslationData data : dataList) {
            float[] embedding = embeddingClient.embed(data.getOriginal());

            vectorStore.save(
                    embedding,
                    data.getOriginal(),
                    data.getTranslated(),
                    data.getSourceLang(),
                    data.getTargetLang()
            );

            log.info("번역 데이터 추가: '{}' -> '{}'", data.getOriginal(), data.getTranslated());
        }

        return ResponseEntity.ok(dataList.size() + "개 저장 완료");
    }

    @Getter
    @Setter
    public static class TranslationData {
        private String original;
        private String translated;
        private Lang sourceLang;
        private Lang targetLang;
    }
}
