package com.beta.application.auth.service;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.repository.RefreshTokenRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public void upsertRefreshToken(Long userId, String refreshToken) {
        refreshTokenRedisRepository.save(userId, refreshToken);
    }

    /**
     * RefreshToken으로 userId 조회
     * @throws InvalidTokenException RefreshToken이 유효하지 않거나 만료된 경우
     */
    public Long findUserIdByToken(String refreshToken) {
        return refreshTokenRedisRepository.findUserIdByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않거나 만료된 리프레시 토큰입니다."));
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRedisRepository.deleteByUserId(userId);
    }
}
