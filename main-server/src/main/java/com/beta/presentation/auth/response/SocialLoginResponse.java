package com.beta.presentation.auth.response;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.dto.TeamDto;
import com.beta.application.auth.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SocialLoginResponse {

    private boolean isNewUser;
    private UserResponse userResponse;

    public static SocialLoginResponse ofLoginResult(LoginResult loginResult) {
        if (loginResult.isNewUser()) {
            return SocialLoginResponse.builder()
                    .isNewUser(true)
                    .userResponse(new NewUserResponse(loginResult.getSignupToken(), loginResult.getTeamList()))
                    .build();
        } else {
            return SocialLoginResponse.builder()
                    .isNewUser(false)
                    .userResponse(new ExistingUserResponse(
                            loginResult.getAccessToken(),
                            loginResult.getRefreshToken(),
                            loginResult.getUserInfo()
                    ))
                    .build();
        }
    }

    private interface UserResponse {}
    private record NewUserResponse(String signupToken, List<TeamDto> teamList) implements UserResponse {}
    private record ExistingUserResponse(String accessToken, String refreshToken, UserDto user) implements UserResponse {}
}
