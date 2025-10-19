package com.beta.application.auth.service;

import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

    @Test
    @DisplayName("토큰으로 RefreshTokenEntity를 조회한다")
    void findByToken_existingToken_returnsEntity() {
        // given
        String token = "existing-token";
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .userId(1L)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        when(refreshTokenJpaRepository.findByToken(token)).thenReturn(Optional.of(entity));

        // when
        RefreshTokenEntity result = refreshTokenService.findByToken(token);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(token);
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(refreshTokenJpaRepository).findByToken(token);
    }

    @Test
    @DisplayName("존재하지 않는 토큰 조회 시 InvalidTokenException을 발생시킨다")
    void findByToken_nonExistentToken_throwsException() {
        // given
        String token = "non-existent-token";
        when(refreshTokenJpaRepository.findByToken(token)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refreshTokenService.findByToken(token))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 리프레시 토큰입니다.");
        
        verify(refreshTokenJpaRepository).findByToken(token);
    }

    @Test
    @DisplayName("사용자 ID로 리프레시 토큰을 삭제한다")
    void deleteByUserId_callsRepository() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.deleteByUserId(userId);

        // then
        verify(refreshTokenJpaRepository).deleteByUserId(userId);
    }

    @Test
    @DisplayName("여러 번 토큰 업데이트 시 매번 기존 토큰을 삭제하고 새로 저장한다")
    void upsertRefreshToken_multipleUpserts_deletesAndSavesEachTime() {
        // given
        Long userId = 1L;
        String firstToken = "first-token";
        String secondToken = "second-token";

        // when
        refreshTokenService.upsertRefreshToken(userId, firstToken);
        refreshTokenService.upsertRefreshToken(userId, secondToken);

        // then
        verify(refreshTokenJpaRepository, times(2)).deleteByUserId(userId);
        verify(refreshTokenJpaRepository, times(2)).save(any(RefreshTokenEntity.class));
        
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenJpaRepository, times(2)).save(captor.capture());
        
        assertThat(captor.getAllValues()).hasSize(2);
        assertThat(captor.getAllValues().get(0).getToken()).isEqualTo(firstToken);
        assertThat(captor.getAllValues().get(1).getToken()).isEqualTo(secondToken);
    }

    @Test
    @DisplayName("토큰 저장 시 만료시간이 현재로부터 1개월 후로 설정된다")
    void upsertRefreshToken_setsExpirationOneMonthFromNow() {
        // given
        Long userId = 1L;
        String refreshToken = "test-token";
        LocalDateTime beforeCall = LocalDateTime.now();
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);

        // when
        refreshTokenService.upsertRefreshToken(userId, refreshToken);
        LocalDateTime afterCall = LocalDateTime.now();

        // then
        verify(refreshTokenJpaRepository).save(captor.capture());
        RefreshTokenEntity captured = captor.getValue();
        
        LocalDateTime expiresAt = captured.getExpiresAt();
        LocalDateTime expectedMin = beforeCall.plusMonths(1).minusSeconds(1);
        LocalDateTime expectedMax = afterCall.plusMonths(1).plusSeconds(1);
        
        assertThat(expiresAt).isAfter(expectedMin);
        assertThat(expiresAt).isBefore(expectedMax);
    }
}
