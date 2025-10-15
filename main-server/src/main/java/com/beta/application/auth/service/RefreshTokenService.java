package com.beta.application.auth.service;

import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenJpaRepository.save(RefreshTokenEntity.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusMonths(1))
                .build());
    }

    @Transactional
    public void deleteRefreshToken(Long userId) {
        refreshTokenJpaRepository.deleteByUserId(userId);
    }
}
