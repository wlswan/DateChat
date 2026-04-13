package com.example.dateServer.chat;

public enum TranslationStatus {
    NONE,    // 번역 불필요 (같은 언어, 언어 정보 없음)
    PENDING, // 번역 요청 중 (큐 발행 후 대기)
    SUCCESS, // 번역 완료
    FAILED   // 번역 실패 (서비스 오류 or 최대 재시도 초과)
}
