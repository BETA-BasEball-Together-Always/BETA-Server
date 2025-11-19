package com.beta.application.auth.facade;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.dto.TeamDto;
import com.beta.application.auth.dto.UserDto;
import com.beta.application.auth.mapper.UserMapper;
import com.beta.application.auth.service.UserReadService;
import com.beta.application.auth.service.RefreshTokenService;
import com.beta.application.auth.service.UserWriteService;
import com.beta.application.auth.service.SocialUserInfoService;
import com.beta.application.common.service.FindTeamService;
import com.beta.common.exception.auth.EmailDuplicateException;
import com.beta.common.exception.auth.InvalidPasswordException;
import com.beta.common.exception.auth.NameDuplicateException;
import com.beta.common.provider.SocialProvider;
import com.beta.common.security.JwtTokenProvider;
import com.beta.domain.auth.User;
import com.beta.domain.auth.service.SocialUserStatusService;
import com.beta.infra.auth.client.SocialUserInfo;
import com.beta.infra.common.entity.BaseballTeamEntity;
import com.beta.presentation.auth.request.SignupCompleteRequest;
import com.beta.presentation.auth.response.TokenResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserReadService userReadService;
    private final RefreshTokenService refreshTokenService;
    private final UserWriteService userWriteService;
    private final PasswordEncoder passwordEncoder;

    private final SocialUserStatusService socialUserStatusService;

    public LoginResult processSocialLogin(String token, SocialProvider socialProvider) {
        SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(token, socialProvider);

        User user = userReadService.findUserBySocialId(socialUserInfo.getSocialId(), socialProvider);

        if (socialUserStatusService.isNewUser(user)) {
            List<TeamDto> teamList = UserMapper.teamList(findTeamService.getAllBaseballTeams());
            return LoginResult.forNewUser(true, teamList, socialProvider.name());
        } else{
            socialUserStatusService.validateUserStatus(user);
            return createLoginResult(
                    user.getId(),
                    user.getFavoriteTeamCode(),
                    user.getRole(),
                    UserMapper.toDto(user),
                    socialProvider.name()
            );
        }
    }

    public LoginResult processEmailLogin(String email, String password) {
        User user = userReadService.findUserByEmail(email);
        socialUserStatusService.validateUserStatus(user);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
        return createLoginResult(
                user.getId(),
                user.getFavoriteTeamCode(),
                user.getRole(),
                UserMapper.toDto(user),
                "EMAIL"
        );
    }

    @Transactional
    public LoginResult completeSignup(SignupCompleteRequest request) {
        if (userReadService.isEmailDuplicate(request.getEmail())) {
            throw new EmailDuplicateException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        if (userReadService.isNameDuplicate(request.getNickName())) {
            throw new NameDuplicateException("이미 존재하는 닉네임입니다: " + request.getNickName());
        }
        String socialId = null;
        if (request.getSocialToken() != null && !request.getSocialToken().trim().isEmpty()) {
            SocialUserInfo socialUserInfo = socialUserInfoService.fetchSocialUserInfo(
                    request.getSocialToken(),
                    SocialProvider.valueOf(request.getSocial())
            );
            socialId = socialUserInfo.getSocialId();
        }

        BaseballTeamEntity baseballTeamEntity = findTeamService.getBaseballTeamById(request.getFavoriteTeamCode());

        UserDto userDto = UserDto.builder()
                .socialId(socialId)
                .email(request.getEmail())
                .password(request.getPassword())
                .nickName(request.getNickName())
                .socialProvider(SocialProvider.valueOf(request.getSocial()))
                .gender(request.getGender())
                .age(request.getAge())
                .favoriteTeamCode(baseballTeamEntity.getCode())
                .favoriteTeamName(baseballTeamEntity.getTeamNameKr())
                .build();

        UserDto savedUser = userWriteService.saveUser(userDto, baseballTeamEntity);

        userWriteService.saveAgreements(
                request.getAgreeMarketing(),
                request.getPersonalInfoRequired(),
                savedUser.getId()
        );

        return createLoginResult(
                savedUser.getId(),
                savedUser.getFavoriteTeamCode(),
                savedUser.getRole(),
                savedUser,
                request.getSocial()
        );
    }

    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        Long userId = refreshTokenService.findUserIdByToken(refreshToken);

        User user = userReadService.findUserById(userId);
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

    public boolean isNameDuplicate(String nickName) {
        return userReadService.isNameDuplicate(nickName);
    }

    public boolean isEmailDuplicate(String email) {
        return userReadService.isEmailDuplicate(email);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    private LoginResult createLoginResult(Long id, String favoriteTeamCode, String role, UserDto userDto, String social) {
        String accessToken = jwtTokenProvider.generateAccessToken(id, favoriteTeamCode, role);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenService.upsertRefreshToken(id, refreshToken);
        return LoginResult.forExistingUser(
                false, accessToken, refreshToken, userDto, social
        );
    }
}
