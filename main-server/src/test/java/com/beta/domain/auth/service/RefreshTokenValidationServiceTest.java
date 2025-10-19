package com.beta.domain.auth.service;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenValidationService 단위 테스트")
class RefreshTokenValidationServiceTest {

    @InjectMocks
    private RefreshTokenValidationService refreshTokenValidationService;

    @Test
    @DisplayName("유효한 리프레시 토큰 검증 시 예외가 발생하지 않는다")
    void validateRefreshToken_validToken_noException() {
        // given
        RefreshTokenEntity validToken = RefreshTokenEntity.builder()
                .userId(1L)
                .token("valid-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))  // 7일 후 만료
                .build();

        // when & then
        assertThatNoException()
                .isThrownBy(() -> refreshTokenValidationService.validateRefreshToken(validToken));
    }

    @Test
    @DisplayName("null 토큰 검증 시 InvalidTokenException을 발생시킨다")
    void validateRefreshToken_nullToken_throwsException() {
        // given
        RefreshTokenEntity nullToken = null;

        // when & then
        assertThatThrownBy(() -> refreshTokenValidationService.validateRefreshToken(nullToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("리프레시 토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 InvalidTokenException을 발생시킨다")
    void validateRefreshToken_expiredToken_throwsException() {
        // given
        RefreshTokenEntity expiredToken = RefreshTokenEntity.builder()
                .userId(1L)
                .token("expired-refresh-token")
                .expiresAt(LocalDateTime.now().minusDays(1))  // 1일 전 만료
                .build();

        // when & then
        assertThatThrownBy(() -> refreshTokenValidationService.validateRefreshToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("만료된 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("정확히 현재 시간에 만료되는 토큰 검증 시 InvalidTokenException을 발생시킨다")
    void validateRefreshToken_justExpiredToken_throwsException() {
        // given
        LocalDateTime now = LocalDateTime.now();
        RefreshTokenEntity justExpiredToken = RefreshTokenEntity.builder()
                .userId(1L)
                .token("just-expired-token")
                .expiresAt(now.minusSeconds(1))  // 1초 전 만료
                .build();

        // when & then
        assertThatThrownBy(() -> refreshTokenValidationService.validateRefreshToken(justExpiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("만료된 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("만료 직전 토큰은 여전히 유효하다")
    void validateRefreshToken_aboutToExpireToken_noException() {
        // given
        RefreshTokenEntity aboutToExpireToken = RefreshTokenEntity.builder()
                .userId(1L)
                .token("about-to-expire-token")
                .expiresAt(LocalDateTime.now().plusSeconds(1))  // 1초 후 만료 예정
                .build();

        // when & then
        assertThatNoException()
                .isThrownBy(() -> refreshTokenValidationService.validateRefreshToken(aboutToExpireToken));
    }
}
