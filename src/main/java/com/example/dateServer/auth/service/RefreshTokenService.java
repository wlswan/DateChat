package com.example.dateServer.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_TTL = 7; // 7일

    public void save(Long userId, String refreshToken) {
        String key = PREFIX + userId + ":" + refreshToken.hashCode();
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL, TimeUnit.DAYS);
    }

    public boolean exists(String refreshToken, Long userId) {
        String key = PREFIX + userId + ":" + refreshToken.hashCode();
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String refreshToken, Long userId) {
        String key = PREFIX + userId + ":" + refreshToken.hashCode();
        redisTemplate.delete(key);
    }

}
