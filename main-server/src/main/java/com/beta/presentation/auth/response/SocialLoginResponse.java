package com.beta.presentation.auth.response;

import com.beta.application.auth.dto.LoginResult;
import com.beta.application.auth.dto.UserDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialLoginResponse {

    private boolean isNewUser;
    private UserResponse userResponse;

    public static SocialLoginResponse ofLoginResult(LoginResult loginResult) {
        if (loginResult.isNewUser()) {
            return SocialLoginResponse.builder()
                    .isNewUser(true)
                    .userResponse(new NewUserResponse(loginResult.getSignupToken()))
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

    @Getter
    private static class NewUserResponse implements UserResponse {
        private final String signupToken;
        public NewUserResponse(String signupToken) {
            this.signupToken = signupToken;
        }
    }

    @Getter
    private static class ExistingUserResponse implements UserResponse {
        private final String accessToken;
        private final String refreshToken;
        private final UserDto user;

        public ExistingUserResponse(String accessToken, String refreshToken, UserDto user) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
        }
    }
}
