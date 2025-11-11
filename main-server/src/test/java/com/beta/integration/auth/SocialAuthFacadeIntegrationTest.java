package com.beta.integration.auth;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.facade.SocialAuthFacade;
import com.beta.common.docker.TestContainer;
import com.beta.common.exception.auth.*;
import com.beta.common.fixture.TeamFixture;
import com.beta.common.fixture.UserFixture;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.infra.auth.client.SocialLoginClient;
import com.beta.infra.auth.client.SocialLoginClientFactory;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import com.beta.infra.auth.repository.UserConsentJpaRepository;
import com.beta.infra.auth.repository.UserJpaRepository;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.infra.common.repository.BaseballTeamRepository;
import com.beta.infra.community.gcs.GcsStorageClient;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.response.TokenResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DisplayName("SocialAuthFacade 통합 테스트")
class SocialAuthFacadeIntegrationTest extends TestContainer {

    @Autowired
    private SocialAuthFacade socialAuthFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Autowired
    private UserConsentJpaRepository userConsentJpaRepository;

    @Autowired
    private BaseballTeamRepository baseballTeamRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SocialLoginClientFactory socialLoginClientFactory;

    @MockitoBean
    private GcsStorageClient gcsStorageClient;

    private BaseballTeamEntity testTeam;
    private SocialLoginClient mockSocialLoginClient;

    @BeforeEach
    void setUp() {
        testTeam = TeamFixture.createDoosan();
        baseballTeamRepository.save(testTeam);

        // Create mock client for auth tests
        mockSocialLoginClient = org.mockito.Mockito.mock(com.beta.infra.auth.client.SocialLoginClient.class);
    }

    @AfterEach
    void tearDown() {
        userConsentJpaRepository.deleteAll();
        refreshTokenJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        baseballTeamRepository.deleteAll();
    }

    @Test
    @DisplayName("신규 사용자 로그인 시 회원가입 토큰과 팀 목록을 반환한다")
    void should_returnSignupTokenAndTeamList_when_processSocialLoginWithNewUser() {
        // given
        String accessToken = "new_user_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("kakao_new_12345")
                .gender("male")
                .ageRange("20~29")
                .build();

        when(socialLoginClientFactory.getClient(provider)).thenReturn(mockSocialLoginClient);
        when(mockSocialLoginClient.getUserInfo(accessToken)).thenReturn(socialUserInfo);

        // when
        LoginResult result = socialAuthFacade.processSocialLogin(accessToken, provider);

        // then
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.getSignupToken()).isNotNull();
        assertThat(result.getTeamList()).isNotEmpty();
        assertThat(result.getAccessToken()).isNull();
        assertThat(result.getRefreshToken()).isNull();
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 액세스 토큰과 리프레시 토큰을 반환한다")
    void should_returnTokens_when_processSocialLoginWithExistingUser() {
        // given
        String accessToken = "existing_user_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        UserEntity existingUser = UserFixture.createActiveUser("kakao_existing_12345", "기존유저", testTeam);
        userJpaRepository.save(existingUser);

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("kakao_existing_12345")
                .gender("male")
                .ageRange("20~29")
                .build();

        when(socialLoginClientFactory.getClient(provider)).thenReturn(mockSocialLoginClient);
        when(mockSocialLoginClient.getUserInfo(accessToken)).thenReturn(socialUserInfo);

        // when
        LoginResult result = socialAuthFacade.processSocialLogin(accessToken, provider);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("기존유저");
    }

    @Test
    @DisplayName("탈퇴한 사용자 로그인 시 UserWithdrawnException을 발생시킨다")
    void should_throwUserWithdrawnException_when_processSocialLoginWithWithdrawnUser() {
        // given
        String accessToken = "withdrawn_user_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        UserEntity withdrawnUser = UserFixture.createUser(
                1L, "탈퇴유저", testTeam,
                UserEntity.UserStatus.WITHDRAWN, UserEntity.UserRole.USER
        );
        userJpaRepository.save(withdrawnUser);

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId(withdrawnUser.getSocialId())
                .gender("male")
                .ageRange("20~29")
                .build();

        when(socialLoginClientFactory.getClient(provider)).thenReturn(mockSocialLoginClient);
        when(mockSocialLoginClient.getUserInfo(accessToken)).thenReturn(socialUserInfo);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.processSocialLogin(accessToken, provider))
                .isInstanceOf(UserWithdrawnException.class)
                .hasMessage("탈퇴한 사용자입니다.");
    }

    @Test
    @DisplayName("정지된 사용자 로그인 시 UserSuspendedException을 발생시킨다")
    void should_throwUserSuspendedException_when_processSocialLoginWithSuspendedUser() {
        // given
        String accessToken = "suspended_user_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        UserEntity suspendedUser = UserFixture.createUser(
                2L, "정지유저", testTeam,
                UserEntity.UserStatus.SUSPENDED, UserEntity.UserRole.USER
        );
        userJpaRepository.save(suspendedUser);

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId(suspendedUser.getSocialId())
                .gender("male")
                .ageRange("20~29")
                .build();

        when(socialLoginClientFactory.getClient(provider)).thenReturn(mockSocialLoginClient);
        when(mockSocialLoginClient.getUserInfo(accessToken)).thenReturn(socialUserInfo);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.processSocialLogin(accessToken, provider))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessage("정지된 사용자입니다. 관리자에게 문의 하세요.");
    }

    @Test
    @DisplayName("회원가입 완료 시 사용자 정보와 토큰을 반환한다")
    void should_saveUserAndReturnTokens_when_completeSignup() {
        // given
        String signupToken = jwtTokenProvider.generateSignupPendingToken(
                "kakao_signup_12345",
                SocialProvider.KAKAO.name(),
                "male",
                "20~29"
        );

        SignupCompleteRequest request = SignupCompleteRequest.builder()
                .signupToken(signupToken)
                .name("회원가입유저")
                .favoriteTeamCode(testTeam.getCode())
                .agreePersonalInfo(true)
                .agreeMarketing(true)
                .build();

        // when
        LoginResult result = socialAuthFacade.completeSignup(request);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("회원가입유저");

        // DB 검증
        UserEntity savedUser = userJpaRepository.findBySocialIdAndSocialProvider(
                "kakao_signup_12345", SocialProvider.KAKAO
        ).orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("회원가입유저");
    }

    @Test
    @DisplayName("개인정보 동의 없이 회원가입 시 PersonalInfoAgreementRequiredException을 발생시킨다")
    void should_throwException_when_completeSignupWithoutPersonalInfoAgreement() {
        // given
        String signupToken = jwtTokenProvider.generateSignupPendingToken(
                "kakao_signup_67890",
                SocialProvider.KAKAO.name(),
                "male",
                "20~29"
        );

        SignupCompleteRequest request = SignupCompleteRequest.builder()
                .signupToken(signupToken)
                .name("개인정보거부유저")
                .favoriteTeamCode(testTeam.getCode())
                .agreePersonalInfo(false)
                .agreeMarketing(false)
                .build();

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.completeSignup(request))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessage("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다.");
    }

    @Test
    @DisplayName("중복된 이름으로 회원가입 시 NameDuplicateException을 발생시킨다")
    void should_throwNameDuplicateException_when_completeSignupWithDuplicateName() {
        // given
        UserEntity existingUser = UserFixture.createActiveUser("existing_social_id", "중복이름", testTeam);
        userJpaRepository.save(existingUser);

        String signupToken = jwtTokenProvider.generateSignupPendingToken(
                "kakao_new_user",
                SocialProvider.KAKAO.name(),
                "male",
                "20~29"
        );

        SignupCompleteRequest request = SignupCompleteRequest.builder()
                .signupToken(signupToken)
                .name("중복이름")
                .favoriteTeamCode(testTeam.getCode())
                .agreePersonalInfo(true)
                .agreeMarketing(true)
                .build();

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.completeSignup(request))
                .isInstanceOf(NameDuplicateException.class)
                .hasMessage("이미 존재하는 이름입니다.");
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 토큰 재발급 시 새로운 토큰을 반환한다")
    void should_returnNewTokens_when_refreshTokensWithValidToken() {
        // given
        UserEntity user = UserFixture.createActiveUser("social_refresh_test", "리프레시유저", testTeam);
        userJpaRepository.save(user);

        RefreshTokenEntity refreshToken = UserFixture.createRefreshToken(user.getId(), "valid_refresh_token");
        refreshTokenJpaRepository.save(refreshToken);

        // when
        TokenResponse result = socialAuthFacade.refreshTokens("valid_refresh_token");

        // then
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotEqualTo("valid_refresh_token");
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 재발급 시 InvalidTokenException을 발생시킨다")
    void should_throwInvalidTokenException_when_refreshTokensWithNonExistentToken() {
        // given
        String nonExistentToken = "non_existent_refresh_token";

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.refreshTokens(nonExistentToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("만료된 리프레시 토큰으로 재발급 시 InvalidTokenException을 발생시킨다")
    void should_throwInvalidTokenException_when_refreshTokensWithExpiredToken() {
        // given
        UserEntity user = UserFixture.createActiveUser("social_expired_test", "만료토큰유저", testTeam);
        userJpaRepository.save(user);

        RefreshTokenEntity expiredToken = UserFixture.createExpiredRefreshToken(user.getId(), "expired_token");
        refreshTokenJpaRepository.save(expiredToken);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.refreshTokens("expired_token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("만료된 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰이 삭제된다")
    void should_deleteRefreshToken_when_logout() {
        // given
        UserEntity user = UserFixture.createActiveUser("social_logout_test", "로그아웃유저", testTeam);
        userJpaRepository.save(user);

        RefreshTokenEntity refreshToken = UserFixture.createRefreshToken(user.getId(), "logout_refresh_token");
        refreshTokenJpaRepository.save(refreshToken);

        // when
        socialAuthFacade.logout(user.getId());

        // then
        // 리프레시 토큰이 삭제되었는지 확인
        assertThat(refreshTokenJpaRepository.findByToken("logout_refresh_token")).isEmpty();
    }

    @Test
    @DisplayName("이름 중복 확인 시 중복된 이름이면 true를 반환한다")
    void should_returnTrue_when_isNameDuplicateWithExistingName() {
        // given
        UserEntity existingUser = UserFixture.createActiveUser("social_dup_test", "중복체크", testTeam);
        userJpaRepository.save(existingUser);

        // when
        boolean result = socialAuthFacade.isNameDuplicate("중복체크");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("이름 중복 확인 시 중복되지 않은 이름이면 false를 반환한다")
    void should_returnFalse_when_isNameDuplicateWithNonExistingName() {
        // when
        boolean result = socialAuthFacade.isNameDuplicate("사용가능한이름");

        // then
        assertThat(result).isFalse();
    }
}
