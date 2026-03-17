package com.example.dateServer.translation.embedding;

import com.example.dateServer.common.Lang;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

@Slf4j
//@Component
@Profile("prod")
@RequiredArgsConstructor
public class TranslationCacheLoader implements CommandLineRunner {

    private final TranslationService translationService;

    @Override
    public void run(String... args) {
        log.info("Preloading translation cache...");

        // 한국어 -> 일본어
        preloadKoToJp();

        // 일본어 -> 한국어
        preloadJpToKo();

        log.info("Translation cache preloading completed");
    }

    private void preloadKoToJp() {
        String[][] data = {
            {"안녕하세요", "こんにちは"},
            {"반가워요", "嬉しいです"},
            {"감사합니다", "ありがとうございます"},
            {"좋아요", "いいですね"},
            {"뭐해요?", "何してますか?"},
            {"밥 먹었어요?", "ご飯食べましたか?"},
            {"오늘 시간 있어요?", "今日時間ありますか?"},
            {"만나서 반가워요", "会えて嬉しいです"},
            {"잘 자요", "おやすみなさい"},
            {"좋은 하루 되세요", "良い一日を"},
        };

        for (String[] pair : data) {
            try {
                translationService.preload(pair[0], pair[1], Lang.KR, Lang.JP);
            } catch (Exception e) {
                log.warn("Failed to preload: {} -> {}", pair[0], pair[1], e);
            }
        }
    }

    private void preloadJpToKo() {
        String[][] data = {
            {"こんにちは", "안녕하세요"},
            {"嬉しいです", "반가워요"},
            {"ありがとうございます", "감사합니다"},
            {"いいですね", "좋아요"},
            {"何してますか?", "뭐해요?"},
            {"ご飯食べましたか?", "밥 먹었어요?"},
            {"今日時間ありますか?", "오늘 시간 있어요?"},
            {"会えて嬉しいです", "만나서 반가워요"},
            {"おやすみなさい", "잘 자요"},
            {"良い一日を", "좋은 하루 되세요"},
        };

        for (String[] pair : data) {
            try {
                translationService.preload(pair[0], pair[1], Lang.JP, Lang.KR);
            } catch (Exception e) {
                log.warn("Failed to preload: {} -> {}", pair[0], pair[1], e);
            }
        }
    }
}
