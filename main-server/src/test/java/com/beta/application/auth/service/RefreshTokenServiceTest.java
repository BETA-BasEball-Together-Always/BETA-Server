package com.beta.application.auth.service;

import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("RefreshTokenJpaRepository delete, save 메서드가 호출된다.")
    void saveRefreshToken() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh_token";
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);

        // when
        refreshTokenService.upsertRefreshToken(userId, refreshToken);

        // then
        verify(refreshTokenJpaRepository).deleteByUserId(1L);
        verify(refreshTokenJpaRepository).save(captor.capture());

        RefreshTokenEntity captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(userId);
        assertThat(captured.getToken()).isEqualTo(refreshToken);
    }
}
