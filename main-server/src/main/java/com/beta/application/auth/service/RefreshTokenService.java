package com.beta.application.auth.service;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Transactional
    public void upsertRefreshToken(Long userId, String refreshToken) {
        refreshTokenJpaRepository.deleteByUserId(userId);
        refreshTokenJpaRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusMonths(1))
                .build());
    }

    @Transactional(readOnly = true)
    public RefreshTokenEntity findByToken(String token) {
        return refreshTokenJpaRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 리프레시 토큰입니다."));
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenJpaRepository.deleteByUserId(userId);
    }
}
