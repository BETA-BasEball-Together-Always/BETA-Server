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
    private final String accessToken;
    private final String refreshToken;
    private final UserDto userInfo;
    private final String social;
    private final List<TeamDto> teamList;

    public static LoginResult forNewUser(boolean isNewUser, List<TeamDto> teamList, String social) {
        return LoginResult.builder()
                .isNewUser(isNewUser)
                .teamList(teamList)
                .social(social)
                .accessToken(null)
                .refreshToken(null)
                .userInfo(null)
                .build();
    }

    public static LoginResult forExistingUser(boolean isNewUser, String accessToken, String refreshToken, UserDto user, String social) {
        return LoginResult.builder()
                .isNewUser(isNewUser)
                .social(social)
                .teamList(null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userInfo(user)
                .build();
    }
}
