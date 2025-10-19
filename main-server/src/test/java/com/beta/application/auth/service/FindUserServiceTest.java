package com.beta.application.auth.service;

import com.beta.common.exception.auth.UserNotFoundException;
import com.beta.common.provider.SocialProvider;
import com.beta.domain.auth.User;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindUserService 테스트")
class FindUserServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private FindUserService findUserService;

    @Test
    @DisplayName("findBySocialIdAndSocialProvider 메서드가 호출된다.")
    void findUserBySocialId() {
        // given
        String socialId = "sample_social_id";

        // when
        findUserService.findUserBySocialId(socialId, SocialProvider.KAKAO);

        // then
        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, SocialProvider.KAKAO);
    }

    @Test
    @DisplayName("existsByName 메서드가 호출된다.")
    void isNameDuplicate() {
        // given
        String name = "sample_name";

        // when
        findUserService.isNameDuplicate(name);

        // then
        verify(userJpaRepository).existsByName(name);
    }

    @Test
    @DisplayName("ID로 사용자를 조회한다")
    void findUserById_existingUser_returnsUser() {
        // given
        Long userId = 1L;
        UserEntity userEntity = UserEntity.builder()
                .socialId("kakao_12345")
                .name("김철수")
                .socialProvider(SocialProvider.KAKAO)
                .baseballTeam(BaseballTeamEntity.builder()
                        .code("KIA")
                        .teamNameKr("KIA 타이거즈")
                        .build())
                .status(UserEntity.UserStatus.ACTIVE)
                .role(UserEntity.UserRole.USER)
                .build();
        // Reflection을 사용해 ID 설정
        ReflectionTestUtils.setField(userEntity, "id", userId);
        
        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // when
        User result = findUserService.findUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("김철수");
        assertThat(result.getSocialId()).isEqualTo("kakao_12345");
        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 사용자 조회 시 UserNotFoundException을 발생시킨다")
    void findUserById_nonExistentUser_throwsException() {
        // given
        Long userId = 999L;
        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findUserService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다. userId: " + userId);
        
        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("닉네임이 중복된 경우 true를 반환한다")
    void isNameDuplicate_duplicateName_returnsTrue() {
        // given
        String name = "중복닉네임";
        when(userJpaRepository.existsByName(name)).thenReturn(true);

        // when
        boolean result = findUserService.isNameDuplicate(name);

        // then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByName(name);
    }

    @Test
    @DisplayName("닉네임이 중복되지 않은 경우 false를 반환한다")
    void isNameDuplicate_uniqueName_returnsFalse() {
        // given
        String name = "유니크닉네임";
        when(userJpaRepository.existsByName(name)).thenReturn(false);

        // when
        boolean result = findUserService.isNameDuplicate(name);

        // then
        assertThat(result).isFalse();
        verify(userJpaRepository).existsByName(name);
    }
}
