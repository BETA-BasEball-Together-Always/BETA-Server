package com.beta.unit.auth;

import com.beta.application.auth.service.UserReadService;
import com.beta.common.exception.auth.UserNotFoundException;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserReadService 단위 테스트")
class UserReadServiceTest {

    @Mock
    private UserJpaRepository userJpaRepository;

    @InjectMocks
    private UserReadService userReadService;

    @Test
    @DisplayName("소셜 ID와 제공자로 사용자 조회 시 사용자를 반환한다")
    void should_returnUser_when_findUserBySocialIdWithExistingUser() {
        // given
        String socialId = "kakao_12345";
        SocialProvider provider = SocialProvider.KAKAO;
        BaseballTeamEntity team = TeamFixture.createDoosan();
        UserEntity userEntity = UserFixture.createActiveUser(socialId, "테스트유저", team);

        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, provider))
                .thenReturn(Optional.of(userEntity));

        // when
        User result = userReadService.findUserBySocialId(socialId, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickName()).isEqualTo("테스트유저");
        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, provider);
    }

    @Test
    @DisplayName("존재하지 않는 소셜 ID 조회 시 null을 반환한다")
    void should_returnNull_when_findUserBySocialIdWithNonExistingUser() {
        // given
        String socialId = "non_existing_id";
        SocialProvider provider = SocialProvider.KAKAO;

        when(userJpaRepository.findBySocialIdAndSocialProvider(socialId, provider))
                .thenReturn(Optional.empty());

        // when
        User result = userReadService.findUserBySocialId(socialId, provider);

        // then
        assertThat(result).isNull();
        verify(userJpaRepository).findBySocialIdAndSocialProvider(socialId, provider);
    }

    @Test
    @DisplayName("중복된 닉네임이 존재하면 true를 반환한다")
    void should_returnTrue_when_isNameDuplicateWithExistingName() {
        // given
        String nickName = "중복된이름";
        when(userJpaRepository.existsByNickName(nickName)).thenReturn(true);

        // when
        boolean result = userReadService.isNameDuplicate(nickName);

        // then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByNickName(nickName);
    }

    @Test
    @DisplayName("중복되지 않은 닉네임이면 false를 반환한다")
    void should_returnFalse_when_isNameDuplicateWithNonExistingName() {
        // given
        String nickName = "사용가능한이름";
        when(userJpaRepository.existsByNickName(nickName)).thenReturn(false);

        // when
        boolean result = userReadService.isNameDuplicate(nickName);

        // then
        assertThat(result).isFalse();
        verify(userJpaRepository).existsByNickName(nickName);
    }

    @Test
    @DisplayName("사용자 ID로 조회 시 사용자를 반환한다")
    void should_returnUser_when_findUserByIdWithExistingUser() {
        // given
        Long userId = 1L;
        BaseballTeamEntity team = TeamFixture.createDoosan();
        UserEntity userEntity = UserFixture.createActiveUser("social_123", "테스트유저", team);

        when(userJpaRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // when
        User result = userReadService.findUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickName()).isEqualTo("테스트유저");
        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID 조회 시 UserNotFoundException을 발생시킨다")
    void should_throwUserNotFoundException_when_findUserByIdWithNonExistingUser() {
        // given
        Long userId = 999L;
        when(userJpaRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userReadService.findUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userJpaRepository).findById(userId);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 시 사용자를 반환한다")
    void should_returnUser_when_findUserByEmailWithExistingUser() {
        // given
        String email = "test@example.com";
        BaseballTeamEntity team = TeamFixture.createDoosan();
        UserEntity userEntity = UserFixture.createActiveUser("social_123", "테스트유저", team);

        when(userJpaRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        User result = userReadService.findUserByEmail(email);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickName()).isEqualTo("테스트유저");
        verify(userJpaRepository).findByEmail(email);
    }

    @Test
    @DisplayName("존재하지 않는 이메일 조회 시 UserNotFoundException을 발생시킨다")
    void should_throwUserNotFoundException_when_findUserByEmailWithNonExistingUser() {
        // given
        String email = "nonexisting@example.com";
        when(userJpaRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userReadService.findUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");

        verify(userJpaRepository).findByEmail(email);
    }

    @Test
    @DisplayName("중복된 이메일이 존재하면 true를 반환한다")
    void should_returnTrue_when_isEmailDuplicateWithExistingEmail() {
        // given
        String email = "duplicate@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userReadService.isEmailDuplicate(email);

        // then
        assertThat(result).isTrue();
        verify(userJpaRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("중복되지 않은 이메일이면 false를 반환한다")
    void should_returnFalse_when_isEmailDuplicateWithNonExistingEmail() {
        // given
        String email = "available@example.com";
        when(userJpaRepository.existsByEmail(email)).thenReturn(false);

        // when
        boolean result = userReadService.isEmailDuplicate(email);

        // then
        assertThat(result).isFalse();
        verify(userJpaRepository).existsByEmail(email);
    }
}
