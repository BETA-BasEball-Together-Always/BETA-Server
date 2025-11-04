package com.beta.infra.community.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class CommunityRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private static final Duration TTL = Duration.ofMinutes(5);

    @Getter
    @RequiredArgsConstructor
    public enum ApiPrefix {
        IMAGE("image"),
        POST("post"),
        IMAGE_ADD("image-add"),
        IMAGE_DELETE("image-delete"),
        IMAGE_ORDER("image-order"),
        POST_UPDATE("post-update"),
        POST_DELETE("post-delete"),
        EMOTION("emotion"),
        EMOTION_DELETE("emotion-delete");

        private final String value;
    }

    private String buildKey(ApiPrefix prefix, String uuid) {
        return String.format("idempotency:%s:%s", prefix.getValue(), uuid);
    }

    public boolean trySetIdempotencyKey(ApiPrefix prefix, String uuid) {
        String key = buildKey(prefix, uuid);
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "0", TTL);
        return Boolean.TRUE.equals(success);
    }

    public void delete(ApiPrefix prefix, String uuid) {
        String key = buildKey(prefix, uuid);
        stringRedisTemplate.delete(key);
    }
}
