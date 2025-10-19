package com.beta.domain.auth.service;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenValidationService {
    public void validateRefreshToken(RefreshTokenEntity refreshToken) {
        if (refreshToken == null) {
            throw new InvalidTokenException("리프레시 토큰이 존재하지 않습니다.");
        }
        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("만료된 리프레시 토큰입니다.");
        }
    }
}
