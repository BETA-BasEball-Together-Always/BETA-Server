package com.beta.application.auth.dto;

import com.beta.common.provider.SocialProvider;
import com.beta.domain.auth.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {
    private Long id;
    private String socialId;
    private String name;
    private SocialProvider socialProvider;
    private String favoriteTeamCode;
}
