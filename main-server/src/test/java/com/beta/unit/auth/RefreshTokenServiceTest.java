package com.beta.unit.auth;

import com.beta.application.auth.service.RefreshTokenService;
import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.repository.RefreshTokenRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 단위 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("리프레시 토큰 저장 시 Redis에 저장한다")
    void should_saveToRedis_when_upsertRefreshToken() {
        // given
        Long userId = 1L;
        String refreshToken = "test_refresh_token";

        // when
        refreshTokenService.upsertRefreshToken(userId, refreshToken);

        // then
        verify(refreshTokenRedisRepository).save(userId, refreshToken);
    }

    @Test
    @DisplayName("유효한 토큰으로 userId 조회 시 userId를 반환한다")
    void should_returnUserId_when_findUserIdByTokenWithValidToken() {
        // given
        String token = "valid_token";
        Long expectedUserId = 100L;

        when(refreshTokenRedisRepository.findUserIdByToken(token))
                .thenReturn(Optional.of(expectedUserId));

        // when
        Long result = refreshTokenService.findUserIdByToken(token);

        // then
        assertThat(result).isEqualTo(expectedUserId);
        verify(refreshTokenRedisRepository).findUserIdByToken(token);
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 userId 조회 시 InvalidTokenException을 발생시킨다")
    void should_throwInvalidTokenException_when_findUserIdByTokenWithInvalidToken() {
        // given
        String token = "invalid_token";

        when(refreshTokenRedisRepository.findUserIdByToken(token))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.findUserIdByToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("리프레시 토큰");

        verify(refreshTokenRedisRepository).findUserIdByToken(token);
    }

    @Test
    @DisplayName("사용자 ID로 토큰 삭제 시 Redis에서 삭제한다")
    void should_deleteFromRedis_when_deleteByUserId() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.deleteByUserId(userId);

        // then
        verify(refreshTokenRedisRepository).deleteByUserId(userId);
    }

    @Test
    @DisplayName("동일한 사용자로 재저장 시 기존 토큰을 교체한다")
    void should_replaceToken_when_upsertRefreshTokenWithSameUser() {
        // given
        Long userId = 1L;
        String oldToken = "old_token";
        String newToken = "new_token";

        // when
        refreshTokenService.upsertRefreshToken(userId, oldToken);
        refreshTokenService.upsertRefreshToken(userId, newToken);

        // then
        verify(refreshTokenRedisRepository, times(2)).save(eq(userId), anyString());
    }
}
