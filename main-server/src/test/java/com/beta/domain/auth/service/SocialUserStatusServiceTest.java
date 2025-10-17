package com.beta.domain.auth.service;

import com.beta.common.exception.auth.NameDuplicateException;
import com.beta.common.exception.auth.PersonalInfoAgreementRequiredException;
import com.beta.common.exception.auth.UserSuspendedException;
import com.beta.common.exception.auth.UserWithdrawnException;
import com.beta.domain.auth.User;
import com.beta.infra.auth.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SocialUserStatusService 단위 테스트")
class SocialUserStatusServiceTest {

    private SocialUserStatusService socialUserStatusService;

    @BeforeEach
    void setUp() {
        socialUserStatusService = new SocialUserStatusService();
    }

    @Test
    @DisplayName("User가 null이면 신규 사용자로 판단한다")
    void isNewUser_nullUser_returnsTrue() {
        // given
        User user = null;

        // when
        boolean result = socialUserStatusService.isNewUser(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("User의 id가 null이면 신규 사용자로 판단한다")
    void isNewUser_userWithNullId_returnsTrue() {
        // given
        User user = User.builder()
                .id(null)
                .socialId("kakao_12345")
                .name("홍길동")
                .build();

        // when
        boolean result = socialUserStatusService.isNewUser(user);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("User의 id가 존재하면 기존 사용자로 판단한다")
    void isNewUser_existingUser_returnsFalse() {
        // given
        User user = User.builder()
                .id(1L)
                .socialId("kakao_12345")
                .name("홍길동")
                .status(UserEntity.UserStatus.ACTIVE.name())
                .build();

        // when
        boolean result = socialUserStatusService.isNewUser(user);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 상태가 ACTIVE면 검증을 통과한다")
    void validateUserStatus_activeUser_noException() {
        // given
        User user = User.builder()
                .id(1L)
                .status(UserEntity.UserStatus.ACTIVE.name())
                .build();

        // when & then - 예외 발생하지 않음
        socialUserStatusService.validateUserStatus(user);
    }

    @Test
    @DisplayName("사용자 상태가 WITHDRAWN이면 UserWithdrawnException을 발생시킨다")
    void validateUserStatus_withdrawnUser_throwsException() {
        // given
        User user = User.builder()
                .id(1L)
                .status(UserEntity.UserStatus.WITHDRAWN.name())
                .build();

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(user))
                .isInstanceOf(UserWithdrawnException.class)
                .hasMessageContaining("탈퇴한 사용자입니다");
    }

    @Test
    @DisplayName("사용자 상태가 SUSPENDED이면 UserSuspendedException을 발생시킨다")
    void validateUserStatus_suspendedUser_throwsException() {
        // given
        User user = User.builder()
                .id(2L)
                .status(UserEntity.UserStatus.SUSPENDED.name())
                .build();

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateUserStatus(user))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessageContaining("정지된 사용자입니다")
                .hasMessageContaining("관리자에게 문의 하세요");
    }

    @Test
    @DisplayName("개인정보 동의가 true면 검증을 통과한다")
    void validateAgreePersonalInfo_true_noException() {
        // given
        Boolean agreePersonalInfo = true;

        // when & then - 예외 발생하지 않음
        socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo);
    }

    @Test
    @DisplayName("개인정보 동의가 false면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_false_throwsException() {
        // given
        Boolean agreePersonalInfo = false;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessageContaining("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다");
    }

    @Test
    @DisplayName("개인정보 동의가 null이면 PersonalInfoAgreementRequiredException을 발생시킨다")
    void validateAgreePersonalInfo_null_throwsException() {
        // given
        Boolean agreePersonalInfo = null;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateAgreePersonalInfo(agreePersonalInfo))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessageContaining("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다");
    }

    @Test
    @DisplayName("이름 중복이 true면 NameDuplicateException을 발생시킨다")
    void validateNameDuplicate_true_throwsException() {
        // given
        boolean nameDuplicate = true;

        // when & then
        assertThatThrownBy(() -> socialUserStatusService.validateNameDuplicate(nameDuplicate))
                .isInstanceOf(NameDuplicateException.class)
                .hasMessageContaining("이미 존재하는 이름입니다.");
    }
}
