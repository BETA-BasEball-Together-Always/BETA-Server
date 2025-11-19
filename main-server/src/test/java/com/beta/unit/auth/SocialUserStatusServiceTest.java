package com.beta.unit.auth;

import com.beta.common.exception.auth.NameDuplicateException;
import com.beta.common.exception.auth.PersonalInfoAgreementRequiredException;
import com.beta.common.exception.auth.UserSuspendedException;
import com.beta.common.exception.auth.UserWithdrawnException;
import com.beta.domain.auth.User;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.infra.auth.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialUserStatusService 단위 테스트")
class SocialUserStatusServiceTest {

    @InjectMocks
    private SocialUserStatusService socialUserStatusService;

    @Test
    @DisplayName("null 사용자는 신규 사용자로 판단한다")
    void should_returnTrue_when_isNewUserWithNullUser() {
        // given
        User nullUser = null;

        // when
        boolean result = socialUserStatusService.isNewUser(nullUser);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("ID가 null인 사용자는 신규 사용자로 판단한다")
    void should_returnTrue_when_isNewUserWithNewUser() {
        // given
        User newUser = User.builder()
                .id(null)
                .nickName("신규유저")
                .build();

        // when
        boolean result = socialUserStatusService.isNewUser(newUser);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("ID가 있는 사용자는 기존 사용자로 판단한다")
    void should_returnFalse_when_isNewUserWithExistingUser() {
        // given
        User existingUser = User.builder()
                .id(1L)
                .nickName("기존유저")
                .build();

        // when
        boolean result = socialUserStatusService.isNewUser(existingUser);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("정상 상태의 사용자는 검증을 통과한다")
    void should_pass_when_validateUserStatusWithActiveUser() {
        // given
        User activeUser = User.builder()
                .id(1L)
                .status(UserEntity.UserStatus.ACTIVE.name())
                .build();

        // when & then
        assertThatCode(() -> socialUserStatusService.validateUserStatus(activeUser))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("탈퇴한 사용자는 UserWithdrawnException을 발생시킨다")
    void should_throwUserWithdrawnException_when_validateUserStatusWithWithdrawnUser() {
        // given
        User withdrawnUser = User.builder()
                .id(1L)
                .status(UserEntity.UserStatus.WITHDRAWN.name())
                .build();

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(withdrawnUser))
                .isInstanceOf(UserWithdrawnException.class)
                .hasMessage("탈퇴한 사용자입니다.");
    }

    @Test
    @DisplayName("정지된 사용자는 UserSuspendedException을 발생시킨다")
    void should_throwUserSuspendedException_when_validateUserStatusWithSuspendedUser() {
        // given
        User suspendedUser = User.builder()
                .id(1L)
                .status(UserEntity.UserStatus.SUSPENDED.name())
                .build();

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(suspendedUser))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessage("정지된 사용자입니다. 관리자에게 문의 하세요.");
    }

    @Test
    @DisplayName("개인정보 동의가 true이면 검증을 통과한다")
    void should_pass_when_validateAgreePersonalInfoWithTrue() {
        // given
        Boolean agreePersonalInfo = true;

        // when & then
        assertThatCode(() -> socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("개인정보 동의가 false이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void should_throwException_when_validateAgreePersonalInfoWithFalse() {
        // given
        Boolean agreePersonalInfo = false;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.");
    }

    @Test
    @DisplayName("개인정보 동의가 null이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void should_throwException_when_validateAgreePersonalInfoWithNull() {
        // given
        Boolean agreePersonalInfo = null;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.");
    }

    @Test
    @DisplayName("이름이 중복되지 않으면 검증을 통과한다")
    void should_pass_when_validateNameDuplicateWithFalse() {
        // given
        boolean nameDuplicate = false;

        // when & then
        assertThatCode(() -> socialUserStatusService.validateNameDuplicate(nameDuplicate))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이름이 중복되면 NameDuplicateException을 발생시킨다")
    void should_throwNameDuplicateException_when_validateNameDuplicateWithTrue() {
        // given
        boolean nameDuplicate = true;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateNameDuplicate(nameDuplicate))
                .isInstanceOf(NameDuplicateException.class)
                .hasMessage("이미 존재하는 이름입니다.");
    }
}
