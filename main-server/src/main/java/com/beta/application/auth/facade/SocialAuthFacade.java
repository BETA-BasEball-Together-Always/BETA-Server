package com.beta.application.auth.facade;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.application.auth.service.FindUserService;
import com.beta.application.auth.service.RefreshTokenService;
import com.beta.application.auth.service.FindSocialService;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.domain.auth.User;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.infra.auth.client.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SocialAuthFacade {

    private final JwtTokenProvider jwtTokenProvider;

    private final SocialUserStatusService socialUserStatusService;
    private final FindSocialService findSocialService;
    private final FindUserService findUserService;
    private final RefreshTokenService refreshTokenService;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialUserInfo socialUserInfo = findSocialService.getSocialUserInfo(token, socialProvider);

        User user = UserMapper.toDomain(findUserService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider));
        socialUserStatusService.validateUserStatus(user);

        if (socialUserStatusService.isNewUser(user)) {
            String signupToken = jwtTokenProvider.generateSignupPendingToken(socialUserInfo.getSocialId(), socialProvider.name(), socialUserInfo.getGender(), socialUserInfo.getAgeRange());
            return LoginResult.forNewUser(true, signupToken);
        } else{
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getFavoriteTeamCode(), user.getRole());
            String refreshToken = UUID.randomUUID().toString();
            refreshTokenService.deleteRefreshToken(user.getId());
            refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
            return LoginResult.forExistingUser(false, accessToken, refreshToken, user);
        }
    }
}
