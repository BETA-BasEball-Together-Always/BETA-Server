package com.beta.application.auth.facade;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.dto.TeamDto;
import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.application.auth.service.FindUserService;
import com.beta.application.auth.service.RefreshTokenService;
import com.beta.application.auth.service.SaveUserService;
import com.beta.application.auth.service.SocialUserInfoService;
import com.beta.application.common.service.FindTeamService;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.domain.auth.User;
import com.beta.domain.auth.service.RefreshTokenValidationService;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialAuthFacade {

    private final JwtTokenProvider jwtTokenProvider;

    private final FindTeamService findTeamService;
    private final SocialUserInfoService socialUserInfoService;
    private final FindUserService findUserService;
    private final RefreshTokenService refreshTokenService;
    private final SaveUserService saveUserService;

    private final SocialUserStatusService socialUserStatusService;
    private final RefreshTokenValidationService refreshTokenValidationService;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, socialProvider);

        User user = findUserService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider);

        if (socialUserStatusService.isNewUser(user)) {
            // 신규 사용자
            String signupToken = jwtTokenProvider.generateSignupPendingToken(
                    socialUserInfo.getSocialId(),
                    socialProvider.name(),
                    socialUserInfo.getGender(),
                    socialUserInfo.getAgeRange()
            );
            List<TeamDto> teamList = UserMapper.teamList(findTeamService.getAllBaseballTeams());
            return LoginResult.forNewUser(true, signupToken, teamList);
        } else{
            // 기존 사용자
            socialUserStatusService.validateUserStatus(user);
            return createLoginResult(
                    user.getId(),
                    user.getFavoriteTeamCode(),
                    user.getRole(),
                    UserMapper.toDto(user)
            );
        }
    }

    @Transactional
    public LoginResult completeSignup(SignupCompleteRequest request) {
        socialUserStatusService.validateAgreePersonalInfo(request.getAgreePersonalInfo());
        socialUserStatusService.validateNameDuplicate(
                findUserService.isNameDuplicate(request.getName())
        );

        UserDto userDto = getSocialUserInfo(request);
        UserDto savedUser = saveUserService.saveUser(userDto, findTeamService.getBaseballTeamById(request.getFavoriteTeamCode()));
        saveUserService.saveAgreements(request.getAgreeMarketing(), request.getAgreePersonalInfo(), savedUser.getId());
        return createLoginResult(savedUser.getId(), savedUser.getFavoriteTeamCode(), savedUser.getRole(), savedUser);
    }

    public boolean isNameDuplicate(String name) {
        return findUserService.isNameDuplicate(name);
    }

    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenService.findByToken(refreshToken);
        refreshTokenValidationService.validateRefreshToken(refreshTokenEntity);

        User user = findUserService.findUserById(refreshTokenEntity.getUserId());
        socialUserStatusService.validateUserStatus(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(
            user.getId(),
            user.getFavoriteTeamCode(),
            user.getRole()
        );
        String newRefreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(user.getId(), newRefreshToken);

        return TokenResponse.from(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    private UserDto getSocialUserInfo(SignupCompleteRequest request) {
        String socialId = jwtTokenProvider.getSubject(request.getSignupToken());

        SocialProvider socialProvider = SocialProvider.valueOf(
                jwtTokenProvider.getClaim(
                        request.getSignupToken(),
                        JwtTokenProvider.ClaimEnum.PROVIDER.name(),
                        String.class
                )
        );
        String gender = jwtTokenProvider.getClaim(
                request.getSignupToken(),
                JwtTokenProvider.ClaimEnum.GENDER.name(),
                String.class
        );
        String ageRange = jwtTokenProvider.getClaim(
                request.getSignupToken(),
                JwtTokenProvider.ClaimEnum.AGE_RANGE.name(),
                String.class
        );

        return UserDto.builder()
                .socialId(socialId)
                .name(request.getName())
                .favoriteTeamCode(request.getFavoriteTeamCode())
                .socialProvider(socialProvider)
                .ageRange(ageRange)
                .gender(gender)
                .build();
    }

    private LoginResult createLoginResult(Long id, String favoriteTeamCode, String role, UserDto userDto) {
        String accessToken = jwtTokenProvider.generateAccessToken(id, favoriteTeamCode, role);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(id, refreshToken);
        return LoginResult.forExistingUser(
                false, accessToken, refreshToken, userDto
        );
    }
}
