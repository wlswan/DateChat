package com.example.dateServer.redis;

import com.example.dateServer.auth.entity.User;
import com.example.dateServer.common.Lang;
import com.example.dateServer.like.entity.Match;
import com.example.dateServer.like.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomLangCacheService {
    private final RedisTemplate<String, String> redisTemplate;
    private final MatchRepository matchRepository;

    private static final String KEY_PREFIX = "room:langs:";
    private static final long TTL_DAYS = 7;

    public void save(Long roomId, User user1, User user2) {
        String key = KEY_PREFIX + roomId;
        redisTemplate.opsForHash().put(key, "user1Id", user1.getId().toString());
        redisTemplate.opsForHash().put(key, "user1Lang", user1.getLang().name());
        redisTemplate.opsForHash().put(key, "user2Id", user2.getId().toString());
        redisTemplate.opsForHash().put(key, "user2Lang", user2.getLang().name());
        redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
    }

    public LangPair get(Long roomId, Long senderId) {
        String key = KEY_PREFIX + roomId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries.isEmpty()) {
            log.info("캐시 miss, DB에서 조회: roomId={}", roomId);
            return loadFromDbAndCache(roomId, senderId);
        }

        String user1Id = (String) entries.get("user1Id");
        String user1Lang = (String) entries.get("user1Lang");
        String user2Lang = (String) entries.get("user2Lang");

        if (senderId.toString().equals(user1Id)) {
            return new LangPair(Lang.valueOf(user1Lang), Lang.valueOf(user2Lang));
        } else {
            return new LangPair(Lang.valueOf(user2Lang), Lang.valueOf(user1Lang));
        }
    }

    private LangPair loadFromDbAndCache(Long roomId, Long senderId) {
        Match match = matchRepository.findByIdWithUsers(roomId).orElseThrow(()->
         new IllegalArgumentException("존재하지 않는 방입니다."));

        User user1 = match.getUser1();
        User user2 = match.getUser2();
        save(roomId, user1, user2);

        if (senderId.equals(user1.getId())) {
            return new LangPair(user1.getLang(), user2.getLang());
        } else {
            return new LangPair(user2.getLang(), user1.getLang());
        }
    }

    public void delete(Long roomId) {
        redisTemplate.delete(KEY_PREFIX + roomId);
    }
}
