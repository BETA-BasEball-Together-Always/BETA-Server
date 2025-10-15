package com.beta.application.auth.service;

import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("RefreshTokenJpaRepository save 메서드가 호출된다.")
    void saveRefreshToken() {
        refreshTokenService.saveRefreshToken(1L, "sample_refresh_token");
        verify(refreshTokenJpaRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("RefreshTokenJpaRepository deleteByUserId 메서드가 호출된다.")
    void deleteRefreshToken() {
        refreshTokenService.deleteRefreshToken(1L);
        verify(refreshTokenJpaRepository).deleteByUserId(1L);
    }
}
