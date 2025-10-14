package com.beta.application.auth;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.common.exception.UserSuspendedException;
import com.beta.common.exception.UserWithdrawnException;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.domain.auth.User;
import com.beta.infra.auth.client.SocialLoginClient;
import com.beta.infra.auth.client.SocialLoginClientFactory;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.auth.entity.RefreshTokenEntity;
import com.beta.infra.auth.entity.UserEntity;
import com.beta.infra.auth.repository.RefreshTokenJpaRepository;
import com.beta.infra.auth.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialLoginService {

    private final SocialLoginClientFactory clientFactory;
    private final UserJpaRepository userJpaRepository;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final SocialUserStatusService socialUserStatusService;

    @Transactional
    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialLoginClient client = clientFactory.getClient(socialProvider);
        SocialUserInfo socialUserInfo = client.getUserInfo(token);

        User user = UserMapper.toDomain(userJpaRepository.findBySocialIdAndSocialProvider(socialUserInfo.getSocialId(), socialProvider).orElse(null));
        if(user != null){
            socialUserStatusService.validateUserStatus(user);
        }

        boolean isNewUser = socialUserStatusService.isNewUser(user);

        if (isNewUser) {
            String signupToken = jwtTokenProvider.generateSignupPendingToken(socialUserInfo.getSocialId(), socialProvider.name(), socialUserInfo.getGender(), socialUserInfo.getAgeRange());
            return LoginResult.forNewUser(true, signupToken);
        } else {
            String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getFavoriteTeamCode(), user.getRole());
            String refreshToken = UUID.randomUUID().toString();

            saveRefreshToken(RefreshTokenEntity.builder()
                    .userId(user.getId())
                    .token(refreshToken)
                    .expiresAt(LocalDateTime.now().plusMonths(1))
                    .build());

            return LoginResult.forExistingUser(false, accessToken, refreshToken, user);
        }
    }

    private void saveRefreshToken(RefreshTokenEntity refreshTokenEntity) {
        refreshTokenJpaRepository.deleteByUserId(refreshTokenEntity.getUserId());
        refreshTokenJpaRepository.save(refreshTokenEntity);
    }
}
