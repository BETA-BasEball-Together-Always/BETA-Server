package com.beta.unit.auth;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.common.fixture.UserFixture;
import com.beta.domain.auth.service.RefreshTokenValidationService;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenValidationService 단위 테스트")
class RefreshTokenValidationServiceTest {

    @InjectMocks
    private RefreshTokenValidationService refreshTokenValidationService;

    @Test
    @DisplayName("유효한 리프레시 토큰은 검증을 통과한다")
    void should_pass_when_validateRefreshTokenWithValidToken() {
        // given
        RefreshTokenEntity validToken = UserFixture.createRefreshToken(1L, "valid_token");

        // when & then
        assertThatCode(() -> refreshTokenValidationService.validateRefreshToken(validToken))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("null 리프레시 토큰은 InvalidTokenException을 발생시킨다")
    void should_throwInvalidTokenException_when_validateRefreshTokenWithNull() {
        // given
        RefreshTokenEntity nullToken = null;

        // when & then
        assertThatThrownBy(() -> refreshTokenValidationService.validateRefreshToken(nullToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("리프레시 토큰이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("만료된 리프레시 토큰은 InvalidTokenException을 발생시킨다")
    void should_throwInvalidTokenException_when_validateRefreshTokenWithExpiredToken() {
        // given
        RefreshTokenEntity expiredToken = UserFixture.createExpiredRefreshToken(1L, "expired_token");

        // when & then
        assertThatThrownBy(() -> refreshTokenValidationService.validateRefreshToken(expiredToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("만료된 리프레시 토큰입니다.");
    }
}
