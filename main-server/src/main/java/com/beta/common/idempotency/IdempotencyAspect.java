package com.beta.common.idempotency;

import com.beta.common.exception.idempotency.IdempotencyKeyException;
import com.beta.common.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:server:";

    @Around("@annotation(idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        // 1. userId 추출
        Long userId = extractUserId();

        // 2. URL 추출
        String requestUrl = extractRequestUrl();

        // 3. Redis 키 생성
        String redisKey = IDEMPOTENCY_KEY_PREFIX + userId + ":" + requestUrl;

        // 4. Redis 에 저장 시도
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofSeconds(idempotent.ttlSeconds()));

        if (!Boolean.TRUE.equals(success)) {
            log.warn("Duplicate request detected - userId: {}, url: {}", userId, requestUrl);
            throw new IdempotencyKeyException();
        }

        log.debug("Idempotency key registered - userId: {}, url: {}, ttl: {}s",
                userId, requestUrl, idempotent.ttlSeconds());

        return joinPoint.proceed();
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.userId();
        }
        throw new IllegalStateException("인증되지 않은 사용자입니다.");
    }

    private String extractRequestUrl() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Request context를 찾을 수 없습니다.");
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        return method + ":" + uri;
    }
}
