package com.beta.application.auth.facade;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.dto.TeamDto;
import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.service.FindUserService;
import com.beta.application.auth.service.RefreshTokenService;
import com.beta.application.auth.service.SaveUserService;
import com.beta.application.auth.service.SocialUserInfoService;
import com.beta.application.common.service.FindTeamService;
import com.beta.common.exception.auth.InvalidTokenException;
import com.beta.common.exception.auth.PersonalInfoAgreementRequiredException;
import com.beta.common.exception.auth.UserSuspendedException;
import com.beta.common.exception.auth.UserWithdrawnException;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.domain.auth.User;
import com.beta.domain.auth.service.RefreshTokenValidationService;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.response.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SocialAuthFacade 단위 테스트")
class SocialAuthFacadeTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FindTeamService findTeamService;

    @Mock
    private SocialUserStatusService socialUserStatusService;

    @Mock
    private SocialUserInfoService socialUserInfoService;

    @Mock
    private FindUserService findUserService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private SaveUserService saveUserService;
    
    @Mock
    private RefreshTokenValidationService refreshTokenValidationService;

    @InjectMocks
    private SocialAuthFacade socialAuthFacade;

    @Test
    @DisplayName("신규 사용자 소셜 로그인 시 signupToken과 팀 목록을 반환한다")
    void processSocialLogin_newUser_returnsSignupTokenAndTeamList() {
        // given
        String token = "kakao_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("kakao_12345")
                .gender("M")
                .ageRange("20-29")
                .build();

        List<BaseballTeamEntity> teamEntities = Arrays.asList(
                createTeam("KIA", "KIA 타이거즈"),
                createTeam("LG", "LG 트윈스")
        );

        when(socialUserInfoService.fetchSocialUserInfo(token, provider)).thenReturn(socialUserInfo);
        when(findUserService.findUserBySocialId(socialUserInfo.getSocialId(), provider)).thenReturn(null);
        when(socialUserStatusService.isNewUser(null)).thenReturn(true);
        when(jwtTokenProvider.generateSignupPendingToken(
                socialUserInfo.getSocialId(),
                provider.name(),
                socialUserInfo.getGender(),
                socialUserInfo.getAgeRange()
        )).thenReturn("signup_token_12345");
        when(findTeamService.getAllBaseballTeams()).thenReturn(teamEntities);

        // when
        LoginResult result = socialAuthFacade.processSocialLogin(token, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.getSignupToken()).isEqualTo("signup_token_12345");
        assertThat(result.getTeamList()).hasSize(2);
        assertThat(result.getAccessToken()).isNull();
        assertThat(result.getRefreshToken()).isNull();

        verify(socialUserInfoService, times(1)).fetchSocialUserInfo(token, provider);
        verify(socialUserStatusService, times(1)).isNewUser(null);
        verify(findTeamService, times(1)).getAllBaseballTeams();
    }

    @Test
    @DisplayName("기존 사용자 소셜 로그인 시 accessToken과 refreshToken을 반환한다")
    void processSocialLogin_existingUser_returnsTokens() {
        // given
        String token = "kakao_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("kakao_67890")
                .build();

        User existingUser = User.builder()
                .id(1L)
                .socialId("kakao_67890")
                .name("김철수")
                .socialProvider(SocialProvider.KAKAO)
                .favoriteTeamCode("KIA")
                .favoriteTeamName("KIA 타이거즈")
                .status(UserEntity.UserStatus.ACTIVE.name())
                .role(UserEntity.UserRole.USER.name())
                .build();

        when(socialUserInfoService.fetchSocialUserInfo(token, provider)).thenReturn(socialUserInfo);
        when(findUserService.findUserBySocialId(socialUserInfo.getSocialId(), provider)).thenReturn(existingUser);
        when(socialUserStatusService.isNewUser(existingUser)).thenReturn(false);
        when(jwtTokenProvider.generateAccessToken(existingUser.getId(), existingUser.getFavoriteTeamCode(), existingUser.getRole()))
                .thenReturn("access_token_12345");

        // when
        LoginResult result = socialAuthFacade.processSocialLogin(token, provider);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo("access_token_12345");
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("김철수");

        verify(socialUserStatusService, times(1)).validateUserStatus(existingUser);
        verify(refreshTokenService, times(1)).upsertRefreshToken(eq(existingUser.getId()), anyString());
    }

    @Test
    @DisplayName("탈퇴한 사용자가 로그인 시도 시 UserWithdrawnException을 발생시킨다")
    void processSocialLogin_withdrawnUser_throwsException() {
        // given
        String token = "kakao_access_token";
        SocialProvider provider = SocialProvider.KAKAO;

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("kakao_99999")
                .build();

        User withdrawnUser = User.builder()
                .id(1L)
                .socialId("kakao_99999")
                .status(UserEntity.UserStatus.WITHDRAWN.name())
                .role(UserEntity.UserRole.USER.name())
                .build();

        when(socialUserInfoService.fetchSocialUserInfo(token, provider)).thenReturn(socialUserInfo);
        when(findUserService.findUserBySocialId(socialUserInfo.getSocialId(), provider)).thenReturn(withdrawnUser);
        when(socialUserStatusService.isNewUser(withdrawnUser)).thenReturn(false);
        doThrow(new UserWithdrawnException("탈퇴한 사용자입니다."))
                .when(socialUserStatusService).validateUserStatus(withdrawnUser);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.processSocialLogin(token, provider))
                .isInstanceOf(UserWithdrawnException.class)
                .hasMessageContaining("탈퇴한 사용자입니다");

        verify(socialUserStatusService, times(1)).validateUserStatus(withdrawnUser);
    }

    @Test
    @DisplayName("정지된 사용자가 로그인 시도 시 UserSuspendedException을 발생시킨다")
    void processSocialLogin_suspendedUser_throwsException() {
        // given
        String token = "naver_access_token";
        SocialProvider provider = SocialProvider.NAVER;

        SocialUserInfo socialUserInfo = SocialUserInfo.builder()
                .socialId("naver_88888")
                .build();

        User suspendedUser = User.builder()
                .id(2L)
                .socialId("naver_88888")
                .status(UserEntity.UserStatus.SUSPENDED.name())
                .role(UserEntity.UserRole.USER.name())
                .build();

        when(socialUserInfoService.fetchSocialUserInfo(token, provider)).thenReturn(socialUserInfo);
        when(findUserService.findUserBySocialId(socialUserInfo.getSocialId(), provider)).thenReturn(suspendedUser);
        when(socialUserStatusService.isNewUser(suspendedUser)).thenReturn(false);
        doThrow(new UserSuspendedException("정지된 사용자입니다. 관리자에게 문의 하세요."))
                .when(socialUserStatusService).validateUserStatus(suspendedUser);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.processSocialLogin(token, provider))
                .isInstanceOf(UserSuspendedException.class)
                .hasMessageContaining("정지된 사용자입니다");

        verify(socialUserStatusService, times(1)).validateUserStatus(suspendedUser);
    }

    @Test
    @DisplayName("회원가입 완료 시 사용자를 저장하고 토큰을 반환한다")
    void completeSignup_success_returnsTokens() {
        // given
        SignupCompleteRequest request = SignupCompleteRequest.builder()
                .signupToken("signup_token_12345")
                .name("홍길동")
                .favoriteTeamCode("KIA")
                .agreePersonalInfo(true)
                .agreeMarketing(false)
                .build();

        BaseballTeamEntity teamEntity = createTeam("KIA", "KIA 타이거즈");

        UserDto savedUserDto = UserDto.builder()
                .id(1L)
                .socialId("kakao_12345")
                .name("홍길동")
                .socialProvider(SocialProvider.KAKAO)
                .favoriteTeamCode("KIA")
                .favoriteTeamName("KIA 타이거즈")
                .role(UserEntity.UserRole.USER.name())
                .build();

        when(jwtTokenProvider.getSubject(request.getSignupToken())).thenReturn("kakao_12345");
        when(jwtTokenProvider.getClaim(request.getSignupToken(), JwtTokenProvider.ClaimEnum.PROVIDER.name(), String.class))
                .thenReturn("KAKAO");
        when(jwtTokenProvider.getClaim(request.getSignupToken(), JwtTokenProvider.ClaimEnum.GENDER.name(), String.class))
                .thenReturn("M");
        when(jwtTokenProvider.getClaim(request.getSignupToken(), JwtTokenProvider.ClaimEnum.AGE_RANGE.name(), String.class))
                .thenReturn("20-29");
        when(findTeamService.getBaseballTeamById("KIA")).thenReturn(teamEntity);
        when(saveUserService.saveUser(any(UserDto.class), eq(teamEntity))).thenReturn(savedUserDto);
        when(jwtTokenProvider.generateAccessToken(savedUserDto.getId(), savedUserDto.getFavoriteTeamCode(), savedUserDto.getRole()))
                .thenReturn("access_token_67890");

        // when
        LoginResult result = socialAuthFacade.completeSignup(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getAccessToken()).isEqualTo("access_token_67890");
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getUserInfo()).isNotNull();
        assertThat(result.getUserInfo().getName()).isEqualTo("홍길동");

        verify(socialUserStatusService, times(1)).validateAgreePersonalInfo(true);
        verify(saveUserService, times(1)).saveUser(any(UserDto.class), eq(teamEntity));
        verify(saveUserService, times(1)).saveAgreements(false, true, 1L);
        verify(refreshTokenService, times(1)).upsertRefreshToken(eq(1L), anyString());
    }

    @Test
    @DisplayName("개인정보 동의하지 않고 회원가입 시도 시 PersonalInfoAgreementRequiredException을 발생시킨다")
    void completeSignup_withoutPersonalInfoAgreement_throwsException() {
        // given
        SignupCompleteRequest request = SignupCompleteRequest.builder()
                .signupToken("signup_token_12345")
                .name("홍길동")
                .favoriteTeamCode("KIA")
                .agreePersonalInfo(false)
                .agreeMarketing(false)
                .build();

        doThrow(new PersonalInfoAgreementRequiredException("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다."))
                .when(socialUserStatusService).validateAgreePersonalInfo(false);

        // when & then
        assertThatThrownBy(() -> socialAuthFacade.completeSignup(request))
                .isInstanceOf(PersonalInfoAgreementRequiredException.class)
                .hasMessageContaining("개인정보 수집 및 이용에 동의하셔야 회원가입이 가능합니다");

        verify(socialUserStatusService, times(1)).validateAgreePersonalInfo(false);
        verify(saveUserService, never()).saveUser(any(), any());
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰을 삭제한다")
    void logout_success_deletesRefreshToken() {
        // given
        Long userId = 1L;
        
        // when
        socialAuthFacade.logout(userId);
        
        // then
        verify(refreshTokenService).deleteByUserId(userId);
    }
    
    @Test
    @DisplayName("닉네임 중복 확인 - 중복된 경우 true를 반환한다")
    void isNameDuplicate_duplicateName_returnsTrue() {
        // given
        String name = "김철수";
        when(findUserService.isNameDuplicate(name)).thenReturn(true);
        
        // when
        boolean result = socialAuthFacade.isNameDuplicate(name);
        
        // then
        assertThat(result).isTrue();
        verify(findUserService).isNameDuplicate(name);
    }
    
    @Test
    @DisplayName("닉네임 중복 확인 - 중복되지 않은 경우 false를 반환한다")
    void isNameDuplicate_uniqueName_returnsFalse() {
        // given
        String name = "홍길동";
        when(findUserService.isNameDuplicate(name)).thenReturn(false);
        
        // when
        boolean result = socialAuthFacade.isNameDuplicate(name);
        
        // then
        assertThat(result).isFalse();
        verify(findUserService).isNameDuplicate(name);
    }

    // Helper method
    private BaseballTeamEntity createTeam(String code, String nameKr) {
        return BaseballTeamEntity.builder()
                .code(code)
                .teamNameKr(nameKr)
                .teamNameEn(nameKr)
                .homeStadium("Stadium")
                .stadiumAddress("Address")
                .build();
    }
}
