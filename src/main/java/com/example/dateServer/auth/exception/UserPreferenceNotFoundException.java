package com.example.dateServer.auth.exception;

public class UserPreferenceNotFoundException extends RuntimeException {
    public UserPreferenceNotFoundException(Long userId) {
        super("선호 설정이 없습니다: ID " + userId);
    }
}
