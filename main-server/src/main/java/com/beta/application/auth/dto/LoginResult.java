package com.beta.application.auth.dto;

import com.beta.application.auth.mapper.UserMapper;
import com.beta.domain.auth.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class LoginResult {
    private final boolean isNewUser;
    private final String signupToken;
    private final String accessToken;
    private final String refreshToken;
    private final UserDto userInfo;
    private final List<TeamDto> teamList;

    public static LoginResult forNewUser(boolean isNewUser, String signupToken, List<TeamDto> teamList) {
        return LoginResult.builder()
                .isNewUser(isNewUser)
                .signupToken(signupToken)
                .teamList(teamList)
                .accessToken(null)
                .refreshToken(null)
                .userInfo(null)
                .build();
    }

    public static LoginResult forExistingUser(boolean isNewUser, String accessToken, String refreshToken, UserDto user) {
        return LoginResult.builder()
                .isNewUser(isNewUser)
                .signupToken(null)
                .teamList(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(user)
                .build();
    }
}
